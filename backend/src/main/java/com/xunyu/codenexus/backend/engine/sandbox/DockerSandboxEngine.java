package com.xunyu.codenexus.backend.engine.sandbox;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.xunyu.codenexus.backend.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * CodeNexus 核心 Docker 沙箱执行引擎
 * 负责底层容器的生命周期管理与安全隔离
 *
 * @author CodeNexusBuilder (The Core Specialist)
 */
@Slf4j
@Component
public class DockerSandboxEngine {

    // 限制单次代码执行的最大超时时间 (毫秒)
    private static final long TIME_LIMIT_MS = 3000L;
    private final DockerClient dockerClient;

    public DockerSandboxEngine() {
        // 初始化 Docker 客户端
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    /**
     * 同步执行单次输入测试 (高度安全管控)
     */
    public SandboxResult executeJava(String code, String input) {
        String containerId = null;
        long startTime = System.currentTimeMillis();

        try {
            // 1. [安全性] 硬件资源限制 (Cgroups)
            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withMemory(256 * 1024 * 1024L)       // 限制内存 256MB
                    .withMemorySwap(256 * 1024 * 1024L)   // 禁用 Swap 交换区，防止 OOM 逃逸
                    .withCpuCount(1L)                     // 限制使用 1 个 CPU 核心
                    .withNetworkMode("none");             // 禁用网络，严防反弹 Shell

            // ================= 跨平台终极修复点 =================
            // 抹除 Windows 特有的 \r (回车符)，防止破坏 Linux 的 Shell Here-Doc 结构！
            String safeCode = code != null ? code.replace("\r", "") : "";
            String safeInput = input != null ? input.replace("\r", "") : "";

            // 1.5 [稳定性] 构建防弹 Shell 脚本
            // 【核心修复】：将代码与输入数据的写入分离，并使用 && 严格级联编译与运行。
            // 保证一旦 javac 失败，立即终止，将最真实的编译错误返回给前端！
            String shellScript = "cat << 'EOF' > Solution.java\n" +
                    safeCode + "\n" +
                    "EOF\n" +
                    "cat << 'EOF' > input.txt\n" +
                    safeInput + "\n" +
                    "EOF\n" +
                    "javac -encoding UTF-8 Solution.java && java Solution < input.txt";
            // ====================================================

            // 2. 创建容器 (使用企业级官方轻量镜像)
            CreateContainerResponse container = dockerClient.createContainerCmd("eclipse-temurin:17-alpine")
                    .withHostConfig(hostConfig)
                    .withCmd("sh", "-c", shellScript)
                    .exec();

            containerId = container.getId();

            // 3. 启动容器
            dockerClient.startContainerCmd(containerId).exec();

            // 4. [稳定性] 超时熔断机制
            final String finalContainerId = containerId;
            CompletableFuture<Boolean> executionFuture = CompletableFuture.supplyAsync(() -> {
                // 等待容器执行完毕，获取退出码 (必须使用 Integer)
                Integer exitCode = dockerClient.waitContainerCmd(finalContainerId).start().awaitStatusCode();
                return exitCode != null && exitCode == 0;
            });

            // 阻塞等待，如果超过时间会抛出 TimeoutException
            boolean isSuccess = executionFuture.get(TIME_LIMIT_MS, TimeUnit.MILLISECONDS);

            long timeCost = System.currentTimeMillis() - startTime;

            // 5. 抓取日志输出
            String stdout = dockerClient.logContainerCmd(containerId)
                    .withStdOut(true).withStdErr(true).withTailAll()
                    .exec(new LogStringCallback()).awaitCompletion().toString();

            if (!isSuccess) {
                return new SandboxResult("ERROR", stdout, timeCost);
            }
            return new SandboxResult("SUCCESS", stdout, timeCost);

        } catch (TimeoutException e) {
            log.warn("[沙箱引擎] 容器执行超时，触发熔断！ContainerID: {}", containerId);
            return new SandboxResult("TLE", "Time Limit Exceeded", TIME_LIMIT_MS);
        } catch (Exception e) {
            log.error("[沙箱引擎] 沙箱执行系统异常: ", e);
            throw new BusinessException(500, "沙箱底层执行异常: " + e.getMessage());
        } finally {
            // 6. [绝对红线清理] 无论成功失败，强制销毁容器，回收宿主机资源
            if (containerId != null) {
                try {
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                } catch (Exception e) {
                    log.error("[沙箱引擎] 容器清理失败，可能产生泄漏！ContainerID: {}", containerId);
                }
            }
        }
    }

    public record SandboxResult(String status, String output, long timeCost) {
    }
}

class LogStringCallback extends com.github.dockerjava.api.async.ResultCallbackTemplate<LogStringCallback, com.github.dockerjava.api.model.Frame> {
    private final StringBuilder log = new StringBuilder();

    @Override
    public void onNext(com.github.dockerjava.api.model.Frame frame) {
        log.append(new String(frame.getPayload()));
    }

    @Override
    public String toString() {
        return log.toString().trim();
    }
}
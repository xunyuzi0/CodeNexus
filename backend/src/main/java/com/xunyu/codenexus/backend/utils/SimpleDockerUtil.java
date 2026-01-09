package com.xunyu.codenexus.backend.utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xunyu
 * @date 2026/1/10 1:33
 * @description: 简单的工具类，用于测试
 */
public class SimpleDockerUtil {
    
    public static String runCode (String code) throws IOException, InterruptedException {
        // 1. 准备宿主机存放代码的临时目录 (放在项目根目录下的 temp 文件夹)
        String userDir = System.getProperty("user.dir");
        String codeParentPath = userDir + File.separator + "tempCode";
        File codeParentFile = new File(codeParentPath);
        if (!codeParentFile.exists()) {
            codeParentFile.mkdirs();
        }
        
        // 2. 把用户的代码写入 Main.java
        // 【注意】文件名必须是 Main.java，因为 Docker 命令里我写死了 javac Main.java
        String codePath = codeParentPath + File.separator + "Main.java";
        File codeFile = new File(codePath);
        Files.write(Paths.get(codePath), code.getBytes(StandardCharsets.UTF_8));
        
        // 3. 构建 Docker 命令
        // 这里的逻辑是：挂载宿主机目录 -> 编译 -> 运行
        // 镜像名 eclipse-temurin:8-jdk-alpine 是你之前选的
        List<String> cmdList = new ArrayList<>();
        cmdList.add("docker");
        cmdList.add("run");
        cmdList.add("--rm"); // 运行完自动删除容器
        cmdList.add("-v");   // 挂载卷
        cmdList.add(codeParentPath + ":/app"); // 宿主机路径:容器内路径
        cmdList.add("-w");
        cmdList.add("/app"); // 指定工作目录
        cmdList.add("eclipse-temurin:8-jdk-alpine"); // 镜像名
        cmdList.add("sh");
        cmdList.add("-c");
        cmdList.add("javac Main.java && java Main"); // 真正执行的命令
        
        // 4. 执行命令
        ProcessBuilder processBuilder = new ProcessBuilder(cmdList);
        Process process = processBuilder.start();
        
        // 5. 获取输出 (标准输出 + 错误输出)
        StringBuilder output = new StringBuilder();
        
        // 读取正常输出
        BufferedReader inputReader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = inputReader.readLine()) != null) {
            output.append(line)
                    .append("\n");
        }
        
        // 读取报错信息 (比如编译失败)
        BufferedReader errorReader =
                new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = errorReader.readLine()) != null) {
            output.append(line)
                    .append("\n");
        }
        
        // 等待执行结束
        process.waitFor();
        
        return output.toString();
    }
}

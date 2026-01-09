package com.xunyu.codenexus.backend.controller;
import com.xunyu.codenexus.backend.model.dto.ExecuteCodeRequest;
import com.xunyu.codenexus.backend.utils.SimpleDockerUtil;
import org.springframework.web.bind.annotation.*;

/**
 * @author xunyu
 * @date 2026/1/10 1:34
 * @description: 测试控制器
 */
@RestController
@RequestMapping("/api/code")
@CrossOrigin(origins = "*")
public class CodeController {
    
    @PostMapping("/run")
    public String runCode (@RequestBody ExecuteCodeRequest request) {
        try {
            // 调用刚才写的简易 Docker 工具
            return SimpleDockerUtil.runCode(request.getCode());
        } catch (Exception e) {
            e.printStackTrace();
            return "后端运行出错: " + e.getMessage();
        }
    }
}
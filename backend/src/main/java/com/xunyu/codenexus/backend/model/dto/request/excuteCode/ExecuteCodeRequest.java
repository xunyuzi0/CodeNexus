package com.xunyu.codenexus.backend.model.dto.request.excuteCode;
import lombok.Data;

/**
 * @author xunyu
 * @date 2026/1/10 1:37
 * @description: 测试类
 */
@Data
public class ExecuteCodeRequest {
    
    /**
     * 用户输入的代码字符串
     */
    private String code;
    
    /**
     * 编程语言 (比如 "java", "python")
     * 虽然目前我们只跑 Java，但留着这个字段为了以后扩展
     */
    private String language;
}
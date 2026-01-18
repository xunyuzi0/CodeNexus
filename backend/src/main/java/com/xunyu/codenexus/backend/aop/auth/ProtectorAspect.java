package com.xunyu.codenexus.backend.aop.auth;

import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.exception.BusinessException;
import com.xunyu.codenexus.backend.mapper.UserMapper;
import com.xunyu.codenexus.backend.model.entity.User;
import com.xunyu.codenexus.backend.model.enums.UserRoleEnum;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 权限校验切面类
 * 拦截所有贴了 @Protector 注解的方法，检查用户权限
 *
 * @author xunyu
 */
@Aspect
@Component
public class ProtectorAspect {

    @Resource
    private UserMapper userMapper;

    @Around("@annotation(protector)")
    public Object doAround(ProceedingJoinPoint joinPoint, Protector protector)
            throws Throwable {

        // 1. 获取当前登录用户 ID
        Long userId = UserContext.getUserId();

        // 如果 ID 为空，说明拦截器放行有问题，或者这就是个未登录接口但加了注解
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 2. 获取注解要求的角色
        UserRoleEnum requiredRole = protector.role();

        // 3. 查询数据库获取真实状态
        // 虽然 Token 里有 role，但为了防止"封号后 Token 仍有效"的情况，这里选择查库
        User user = userMapper.selectById(userId);

        // 用户不存在（可能被物理删除了）
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 4. 校验用户是否被封号
        if (user.getUserRole() == UserRoleEnum.BAN) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "账号已被封禁");
        }

        // 5. 校验权限等级
        // 如果需要 ADMIN，但当前用户不是 ADMIN
        if (requiredRole == UserRoleEnum.ADMIN && user.getUserRole() != UserRoleEnum.ADMIN) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 6. 放行
        return joinPoint.proceed();
    }
}
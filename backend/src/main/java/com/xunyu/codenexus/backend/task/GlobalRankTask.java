// src/main/java/com/xunyu/codenexus/backend/task/GlobalRankTask.java
package com.xunyu.codenexus.backend.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunyu.codenexus.backend.mapper.UserMapper;
import com.xunyu.codenexus.backend.model.entity.User;
import com.xunyu.codenexus.backend.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 全服排名定时刷新任务
 *
 * @author CodeNexusBuilder
 */
@Slf4j
@Component
public class GlobalRankTask {

    @Resource
    private UserMapper userMapper;

    /**
     * 注意：这里注入 UserService 是为了使用它继承自 IService 的 updateBatchById 方法
     * 避免在 for 循环中执行成百上千次单条 update 语句，提升性能。
     */
    @Resource
    private UserService userService;

    /**
     * 核心调度逻辑
     * cron 表达式 "0 0 * * * ?" 表示：每小时的第 0 分 0 秒执行一次 (例如 1:00, 2:00, 3:00...)
     * * [开发阶段技巧] 如果你现在就想看效果，可以把下面的注解改成：
     *
     * @Scheduled(fixedDelay = 60000)  // 意思是每隔 60 秒执行一次，方便联调
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void refreshGlobalRank() {
        log.info("[全服排名任务] 开始执行定时刷新...");
        long startTime = System.currentTimeMillis();

        // 1. 查询所有参与排行的用户，按照战力积分降序，ID升序(防止同分乱序)
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 为了极致性能，仅 select 必要的字段 (ID, 当前排名, 积分)，不加载头像等大文本字段
        queryWrapper.select(User::getId, User::getGlobalRank, User::getRatingScore)
                .orderByDesc(User::getRatingScore)
                .orderByAsc(User::getId);

        List<User> userList = userMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(userList)) {
            log.info("[全服排名任务] 当前无用户数据，跳过执行。");
            return;
        }

        // 2. 遍历并重新计算排名
        List<User> updateList = new ArrayList<>();
        int currentRank = 1;

        for (User user : userList) {
            // 核心优化：如果该用户原本的排名就等于计算出的当前排名，说明名次没变，无需加入更新队列！
            if (user.getGlobalRank() == null || !user.getGlobalRank().equals(currentRank)) {
                User updateUser = new User();
                updateUser.setId(user.getId());
                updateUser.setGlobalRank(currentRank);
                updateList.add(updateUser);
            }
            currentRank++;
        }

        // 3. 批量回写到数据库
        if (!updateList.isEmpty()) {
            // 每次提交 1000 条，防止一次性事务过大导致 MySQL 锁表
            userService.updateBatchById(updateList, 1000);
        }

        log.info("[全服排名任务] 执行完毕！总耗时: {} ms, 实际更新了 {} 名用户的排名。",
                (System.currentTimeMillis() - startTime), updateList.size());
    }
}
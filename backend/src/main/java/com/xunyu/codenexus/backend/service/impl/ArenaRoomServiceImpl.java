package com.xunyu.codenexus.backend.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.exception.BusinessException;
import com.xunyu.codenexus.backend.mapper.ArenaRoomMapper;
import com.xunyu.codenexus.backend.mapper.ArenaRoomUserMapper;
import com.xunyu.codenexus.backend.mapper.ProblemMapper;
import com.xunyu.codenexus.backend.mapper.UserProblemStateMapper;
import com.xunyu.codenexus.backend.model.enums.ArenaRoomStatus;
import com.xunyu.codenexus.backend.model.dto.request.arena.RoomCreateRequest;
import com.xunyu.codenexus.backend.model.dto.response.arena.RoomCreateVO;
import com.xunyu.codenexus.backend.model.dto.response.arena.RoomValidityVO;
import com.xunyu.codenexus.backend.model.entity.ArenaRoom;
import com.xunyu.codenexus.backend.model.entity.ArenaRoomUser;
import com.xunyu.codenexus.backend.model.entity.Problem;
import com.xunyu.codenexus.backend.model.entity.UserProblemState;
import com.xunyu.codenexus.backend.service.ArenaRoomService;
import com.xunyu.codenexus.backend.service.ProblemService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 竞技场房间业务实现类
 *
 * @author CodeNexusBuilder
 */
@Slf4j
@Service
public class ArenaRoomServiceImpl extends ServiceImpl<ArenaRoomMapper, ArenaRoom> implements ArenaRoomService {

    @Resource
    private ArenaRoomUserMapper arenaRoomUserMapper;

    @Resource
    private ProblemService problemService;

    @Resource
    private ProblemMapper problemMapper;

    @Resource
    private UserProblemStateMapper userProblemStateMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoomCreateVO createRoom(RoomCreateRequest request) {
        Long userId = UserContext.getUserId();

        // 1. 生成房间
        ArenaRoom room = new ArenaRoom();
        room.setRoomCode(generateUniqueRoomCode());
        room.setRoomType(request.getRoomType());
        room.setPassword(request.getPassword());
        room.setStatus(ArenaRoomStatus.WAITING.getValue());
        room.setCreatorId(userId);

        // 【新增极客机制】：如果房主没有指定题目，埋下延迟智能抽题的种子
        if (request.getProblemId() == null) {
            // 暂时使用每日练习题作为合法占位符落库
            room.setProblemId(problemService.getDailyPracticeProblem());

            // 在 Redis 埋下一个“等待智能抽题”的标记，2小时过期（与房间可能存在的最大生命周期一致）
            stringRedisTemplate.opsForValue().set("codenexus:arena:smart_pending:" + room.getRoomCode(), "1", 2, TimeUnit.HOURS);
        } else {
            // 如果房主指定了题目，尊重房主选择
            room.setProblemId(request.getProblemId());
        }
        this.save(room);

        // 2. 将房主加入房间关联表
        ArenaRoomUser roomUser = new ArenaRoomUser();
        roomUser.setRoomId(room.getId());
        roomUser.setUserId(userId);
        roomUser.setIsCreator(1);
        roomUser.setStatus("JOINED");
        arenaRoomUserMapper.insert(roomUser);

        RoomCreateVO vo = new RoomCreateVO();
        vo.setRoomCode(room.getRoomCode());
        return vo;
    }

    @Override
    public RoomValidityVO checkRoomValidity(String roomCode, String ticket) {
        Long userId = UserContext.getUserId();

        LambdaQueryWrapper<ArenaRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArenaRoom::getRoomCode, roomCode);
        ArenaRoom room = this.getOne(wrapper);

        AssertUtil.notNull(room, ResultCode.NOT_FOUND, "房间不存在或已被解散");
        AssertUtil.notEquals(room.getStatus(), ArenaRoomStatus.DISMISSED.getValue(), ResultCode.FORBIDDEN, "房间已解散");
        AssertUtil.notEquals(room.getStatus(), ArenaRoomStatus.FINISHED.getValue(), ResultCode.FORBIDDEN, "对局已结束");

        // 物理时间生命周期校验 (2分钟)
        if (ArenaRoomStatus.WAITING.getValue().equals(room.getStatus())) {
            long expireTime = room.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() + (2 * 60 * 1000L);
            if (System.currentTimeMillis() > expireTime) {
                // 房间寿命耗尽，主动标记为解散，彻底封死大门
                room.setStatus(ArenaRoomStatus.DISMISSED.getValue());
                this.updateById(room);
                AssertUtil.isTrue(false, ResultCode.FORBIDDEN, "房间存活时间已到期，已自动解散");
            }
        }

        // 核心验票逻辑
        if (room.getRoomType() == 3) {
            AssertUtil.isTrue(StringUtils.hasText(ticket), ResultCode.FORBIDDEN, "匹配房间需要入场凭证");

            String ticketKey = "codenexus:arena:ticket:" + ticket;
            String ticketValue = stringRedisTemplate.opsForValue().get(ticketKey);

            AssertUtil.isTrue(StringUtils.hasText(ticketValue), ResultCode.FORBIDDEN, "入场凭证无效或已过期");

            String expectedValue = roomCode + ":" + userId;
            AssertUtil.equals(ticketValue, expectedValue, ResultCode.FORBIDDEN, "入场凭证与身份不匹配");

            // 阅后即焚，防止票据重放攻击
            stringRedisTemplate.delete(ticketKey);
        }

        RoomValidityVO vo = new RoomValidityVO();
        vo.setIsValid(true);
        vo.setRoomType(room.getRoomType());
        vo.setStatus(room.getStatus());
        vo.setProblemId(room.getProblemId());
        vo.setMessage("验票成功，准许接入神经网络");
        return vo;
    }

    /**
     * 竞技场智能抽题核心逻辑 (分级漏斗筛选器)
     */
    private Long selectSmartProblem(Long userId1, Long userId2) {
        // 1. 获取全量有效题目 ID 列表
        List<Long> allProblemIds = problemMapper.selectList(
                new LambdaQueryWrapper<Problem>().select(Problem::getId)
        ).stream().map(Problem::getId).toList();

        if (allProblemIds.isEmpty()) {
            throw new BusinessException("题库暂无题目，无法开启竞技");
        }

        // 2. 提取两个人的 AC 记录 (使用 getState 精准对齐实体类)
        Set<Long> acSet1 = userProblemStateMapper.selectList(
                new LambdaQueryWrapper<UserProblemState>()
                        .eq(UserProblemState::getUserId, userId1)
                        .eq(UserProblemState::getState, 2)
        ).stream().map(UserProblemState::getProblemId).collect(Collectors.toSet());

        Set<Long> acSet2 = userProblemStateMapper.selectList(
                new LambdaQueryWrapper<UserProblemState>()
                        .eq(UserProblemState::getUserId, userId2)
                        .eq(UserProblemState::getState, 2)
        ).stream().map(UserProblemState::getProblemId).collect(Collectors.toSet());

        // --- 优先级一：两人均未 AC ---
        List<Long> priority1 = allProblemIds.stream()
                .filter(id -> !acSet1.contains(id) && !acSet2.contains(id))
                .collect(Collectors.toList());
        if (!priority1.isEmpty()) {
            return priority1.get(ThreadLocalRandom.current().nextInt(priority1.size()));
        }

        // --- 优先级二：仅一方未 AC (至少消耗掉一方的新题) ---
        List<Long> priority2 = allProblemIds.stream()
                .filter(id -> !(acSet1.contains(id) && acSet2.contains(id)))
                .collect(Collectors.toList());
        if (!priority2.isEmpty()) {
            return priority2.get(ThreadLocalRandom.current().nextInt(priority2.size()));
        }

        // --- 优先级三：两人均已全 AC，寻找“陈年老题”唤醒记忆 ---
        List<UserProblemState> rustiestRecords = userProblemStateMapper.selectList(
                new LambdaQueryWrapper<UserProblemState>()
                        .in(UserProblemState::getUserId, List.of(userId1, userId2))
                        .in(UserProblemState::getProblemId, allProblemIds)
                        .orderByAsc(UserProblemState::getUpdateTime) // 使用 getUpdateTime，最后更新时间最久远的排前面
                        .last("LIMIT 1")
        );

        if (!rustiestRecords.isEmpty()) {
            return rustiestRecords.get(0).getProblemId();
        }

        // 兜底逻辑：纯随机选一题
        return allProblemIds.get(ThreadLocalRandom.current().nextInt(allProblemIds.size()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createMatchRoom(Long user1Id, Long user2Id) {
        ArenaRoom room = new ArenaRoom();
        room.setRoomCode(generateUniqueRoomCode());
        room.setRoomType(3); // 3 代表天梯匹配模式
        room.setStatus(ArenaRoomStatus.WAITING.getValue());
        room.setCreatorId(user1Id);

        // 匹配模式下同时知晓两人，直接触发智能抽题
        room.setProblemId(selectSmartProblem(user1Id, user2Id));

        this.save(room);

        ArenaRoomUser u1 = new ArenaRoomUser();
        u1.setRoomId(room.getId());
        u1.setUserId(user1Id);
        u1.setIsCreator(1);

        ArenaRoomUser u2 = new ArenaRoomUser();
        u2.setRoomId(room.getId());
        u2.setUserId(user2Id);
        u2.setIsCreator(0);

        arenaRoomUserMapper.insert(u1);
        arenaRoomUserMapper.insert(u2);

        return room.getRoomCode();
    }

    private String generateUniqueRoomCode() {
        String code;
        LambdaQueryWrapper<ArenaRoom> checkWrapper;
        do {
            code = RandomUtil.randomStringUpper(6);
            checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(ArenaRoom::getRoomCode, code);
        } while (this.count(checkWrapper) > 0);
        return code;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean joinPrivateRoom(String roomCode, String password) {
        Long userId = UserContext.getUserId();

        // 1. 查询房间
        LambdaQueryWrapper<ArenaRoom> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(ArenaRoom::getRoomCode, roomCode);
        ArenaRoom room = this.getOne(roomWrapper);

        AssertUtil.notNull(room, ResultCode.NOT_FOUND, "房间不存在");
        AssertUtil.isTrue(room.getRoomType() != 3, ResultCode.FORBIDDEN, "天梯匹配房间禁止主动加入");
        AssertUtil.isTrue(ArenaRoomStatus.WAITING.getValue().equals(room.getStatus()), ResultCode.FORBIDDEN, "房间已在战斗中或已结束");

        // 物理时间生命周期校验 (2分钟)
        long expireTime = room.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() + (2 * 60 * 1000L);
        if (System.currentTimeMillis() > expireTime) {
            room.setStatus(ArenaRoomStatus.DISMISSED.getValue());
            this.updateById(room);
            AssertUtil.isTrue(false, ResultCode.FORBIDDEN, "该房间邀请码已过期失效");
        }

        // 2. 校验密码 (如果有)
        if (StringUtils.hasText(room.getPassword())) {
            AssertUtil.equals(password, room.getPassword(), ResultCode.FORBIDDEN, "房间密码错误");
        }

        // 3. 检查是否已经在房间内 (防止重复加入)
        LambdaQueryWrapper<ArenaRoomUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(ArenaRoomUser::getRoomId, room.getId())
                .eq(ArenaRoomUser::getUserId, userId);
        if (arenaRoomUserMapper.selectCount(userWrapper) > 0) {
            return true; // 已经在里面了，直接放行
        }

        // 4. 检查房间人数
        LambdaQueryWrapper<ArenaRoomUser> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(ArenaRoomUser::getRoomId, room.getId());
        AssertUtil.isTrue(arenaRoomUserMapper.selectCount(countWrapper) < 2, ResultCode.FORBIDDEN, "房间人数已满");

        // 5. 写入关联表
        ArenaRoomUser roomUser = new ArenaRoomUser();
        roomUser.setRoomId(room.getId());
        roomUser.setUserId(userId);
        roomUser.setIsCreator(0); // 客机
        roomUser.setStatus("JOINED");
        arenaRoomUserMapper.insert(roomUser);

        // 👇 【核心增强：延迟智能抽题 (薛定谔的题目)】
        // 客机加入瞬间，凑齐两人，触发量子坍缩，将题目锁定为最适合两人的那一道！
        String pendingKey = "codenexus:arena:smart_pending:" + roomCode;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(pendingKey))) {
            Long smartProblemId = selectSmartProblem(room.getCreatorId(), userId);

            // 悄悄把数据库中房间的题目换成智能抽出来的题
            room.setProblemId(smartProblemId);
            this.updateById(room);

            // 阅后即焚，防止重复触发
            stringRedisTemplate.delete(pendingKey);
            log.info("[私有房间] 客机 {} 加入，智能重抽题目完毕，目标题目 ID: {}", userId, smartProblemId);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveWaitingRoom(String roomCode, Long userId) {
        // 1. 获取房间
        LambdaQueryWrapper<ArenaRoom> roomQw = new LambdaQueryWrapper<>();
        roomQw.eq(ArenaRoom::getRoomCode, roomCode);
        ArenaRoom room = this.getOne(roomQw);

        // 只有等待中的房间允许退出转移
        if (room == null || !ArenaRoomStatus.WAITING.getValue().equals(room.getStatus())) return;

        // 2. 获取玩家记录
        LambdaQueryWrapper<ArenaRoomUser> userQw = new LambdaQueryWrapper<>();
        userQw.eq(ArenaRoomUser::getRoomId, room.getId())
                .eq(ArenaRoomUser::getUserId, userId);
        ArenaRoomUser ru = arenaRoomUserMapper.selectOne(userQw);

        if (ru == null) return;

        // 使用物理删除，彻底从磁盘抹除记录，防止下次进入时触发 DuplicateKeyException
        arenaRoomUserMapper.physicalDeleteById(ru.getId());

        // 4. 【核心】房主转移逻辑
        if (ru.getIsCreator() == 1) {
            // 找找房间里还有没有其他人 (按加入时间排序，最早加入的继承房主)
            LambdaQueryWrapper<ArenaRoomUser> nextQw = new LambdaQueryWrapper<>();
            nextQw.eq(ArenaRoomUser::getRoomId, room.getId())
                    .orderByAsc(ArenaRoomUser::getCreateTime)
                    .last("LIMIT 1");
            ArenaRoomUser nextUser = arenaRoomUserMapper.selectOne(nextQw);

            if (nextUser != null) {
                // 继承房主
                nextUser.setIsCreator(1);
                arenaRoomUserMapper.updateById(nextUser);

                room.setCreatorId(nextUser.getUserId());
                this.updateById(room);
                log.info("[房间管理] 房间 {} 房主已转移给玩家 {}", roomCode, nextUser.getUserId());
            } else {
                // 房间空了，直接解散
                room.setStatus(ArenaRoomStatus.DISMISSED.getValue());
                this.updateById(room);
                // 顺手清理可能存在的薛定谔标记
                stringRedisTemplate.delete("codenexus:arena:smart_pending:" + roomCode);
                log.info("[房间管理] 房间 {} 人去楼空，已解散", roomCode);
            }
        }
    }
}
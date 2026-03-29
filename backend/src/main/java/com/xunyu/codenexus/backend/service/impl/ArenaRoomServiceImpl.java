package com.xunyu.codenexus.backend.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.mapper.ArenaRoomMapper;
import com.xunyu.codenexus.backend.mapper.ArenaRoomUserMapper;
import com.xunyu.codenexus.backend.model.dto.request.arena.RoomCreateRequest;
import com.xunyu.codenexus.backend.model.dto.response.arena.RoomCreateVO;
import com.xunyu.codenexus.backend.model.dto.response.arena.RoomValidityVO;
import com.xunyu.codenexus.backend.model.entity.ArenaRoom;
import com.xunyu.codenexus.backend.model.entity.ArenaRoomUser;
import com.xunyu.codenexus.backend.service.ArenaRoomService;
import com.xunyu.codenexus.backend.service.ProblemService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
        room.setStatus("WAITING");
        room.setCreatorId(userId);

        if (request.getProblemId() == null) {
            room.setProblemId(problemService.getDailyPracticeProblem());
        } else {
            room.setProblemId(request.getProblemId());
        }
        this.save(room);

        // 2. 将房主加入房间关联表
        ArenaRoomUser roomUser = new ArenaRoomUser();
        roomUser.setRoomId(room.getId());
        roomUser.setUserId(userId);
        roomUser.setIsCreator(1);
        roomUser.setStatus("JOINED");
        arenaRoomUserMapper.insert(roomUser); // 如果IDEA依然报红，可无视，它是BaseMapper自带的方法

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
        AssertUtil.notEquals(room.getStatus(), "DISMISSED", ResultCode.FORBIDDEN, "房间已解散");
        AssertUtil.notEquals(room.getStatus(), "FINISHED", ResultCode.FORBIDDEN, "对局已结束");

        // 【新增：物理时间生命周期校验 (2分钟)】
        if ("WAITING".equals(room.getStatus())) {
            long expireTime = room.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() + (2 * 60 * 1000L);
            if (System.currentTimeMillis() > expireTime) {
                // 房间寿命耗尽，主动标记为解散，彻底封死大门
                room.setStatus("DISMISSED");
                this.updateById(room);
                AssertUtil.isTrue(false, ResultCode.FORBIDDEN, "房间存活时间已到期，已自动解散");
            }
        }

        // 核心验票逻辑
        if (room.getRoomType() == 3) {
            // 修复：使用 isTrue 替代 notEmpty 来实现带 ResultCode 的断言
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createMatchRoom(Long user1Id, Long user2Id) {
        ArenaRoom room = new ArenaRoom();
        room.setRoomCode(generateUniqueRoomCode());
        room.setRoomType(3);
        room.setStatus("WAITING");
        room.setCreatorId(user1Id);
        room.setProblemId(1L); // TODO: 后续接入真实随机抽题

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
        AssertUtil.isTrue("WAITING".equals(room.getStatus()), ResultCode.FORBIDDEN, "房间已在战斗中或已结束");

        // 【新增：物理时间生命周期校验 (2分钟)】
        long expireTime = room.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() + (2 * 60 * 1000L);
        if (System.currentTimeMillis() > expireTime) {
            room.setStatus("DISMISSED");
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

        // 4. 检查房间人数 (竞技场设定为 2 人对战)
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
        if (room == null || !"WAITING".equals(room.getStatus())) return;

        // 2. 获取玩家记录
        LambdaQueryWrapper<ArenaRoomUser> userQw = new LambdaQueryWrapper<>();
        userQw.eq(ArenaRoomUser::getRoomId, room.getId())
                .eq(ArenaRoomUser::getUserId, userId);
        ArenaRoomUser ru = arenaRoomUserMapper.selectOne(userQw);

        if (ru == null) return;

        // 【修改这里】使用物理删除，彻底从磁盘抹除记录，防止下次进入时触发 DuplicateKeyException
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
                room.setStatus("DISMISSED");
                this.updateById(room);
                log.info("[房间管理] 房间 {} 人去楼空，已解散", roomCode);
            }
        }
    }
}
// src/main/java/com/xunyu/codenexus/backend/service/impl/FavoriteServiceImpl.java
package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.mapper.FavoriteFolderMapper;
import com.xunyu.codenexus.backend.mapper.FavoriteItemMapper;
import com.xunyu.codenexus.backend.mapper.ProblemMapper;
import com.xunyu.codenexus.backend.mapper.UserProblemStateMapper;
import com.xunyu.codenexus.backend.model.dto.request.BaseQueryRequest;
import com.xunyu.codenexus.backend.model.dto.request.favorite.FavoriteAddRequest;
import com.xunyu.codenexus.backend.model.dto.request.favorite.FavoriteFolderAddRequest;
import com.xunyu.codenexus.backend.model.dto.request.favorite.FavoriteRemoveRequest;
import com.xunyu.codenexus.backend.model.dto.response.favorite.FavoriteFolderVO;
import com.xunyu.codenexus.backend.model.dto.response.favorite.FavoriteProblemVO;
import com.xunyu.codenexus.backend.model.entity.FavoriteFolder;
import com.xunyu.codenexus.backend.model.entity.FavoriteItem;
import com.xunyu.codenexus.backend.model.entity.Problem;
import com.xunyu.codenexus.backend.model.entity.UserProblemState;
import com.xunyu.codenexus.backend.service.FavoriteService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 我的收藏业务实现类
 *
 * @author CodeNexusBuilder
 */
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteFolderMapper, FavoriteFolder> implements FavoriteService {

    @Resource
    private FavoriteItemMapper favoriteItemMapper;

    @Resource
    private ProblemMapper problemMapper;

    @Resource
    private UserProblemStateMapper userProblemStateMapper;

    @Override
    public List<FavoriteFolderVO> listMyFolders() {
        Long userId = UserContext.getUserId();

        // 1. 查询用户的收藏夹列表
        LambdaQueryWrapper<FavoriteFolder> folderWrapper = new LambdaQueryWrapper<>();
        folderWrapper.eq(FavoriteFolder::getUserId, userId)
                .orderByDesc(FavoriteFolder::getCreateTime);
        List<FavoriteFolder> folders = this.list(folderWrapper);

        if (CollectionUtils.isEmpty(folders)) {
            return new ArrayList<>();
        }

        // 2. 批量统计每个文件夹的题目数量 (避免 N+1 查询)
        List<Long> folderIds = folders.stream().map(FavoriteFolder::getId).collect(Collectors.toList());
        QueryWrapper<FavoriteItem> countWrapper = new QueryWrapper<>();
        countWrapper.select("folder_id", "count(*) as count")
                .in("folder_id", folderIds)
                .groupBy("folder_id");

        List<Map<String, Object>> counts = favoriteItemMapper.selectMaps(countWrapper);
        Map<Long, Integer> countMap = counts.stream().collect(Collectors.toMap(
                map -> ((Number) map.get("folder_id")).longValue(),
                map -> ((Number) map.get("count")).intValue()
        ));

        // 3. 组装 VO 返回
        return folders.stream().map(folder -> {
            FavoriteFolderVO vo = new FavoriteFolderVO();
            BeanUtils.copyProperties(folder, vo);
            vo.setProblemCount(countMap.getOrDefault(folder.getId(), 0));
            // 默认填充为非默认收藏夹 (前端需求：0为自定义可删除)
            vo.setIsDefault(0);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Long createFolder(FavoriteFolderAddRequest request) {
        FavoriteFolder folder = new FavoriteFolder();
        BeanUtils.copyProperties(request, folder);
        folder.setUserId(UserContext.getUserId());
        this.save(folder);
        return folder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFolder(Long folderId) {
        Long userId = UserContext.getUserId();
        FavoriteFolder folder = this.getById(folderId);

        AssertUtil.notNull(folder, ResultCode.NOT_FOUND, "收藏夹不存在");
        AssertUtil.equals(folder.getUserId(), userId, ResultCode.FORBIDDEN, "无权操作他人的收藏夹");

        // 1. 删除收藏夹本体
        this.removeById(folderId);

        // 2. 级联删除该收藏夹下的所有题目记录
        LambdaQueryWrapper<FavoriteItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(FavoriteItem::getFolderId, folderId);
        favoriteItemMapper.delete(itemWrapper);

        return true;
    }

    @Override
    public Page<FavoriteProblemVO> getProblemsInFolder(Long folderId, BaseQueryRequest request) {
        Long userId = UserContext.getUserId();
        FavoriteFolder folder = this.getById(folderId);
        AssertUtil.notNull(folder, ResultCode.NOT_FOUND, "收藏夹不存在");

        if (folder.getIsPublic() == 0) {
            AssertUtil.equals(folder.getUserId(), userId, ResultCode.FORBIDDEN, "无权查看该私有收藏夹");
        }

        // 1. 分页查询 favorite_item，按收藏时间倒序
        Page<FavoriteItem> itemPageParam = new Page<>(request.getCurrent(), request.getPageSize());
        LambdaQueryWrapper<FavoriteItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(FavoriteItem::getFolderId, folderId)
                .orderByDesc(FavoriteItem::getCreateTime);
        Page<FavoriteItem> itemPage = favoriteItemMapper.selectPage(itemPageParam, itemWrapper);

        if (CollectionUtils.isEmpty(itemPage.getRecords())) {
            return new Page<>(itemPage.getCurrent(), itemPage.getSize(), itemPage.getTotal());
        }

        // 2. 提取 problemIds，批量查询 Problem 基础表
        List<Long> problemIds = itemPage.getRecords().stream()
                .map(FavoriteItem::getProblemId)
                .collect(Collectors.toList());

        List<Problem> problems = problemMapper.selectBatchIds(problemIds);
        Map<Long, Problem> problemMap = problems.stream()
                .collect(Collectors.toMap(Problem::getId, p -> p));

        // 3. 批量查询 UserProblemState 获取当前用户的解答状态 (避免 N+1)
        Map<Long, Integer> userStateMap = new HashMap<>();
        if (userId != null && !problemIds.isEmpty()) {
            LambdaQueryWrapper<UserProblemState> stateWrapper = new LambdaQueryWrapper<>();
            stateWrapper.eq(UserProblemState::getUserId, userId)
                    .in(UserProblemState::getProblemId, problemIds);
            List<UserProblemState> states = userProblemStateMapper.selectList(stateWrapper);
            userStateMap = states.stream().collect(Collectors.toMap(
                    UserProblemState::getProblemId,
                    UserProblemState::getState
            ));
        }

        // 4. 内存组装 VO
        List<FavoriteProblemVO> voList = new ArrayList<>();
        for (FavoriteItem item : itemPage.getRecords()) {
            Problem problem = problemMap.get(item.getProblemId());
            if (problem != null) {
                FavoriteProblemVO vo = new FavoriteProblemVO();
                vo.setId(problem.getId());
                vo.setProblemId(problem.getId());
                // 将真实的物理 ID 作为前端的展示 displayId
                vo.setDisplayId(String.valueOf(problem.getId()));
                vo.setTitle(problem.getTitle());
                vo.setDifficulty(problem.getDifficulty());

                // Mybatis-Plus TypeHandler 已自动将数据库 JSON 转化为 List<String>
                vo.setTags(problem.getTags());

                // 计算通过率并转换为百分比格式 (例如 45.5)
                if (problem.getSubmitNum() == null || problem.getSubmitNum() == 0) {
                    vo.setPassRate(0.0);
                } else {
                    double rate = (double) problem.getAcceptedNum() / problem.getSubmitNum() * 100;
                    vo.setPassRate(Math.round(rate * 10.0) / 10.0); // 保留一位小数
                }

                // 映射用户答题状态给前端通用组件
                Integer state = userStateMap.getOrDefault(problem.getId(), 0);
                if (state == 2) {
                    vo.setStatus("PASSED");
                } else if (state == 1) {
                    vo.setStatus("ATTEMPTED");
                } else {
                    vo.setStatus(null); // 未尝试设为空
                }

                // 放入该题目被添加到本收藏夹的具体时间
                vo.setAddTime(item.getCreateTime());
                voList.add(vo);
            }
        }

        Page<FavoriteProblemVO> voPage = new Page<>(itemPage.getCurrent(), itemPage.getSize(), itemPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public boolean addFavorite(FavoriteAddRequest request) {
        Long userId = UserContext.getUserId();

        FavoriteFolder folder = this.getById(request.getFolderId());
        AssertUtil.notNull(folder, ResultCode.NOT_FOUND, "收藏夹不存在");
        AssertUtil.equals(folder.getUserId(), userId, ResultCode.FORBIDDEN, "无权操作他人的收藏夹");

        LambdaQueryWrapper<FavoriteItem> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(FavoriteItem::getFolderId, request.getFolderId())
                .eq(FavoriteItem::getProblemId, request.getProblemId());
        boolean exists = favoriteItemMapper.exists(checkWrapper);
        AssertUtil.isFalse(exists, "该题目已在收藏夹中，请勿重复添加");

        FavoriteItem item = new FavoriteItem();
        item.setUserId(userId);
        item.setFolderId(request.getFolderId());
        item.setProblemId(request.getProblemId());

        favoriteItemMapper.upsertFavoriteItem(item);

        return true;
    }

    @Override
    public boolean removeFavorite(FavoriteRemoveRequest request) {
        Long userId = UserContext.getUserId();

        FavoriteFolder folder = this.getById(request.getFolderId());
        AssertUtil.notNull(folder, ResultCode.NOT_FOUND, "收藏夹不存在");
        AssertUtil.equals(folder.getUserId(), userId, ResultCode.FORBIDDEN, "无权操作他人的收藏夹");

        LambdaQueryWrapper<FavoriteItem> removeWrapper = new LambdaQueryWrapper<>();
        removeWrapper.eq(FavoriteItem::getFolderId, request.getFolderId())
                .eq(FavoriteItem::getProblemId, request.getProblemId());
        favoriteItemMapper.delete(removeWrapper);

        return true;
    }

    @Override
    public FavoriteFolderVO getFolderDetail(Long folderId) {
        Long userId = UserContext.getUserId();

        FavoriteFolder folder = this.getById(folderId);
        AssertUtil.notNull(folder, ResultCode.NOT_FOUND, "收藏夹不存在");

        if (folder.getIsPublic() == 0) {
            AssertUtil.equals(folder.getUserId(), userId, ResultCode.FORBIDDEN, "无权查看该私有收藏夹");
        }

        LambdaQueryWrapper<FavoriteItem> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(FavoriteItem::getFolderId, folderId);
        long count = favoriteItemMapper.selectCount(countWrapper);

        FavoriteFolderVO vo = new FavoriteFolderVO();
        BeanUtils.copyProperties(folder, vo);
        vo.setProblemCount((int) count);
        // 动态填充 isDefault 为 0
        vo.setIsDefault(0);

        return vo;
    }
}
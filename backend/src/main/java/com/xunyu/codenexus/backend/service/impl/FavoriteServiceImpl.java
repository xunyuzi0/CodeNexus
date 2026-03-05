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
import com.xunyu.codenexus.backend.model.dto.request.BaseQueryRequest;
import com.xunyu.codenexus.backend.model.dto.request.favorite.FavoriteAddRequest;
import com.xunyu.codenexus.backend.model.dto.request.favorite.FavoriteFolderAddRequest;
import com.xunyu.codenexus.backend.model.dto.request.favorite.FavoriteRemoveRequest;
import com.xunyu.codenexus.backend.model.dto.response.favorite.FavoriteFolderVO;
import com.xunyu.codenexus.backend.model.dto.response.favorite.FavoriteProblemVO;
import com.xunyu.codenexus.backend.model.entity.FavoriteFolder;
import com.xunyu.codenexus.backend.model.entity.FavoriteItem;
import com.xunyu.codenexus.backend.model.entity.Problem;
import com.xunyu.codenexus.backend.service.FavoriteService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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

        // 鉴权：如果是私有文件夹，必须是本人的
        if (folder.getIsPublic() == 0) {
            AssertUtil.equals(folder.getUserId(), userId, ResultCode.FORBIDDEN, "无权查看该私有收藏夹");
        }

        // 1. 分页查询 favorite_item
        Page<FavoriteItem> itemPageParam = new Page<>(request.getCurrent(), request.getPageSize());
        LambdaQueryWrapper<FavoriteItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(FavoriteItem::getFolderId, folderId)
                .orderByDesc(FavoriteItem::getCreateTime); // 按收藏时间倒序
        Page<FavoriteItem> itemPage = favoriteItemMapper.selectPage(itemPageParam, itemWrapper);

        if (CollectionUtils.isEmpty(itemPage.getRecords())) {
            return new Page<>(itemPage.getCurrent(), itemPage.getSize(), itemPage.getTotal());
        }

        // 2. 提取 problemIds，批量查询 Problem 表
        List<Long> problemIds = itemPage.getRecords().stream()
                .map(FavoriteItem::getProblemId)
                .collect(Collectors.toList());

        List<Problem> problems = problemMapper.selectBatchIds(problemIds);
        Map<Long, Problem> problemMap = problems.stream()
                .collect(Collectors.toMap(Problem::getId, p -> p));

        // 3. 内存组装 VO
        List<FavoriteProblemVO> voList = new ArrayList<>();
        for (FavoriteItem item : itemPage.getRecords()) {
            Problem problem = problemMap.get(item.getProblemId());
            if (problem != null) {
                FavoriteProblemVO vo = new FavoriteProblemVO();
                vo.setProblemId(problem.getId());
                vo.setTitle(problem.getTitle());
                vo.setDifficulty(problem.getDifficulty());

                // 计算通过率
                if (problem.getSubmitNum() == null || problem.getSubmitNum() == 0) {
                    vo.setPassRate(0.0);
                } else {
                    vo.setPassRate((double) problem.getAcceptedNum() / problem.getSubmitNum());
                }

                // 放入收藏时间
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

        // 校验文件夹归属
        FavoriteFolder folder = this.getById(request.getFolderId());
        AssertUtil.notNull(folder, ResultCode.NOT_FOUND, "收藏夹不存在");
        AssertUtil.equals(folder.getUserId(), userId, ResultCode.FORBIDDEN, "无权操作他人的收藏夹");

        // 校验是否重复收藏
        LambdaQueryWrapper<FavoriteItem> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(FavoriteItem::getFolderId, request.getFolderId())
                .eq(FavoriteItem::getProblemId, request.getProblemId());
        boolean exists = favoriteItemMapper.exists(checkWrapper);
        AssertUtil.isFalse(exists, "该题目已在收藏夹中，请勿重复添加");

        // 插入记录
        FavoriteItem item = new FavoriteItem();
        item.setUserId(userId); // 冗余字段
        item.setFolderId(request.getFolderId());
        item.setProblemId(request.getProblemId());
        favoriteItemMapper.insert(item);

        return true;
    }

    @Override
    public boolean removeFavorite(FavoriteRemoveRequest request) {
        Long userId = UserContext.getUserId();

        // 校验文件夹归属
        FavoriteFolder folder = this.getById(request.getFolderId());
        AssertUtil.notNull(folder, ResultCode.NOT_FOUND, "收藏夹不存在");
        AssertUtil.equals(folder.getUserId(), userId, ResultCode.FORBIDDEN, "无权操作他人的收藏夹");

        // 执行删除
        LambdaQueryWrapper<FavoriteItem> removeWrapper = new LambdaQueryWrapper<>();
        removeWrapper.eq(FavoriteItem::getFolderId, request.getFolderId())
                .eq(FavoriteItem::getProblemId, request.getProblemId());
        favoriteItemMapper.delete(removeWrapper);

        return true;
    }

    @Override
    public FavoriteFolderVO getFolderDetail(Long folderId) {
        Long userId = UserContext.getUserId();

        // 1. 查询收藏夹本体
        FavoriteFolder folder = this.getById(folderId);
        AssertUtil.notNull(folder, ResultCode.NOT_FOUND, "收藏夹不存在");

        // 2. 越权校验：如果是私有文件夹，只有创建者本人可以查看
        if (folder.getIsPublic() == 0) {
            AssertUtil.equals(folder.getUserId(), userId, ResultCode.FORBIDDEN, "无权查看该私有收藏夹");
        }

        // 3. 统计该收藏夹下的题目总数
        LambdaQueryWrapper<FavoriteItem> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(FavoriteItem::getFolderId, folderId);
        long count = favoriteItemMapper.selectCount(countWrapper);

        // 4. 组装 VO 返回
        FavoriteFolderVO vo = new FavoriteFolderVO();
        BeanUtils.copyProperties(folder, vo);
        vo.setProblemCount((int) count);

        return vo;
    }
}
// src/main/java/com/xunyu/codenexus/backend/service/FavoriteService.java
package com.xunyu.codenexus.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xunyu.codenexus.backend.model.dto.request.BaseQueryRequest;
import com.xunyu.codenexus.backend.model.dto.request.favorite.FavoriteAddRequest;
import com.xunyu.codenexus.backend.model.dto.request.favorite.FavoriteFolderAddRequest;
import com.xunyu.codenexus.backend.model.dto.request.favorite.FavoriteRemoveRequest;
import com.xunyu.codenexus.backend.model.dto.response.favorite.FavoriteFolderVO;
import com.xunyu.codenexus.backend.model.dto.response.favorite.FavoriteProblemVO;
import com.xunyu.codenexus.backend.model.entity.FavoriteFolder;

import java.util.List;

/**
 * 我的收藏业务接口
 *
 * @author CodeNexusBuilder
 */
public interface FavoriteService extends IService<FavoriteFolder> {

    /**
     * 获取当前登录用户的收藏夹列表 (包含题目数量统计)
     */
    List<FavoriteFolderVO> listMyFolders();

    /**
     * 创建一个新的收藏夹
     */
    Long createFolder(FavoriteFolderAddRequest request);

    /**
     * 删除一个收藏夹 (级联删除其中的题目)
     */
    boolean deleteFolder(Long folderId);

    /**
     * 分页获取指定收藏夹下的题目列表
     */
    Page<FavoriteProblemVO> getProblemsInFolder(Long folderId, BaseQueryRequest request);

    /**
     * 将某道题加入指定收藏夹
     */
    boolean addFavorite(FavoriteAddRequest request);

    /**
     * 将某道题从指定收藏夹移除
     */
    boolean removeFavorite(FavoriteRemoveRequest request);

    /**
     * 获取单个收藏夹的详细元数据
     *
     * @param folderId 收藏夹ID
     * @return 收藏夹详情VO
     */
    FavoriteFolderVO getFolderDetail(Long folderId);
}
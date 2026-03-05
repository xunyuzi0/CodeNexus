// src/main/java/com/xunyu/codenexus/backend/controller/FavoriteController.java
package com.xunyu.codenexus.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunyu.codenexus.backend.aop.auth.Protector;
import com.xunyu.codenexus.backend.common.result.Result;
import com.xunyu.codenexus.backend.model.dto.request.BaseQueryRequest;
import com.xunyu.codenexus.backend.model.dto.request.favorite.FavoriteAddRequest;
import com.xunyu.codenexus.backend.model.dto.request.favorite.FavoriteFolderAddRequest;
import com.xunyu.codenexus.backend.model.dto.request.favorite.FavoriteRemoveRequest;
import com.xunyu.codenexus.backend.model.dto.response.favorite.FavoriteFolderVO;
import com.xunyu.codenexus.backend.model.dto.response.favorite.FavoriteProblemVO;
import com.xunyu.codenexus.backend.model.enums.UserRoleEnum;
import com.xunyu.codenexus.backend.service.FavoriteService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 我的收藏模块接口控制器
 *
 * @author CodeNexusBuilder
 */
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Resource
    private FavoriteService favoriteService;

    /**
     * 获取当前登录用户的收藏夹列表
     */
    @GetMapping("/folders")
    @Protector(role = UserRoleEnum.USER)
    public Result<List<FavoriteFolderVO>> listMyFolders() {
        List<FavoriteFolderVO> list = favoriteService.listMyFolders();
        return Result.success(list);
    }

    /**
     * 创建一个新的收藏夹
     */
    @PostMapping("/folders")
    @Protector(role = UserRoleEnum.USER)
    public Result<Long> createFolder(@RequestBody @Validated FavoriteFolderAddRequest request) {
        Long folderId = favoriteService.createFolder(request);
        return Result.success(folderId);
    }

    /**
     * 删除一个收藏夹
     */
    @DeleteMapping("/folders/{folderId}")
    @Protector(role = UserRoleEnum.USER)
    public Result<Boolean> deleteFolder(@PathVariable("folderId") Long folderId) {
        boolean result = favoriteService.deleteFolder(folderId);
        return Result.success(result);
    }

    /**
     * 获取指定收藏夹下的题目列表
     */
    @GetMapping("/folders/{folderId}/problems")
    @Protector(role = UserRoleEnum.USER)
    public Result<Page<FavoriteProblemVO>> getProblemsInFolder(
            @PathVariable("folderId") Long folderId,
            BaseQueryRequest request) {
        Page<FavoriteProblemVO> page = favoriteService.getProblemsInFolder(folderId, request);
        return Result.success(page);
    }

    /**
     * 将某道题加入指定收藏夹
     */
    @PostMapping("/add")
    @Protector(role = UserRoleEnum.USER)
    public Result<Boolean> addFavorite(@RequestBody @Validated FavoriteAddRequest request) {
        boolean result = favoriteService.addFavorite(request);
        return Result.success(result);
    }

    /**
     * 将某道题从指定收藏夹移除
     */
    @PostMapping("/remove")
    @Protector(role = UserRoleEnum.USER)
    public Result<Boolean> removeFavorite(@RequestBody @Validated FavoriteRemoveRequest request) {
        boolean result = favoriteService.removeFavorite(request);
        return Result.success(result);
    }

    /**
     * 获取单个收藏夹元数据 (详情页顶部信息)
     */
    @GetMapping("/folders/{folderId}")
    @Protector(role = UserRoleEnum.USER)
    public Result<FavoriteFolderVO> getFolderDetail(@PathVariable("folderId") Long folderId) {
        FavoriteFolderVO detail = favoriteService.getFolderDetail(folderId);
        return Result.success(detail);
    }
}
/**
 * src/api/favorites.ts
 * 收藏夹模块 API 定义
 * -----------------
 * 已根据后端 FavoriteController 真实接口规范全面重构
 */

import request from '@/utils/request'
import type { ProblemDifficulty } from './problem'

export interface FavoriteFolder {
  id: number
  name: string
  count: number
  createTime: string
  isDefault: boolean
}

export interface FavoriteProblem {
  id: number
  displayId: string
  title: string
  difficulty: ProblemDifficulty
  tags: string[]
  joinTime: string
}

const mapDifficultyToFront = (diff: number): ProblemDifficulty => {
  if (diff === 1) return 'EASY'
  if (diff === 2) return 'MEDIUM'
  return 'HARD'
}

// 获取当前登录用户的收藏夹列表
export async function getFolders(): Promise<FavoriteFolder[]> {
  const res = await request<any[]>({
    url: '/api/favorites/folders',
    method: 'GET',
  })

  return (res || []).map((item) => ({
    id: item.id,
    name: item.name,
    count: item.problemCount || 0,
    createTime: item.createTime,
    isDefault: item.isDefault === 1 || item.isDefault === true,
  }))
}

// 创建一个新的收藏夹 (后端返回 Long 类型的 ID)
export async function createFolder(name: string): Promise<FavoriteFolder> {
  const folderId = await request<number>({
    url: '/api/favorites/folders',
    method: 'POST',
    data: { name }, // 对应后端的 FavoriteFolderAddRequest
  })

  // 后端只返回了 ID，前端自行拼装完整的 Folder 对象用于 UI 渲染
  const today = new Date().toISOString().split('T')[0]
  return {
    id: folderId,
    name: name,
    count: 0,
    createTime: today,
    isDefault: false,
  }
}

// 删除一个收藏夹
export async function deleteFolder(id: number): Promise<void> {
  await request({
    url: `/api/favorites/folders/${id}`,
    method: 'DELETE',
  })
}

// 获取单个收藏夹元数据及题目列表
export async function getFolderDetail(
  id: number,
): Promise<{ folder: FavoriteFolder; list: FavoriteProblem[] }> {
  const [folderRes, problemsRes] = await Promise.all([
    request<any>({ url: `/api/favorites/folders/${id}`, method: 'GET' }),
    request<any>({
      url: `/api/favorites/folders/${id}/problems`,
      method: 'GET',
      params: { current: 1, pageSize: 100 }, // 对应后端的 BaseQueryRequest
    }),
  ])

  const folder: FavoriteFolder = folderRes
    ? {
        id: folderRes.id,
        name: folderRes.name,
        count: folderRes.problemCount || 0,
        createTime: folderRes.createTime,
        isDefault: folderRes.isDefault === 1 || folderRes.isDefault === true,
      }
    : { id, name: '未知收藏夹', count: 0, createTime: '', isDefault: false }

  const list: FavoriteProblem[] = (problemsRes?.records || []).map((item: any) => ({
    id: item.problemId || item.id,
    displayId: item.displayId || `P-${item.id}`,
    title: item.title,
    difficulty: mapDifficultyToFront(item.difficulty),
    tags: typeof item.tags === 'string' ? JSON.parse(item.tags) : item.tags || [],
    joinTime: item.addTime || item.createTime,
  }))

  return { folder, list }
}

// 将某道题加入指定收藏夹
export async function addFavoriteProblem(folderId: number, problemId: number): Promise<void> {
  await request({
    url: '/api/favorites/add',
    method: 'POST',
    data: { folderId, problemId }, // 对应后端的 FavoriteAddRequest
  })
}

// 将某道题从指定收藏夹移除
export async function removeFavoriteProblem(folderId: number, problemId: number): Promise<void> {
  await request({
    url: '/api/favorites/remove',
    method: 'POST',
    data: { folderId, problemId }, // 对应后端的 FavoriteRemoveRequest
  })
}

// 修改收藏夹名称
export async function updateFolderName(folderId: number, name: string): Promise<void> {
  await request({
    url: `/api/favorites/folders/${folderId}/name`,
    method: 'PUT',
    data: { name },
  })
}

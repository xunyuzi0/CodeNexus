/**
 * src/api/favorites.ts
 * 收藏夹模块 API 定义
 * -----------------
 * 基于文件夹的收藏管理系统
 */

import type { ProblemDifficulty } from './problem'

export interface FavoriteFolder {
  id: number
  name: string
  count: number
  createTime: string
  isDefault: boolean // 是否为默认收藏夹 (不可删除)
}

export interface FavoriteProblem {
  id: number
  displayId: string
  title: string
  difficulty: ProblemDifficulty
  tags: string[]
  joinTime: string
}

// --- Mock Data ---

let MOCK_FOLDERS: FavoriteFolder[] = [
  {
    id: 1,
    name: '默认收藏夹',
    count: 12,
    createTime: '2023-01-01',
    isDefault: true,
  },
  {
    id: 2,
    name: '动态规划精选',
    count: 5,
    createTime: '2023-10-15',
    isDefault: false,
  },
  {
    id: 3,
    name: '大厂高频题 Top100',
    count: 8,
    createTime: '2023-11-20',
    isDefault: false,
  },
]

const MOCK_PROBLEMS: FavoriteProblem[] = [
  {
    id: 101,
    displayId: '1001',
    title: '爬楼梯 (Climbing Stairs)',
    difficulty: 'EASY',
    tags: ['动态规划', '记忆化'],
    joinTime: '2023-10-16',
  },
  {
    id: 102,
    displayId: '1049',
    title: '最后一块石头的重量 II',
    difficulty: 'MEDIUM',
    tags: ['动态规划', '背包问题'],
    joinTime: '2023-10-16',
  },
  {
    id: 103,
    displayId: '1143',
    title: '最长公共子序列',
    difficulty: 'MEDIUM',
    tags: ['动态规划', '字符串'],
    joinTime: '2023-10-17',
  },
  {
    id: 104,
    displayId: '1072',
    title: '编辑距离',
    difficulty: 'HARD',
    tags: ['动态规划', '字符串'],
    joinTime: '2023-10-18',
  },
]

// --- API Functions ---

/** 获取收藏夹列表 */
export function getFolders(): Promise<FavoriteFolder[]> {
  return new Promise((resolve) => {
    setTimeout(() => resolve([...MOCK_FOLDERS]), 400)
  })
}

/** 新建收藏夹 */
export function createFolder(name: string): Promise<FavoriteFolder> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const newFolder: FavoriteFolder = {
        id: Date.now(),
        name,
        count: 0,
        createTime: new Date().toISOString().split('T')[0],
        isDefault: false,
      }
      MOCK_FOLDERS.push(newFolder)
      resolve(newFolder)
    }, 500)
  })
}

/** 删除收藏夹 */
export function deleteFolder(id: number): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(() => {
      MOCK_FOLDERS = MOCK_FOLDERS.filter((f) => f.id !== id)
      resolve()
    }, 400)
  })
}

/** 获取特定收藏夹详情 (包含题目列表) */
export function getFolderDetail(
  id: number,
): Promise<{ folder: FavoriteFolder; list: FavoriteProblem[] }> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const folder = MOCK_FOLDERS.find((f) => f.id === Number(id)) || MOCK_FOLDERS[0]
      // 简单模拟：如果是默认文件夹返回空，如果是动态规划返回特定列表，其他随机
      const list = id === 2 ? [...MOCK_PROBLEMS] : []
      resolve({ folder, list })
    }, 400)
  })
}

/** 移除收藏题目 */
export function removeFavoriteProblem(folderId: number, problemId: number): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(() => resolve(), 300)
  })
}

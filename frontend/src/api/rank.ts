/**
 * src/api/rank.ts
 * 竞技场排行榜 API 接口
 */
import { request } from '@/utils/request'
import type { PageResult } from '@/types/api'

// 1. 对应后端的排行榜用户实体
export interface RankUser {
  id: number
  name: string
  avatar: string
  score: number
  winRate: number
  tier: string
  rank?: number // 后端有时会直出排名
}

// 2. 个人排名数据实体
export interface MyRankData {
  rank: number
  score: number
  tier: string
  winRate: number
}

/**
 * 获取排行榜分页列表 (公开接口)
 * @param current 当前页
 * @param pageSize 页大小
 */
export function getLeaderboard(current = 1, pageSize = 50) {
  // 注意：后端通常返回 Page 结构，前端类型定义为 PageResult<RankUser>
  return request<PageResult<RankUser> | any>({
    url: '/api/rank/leaderboard',
    method: 'GET',
    params: { current, pageSize },
  })
}

/**
 * 获取当前登录用户的排名信息 (需 Token)
 */
export function getMyRank() {
  return request<MyRankData>({
    url: '/api/rank/me',
    method: 'GET',
  })
}

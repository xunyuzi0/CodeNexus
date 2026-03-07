/**
 * src/api/dashboard.ts
 * 仪表盘模块 API 定义
 */
import request from '@/utils/request'

export interface DashboardStatsVO {
  isCheckedInToday: boolean
  continuousCheckInDays: number
  rankScore: number
  weeklyScoreChange: number
  globalRank: number
  solvedCount: number
  totalProblems: number
}

export interface HeatmapItemVO {
  date: string
  level: number
  submissionCount?: number
}

// 1. 获取仪表盘核心指标
export const getDashboardStats = (): Promise<DashboardStatsVO> => {
  return request({ url: '/api/dashboard/stats', method: 'GET' })
}

// 2. 每日充能打卡
export const dailyCheckIn = (): Promise<{
  success: boolean
  continuousCheckInDays: number
  reward: string
}> => {
  return request({ url: '/api/dashboard/checkin', method: 'POST' })
}

// 3. 获取年度活跃度热力图
export const getActivityHeatmap = (year: number): Promise<HeatmapItemVO[]> => {
  return request({ url: '/api/dashboard/heatmap', method: 'GET', params: { year } })
}

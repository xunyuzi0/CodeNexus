/**
 * src/api/arena.ts
 * 竞技场模块 API 定义
 * -----------------
 */
import { request } from '@/utils/request'

// ==========================================
// 模块一：房间管理 (Room Management)
// ==========================================

export interface CreateRoomParams {
  roomType: number // 1-私人邀请, 2-公开大厅
  password?: string
  problemId?: number | null
  type?: number // [兼容补丁]: 防止后端实体类实际叫 type
}

export interface JoinRoomParams {
  password?: string
}

export interface RoomValidityResponse {
  isValid: boolean
  roomType: number
  status: 'WAITING' | 'BATTLING' | 'FINISHED' | 'DISMISSED'
  problemId: number
  message: string
}

/**
 * 创建私人/公开房间
 */
export function createRoom(data: CreateRoomParams) {
  return request<{ roomCode: string }>({
    url: '/api/arena/room',
    method: 'POST',
    data,
  })
}

/**
 * [新增]: 加入私人房间
 * 前端先调此接口落盘，成功后再跳转大厅
 */
export function joinRoom(roomCode: string, data?: JoinRoomParams) {
  return request<boolean>({
    url: `/api/arena/room/${roomCode}/join`,
    method: 'POST',
    data: data || {},
  })
}

/**
 * 检查房间有效性与验票前置守卫
 */
export function checkRoomValidity(roomCode: string, ticket?: string) {
  return request<RoomValidityResponse>({
    url: `/api/arena/room/${roomCode}/validity`,
    method: 'GET',
    params: { ticket },
  })
}

// ==========================================
// 模块二：天梯高并发匹配 (Matchmaking System)
// ==========================================

export interface MatchStatusResponse {
  status: 'MATCHING' | 'SUCCESS' | 'FAILED'
  roomCode?: string
  ticket?: string
}

export function joinMatchmaking() {
  return request<boolean>({
    url: '/api/arena/match',
    method: 'POST',
  })
}

export function cancelMatchmaking() {
  return request<boolean>({
    url: '/api/arena/match',
    method: 'DELETE',
  })
}

export function getMatchStatus() {
  return request<MatchStatusResponse>({
    url: '/api/arena/match/status',
    method: 'GET',
  })
}

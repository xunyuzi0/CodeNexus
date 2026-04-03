import request from '@/utils/request'

export interface MatchStatus {
  status: 'MATCHING' | 'SUCCESS' | 'FAILED'
  roomCode?: string
  ticket?: string
}

export interface RoomValidity {
  isValid: boolean
  message?: string
  roomType?: number
  status?: string
  problemId?: number
}

// === 1. 私人房间 (熟人开黑) ===
// 🎯 核心修复：接收 data 参数，并将其透传给 axios 的 post 请求体中
export const createRoom = (data?: any) => {
  // 同时修复泛型返回值，使其能够正确接收后端返回的 RoomCreateVO (包含 roomCode)
  return request.post<any, any>('/arena/room', data)
}

export const createPrivateRoom = createRoom

export const checkRoomValidity = (roomCode: string, ticket?: string) => {
  return request.get<any, RoomValidity>(`/arena/room/${roomCode}/validity`, {
    params: { ticket },
  })
}

export const joinRoom = (roomCode: string, ticket?: string) => {
  return checkRoomValidity(roomCode, ticket)
}

// === 2. 天梯匹配 (全服排位) ===
export const joinMatchmaking = () => {
  return request.post<any, void>('/arena/match')
}

export const leaveMatchmaking = () => {
  return request.delete<any, void>('/arena/match')
}

export const getMatchStatus = () => {
  return request.get<any, MatchStatus>('/arena/match/status')
}

// 🐛 核心修复：增加获取对手最终提交代码的接口契约
export const getOpponentCode = (roomCode: string) => {
  return request.get<any, string>(`/arena/room/${roomCode}/opponent-code`)
}

// 结算接口
export function forceSettleMatch(data: { roomCode: string; winnerId: number }) {
  return request.post('/arena/settle', data)
}

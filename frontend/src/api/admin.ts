import { request } from '@/utils/request'
import type {
  AdminUserVO,
  AdminUserDetailVO,
  AdminProblemVO,
  AdminProblemDetailVO,
  ProblemUpsertRequest,
  AdminArenaRoomVO,
  AdminStatsOverviewVO,
  DailyTrendVO,
  ActivityAnalysisVO,
} from '@/types/api'

// ==========================================
// 管理端 API 模块
// ==========================================

// ---------- 用户管理 ----------

export function getAdminUsers(params: {
  current?: number
  pageSize?: number
  keyword?: string
  role?: string
}) {
  return request<{ records: AdminUserVO[]; total: number; current: number; size: number }>({
    url: '/api/admin/users',
    method: 'GET',
    params,
  })
}

export function getAdminUserDetail(id: number) {
  return request<AdminUserDetailVO>({
    url: `/api/admin/users/${id}`,
    method: 'GET',
  })
}

export function updateUserRole(id: number, role: string) {
  return request<boolean>({
    url: `/api/admin/users/${id}/role`,
    method: 'PUT',
    data: { role },
  })
}

export function banUser(id: number) {
  return request<boolean>({
    url: `/api/admin/users/${id}/ban`,
    method: 'PUT',
  })
}

export function unbanUser(id: number) {
  return request<boolean>({
    url: `/api/admin/users/${id}/unban`,
    method: 'PUT',
  })
}

// ---------- 题库管理 ----------

export function getAdminProblems(params: {
  current?: number
  pageSize?: number
  keyword?: string
  difficulty?: number
  status?: number
}) {
  return request<{ records: AdminProblemVO[]; total: number; current: number; size: number }>({
    url: '/api/admin/problems',
    method: 'GET',
    params,
  })
}

export function getAdminProblemDetail(id: number) {
  return request<AdminProblemDetailVO>({
    url: `/api/admin/problems/${id}/detail`,
    method: 'GET',
  })
}

export function createProblem(data: ProblemUpsertRequest) {
  return request<number>({
    url: '/api/admin/problems',
    method: 'POST',
    data,
  })
}

export function updateProblem(id: number, data: ProblemUpsertRequest) {
  return request<boolean>({
    url: `/api/admin/problems/${id}`,
    method: 'PUT',
    data,
  })
}

export function deleteProblem(id: number) {
  return request<boolean>({
    url: `/api/admin/problems/${id}`,
    method: 'DELETE',
  })
}

export function publishProblem(id: number) {
  return request<boolean>({
    url: `/api/admin/problems/${id}/publish`,
    method: 'PUT',
  })
}

export function offlineProblem(id: number) {
  return request<boolean>({
    url: `/api/admin/problems/${id}/offline`,
    method: 'PUT',
  })
}

export function getProblemTestcases(problemId: number) {
  return request<any[]>({
    url: `/api/admin/problems/${problemId}/testcases`,
    method: 'GET',
  })
}

export function addTestcase(
  problemId: number,
  data: { inputData: string; expectedOutput: string; isPublic: number; sortOrder?: number },
) {
  return request<number>({
    url: `/api/admin/problems/${problemId}/testcases`,
    method: 'POST',
    data,
  })
}

export function updateTestcase(
  id: number,
  data: { inputData: string; expectedOutput: string; isPublic: number; sortOrder?: number },
) {
  return request<boolean>({
    url: `/api/admin/problems/testcases/${id}`,
    method: 'PUT',
    data,
  })
}

export function deleteTestcase(id: number) {
  return request<boolean>({
    url: `/api/admin/problems/testcases/${id}`,
    method: 'DELETE',
  })
}

// ---------- 对战记录 ----------

export function getAdminArenaRooms(params: {
  current?: number
  pageSize?: number
  status?: string
  roomType?: number
}) {
  return request<{ records: AdminArenaRoomVO[]; total: number; current: number; size: number }>({
    url: '/api/admin/arena/rooms',
    method: 'GET',
    params,
  })
}

export function getAdminArenaRoomDetail(id: number) {
  return request<AdminArenaRoomVO>({
    url: `/api/admin/arena/rooms/${id}`,
    method: 'GET',
  })
}

export function getAbnormalArenaRooms(params: { current?: number; pageSize?: number }) {
  return request<{ records: AdminArenaRoomVO[]; total: number; current: number; size: number }>({
    url: '/api/admin/arena/rooms/abnormal',
    method: 'GET',
    params,
  })
}

// ---------- 数据统计 ----------

export function getAdminStatsOverview() {
  return request<AdminStatsOverviewVO>({
    url: '/api/admin/stats/overview',
    method: 'GET',
  })
}

export function getAdminStatsTrends(days?: number) {
  return request<DailyTrendVO[]>({
    url: '/api/admin/stats/trends',
    method: 'GET',
    params: { days: days || 30 },
  })
}

export function getAdminStatsActivity() {
  return request<ActivityAnalysisVO>({
    url: '/api/admin/stats/activity',
    method: 'GET',
  })
}

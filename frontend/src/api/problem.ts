/**
 * src/api/problem.ts
 * 题库模块 API 定义
 * -----------------
 * 已根据后端 ProblemController 真实接口规范全面重构
 * 包含 1000 偏移量 displayId 的前端兜底逻辑及代码评测接口
 */

import { request } from '@/utils/request'
import type { PageResult } from '@/types/api'

export type ProblemDifficulty = 'EASY' | 'MEDIUM' | 'HARD'
export type ProblemStatus = 'PASSED' | 'ATTEMPTED' | 'NOT_STARTED'

export interface Problem {
  id: number
  displayId: string
  title: string
  difficulty: ProblemDifficulty
  tags: string[]
  passRate: number
  status: ProblemStatus
  updatedTime: string
  content: string
  timeLimit?: number
  memoryLimit?: number
  examples: {
    input: string
    output: string
    explanation?: string
  }[]
}

export interface ProblemQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  difficulty?: ProblemDifficulty | ''
  tags?: string[]
}

// ==== 题解数据结构 ====
export interface ProblemSolution {
  id: number
  problemId: number
  authorId: number
  title: string
  content: string
  code: string
  authorName: string
  viewCount: number
  createTime: string
  isOfficial: number
}

// ==== 提交记录数据结构 ====
export interface SubmissionHistory {
  id: number
  status: number // 0: PENDING, 1: AC, 2: WA, 3: TLE, 4: MLE, 5: RE, 6: CE
  timeCost: number
  memoryCost: number
  language: string
  submitTime: string
}

// ==========================================
// 新增：沙箱判题与代码执行接口相关类型
// ==========================================

export interface RunCodeRequest {
  code: string
  language: string
  inputs: string[]
}

export interface RunCodeResult {
  status: 'AC' | 'ERROR' | 'TLE'
  input: string
  output: string
  expected: string | null
  runtime: string
}

export type CheckpointStatus = 'PENDING' | 'RUNNING' | 'AC' | 'WA' | 'TLE' | 'RE' | 'MLE'

export interface SubmissionCheckpoint {
  id: number
  status: CheckpointStatus
  time?: string | null
  memory?: string | null
}

export interface PollSubmissionResult {
  status: 'JUDGING' | 'OK'
  message?: string
  checkpoints: SubmissionCheckpoint[]
}

// ==========================================

const mapDifficultyToBackend = (diff?: ProblemDifficulty | ''): number | undefined => {
  if (diff === 'EASY') return 1
  if (diff === 'MEDIUM') return 2
  if (diff === 'HARD') return 3
  return undefined
}

const mapDifficultyToFront = (diff: number): ProblemDifficulty => {
  if (diff === 1) return 'EASY'
  if (diff === 2) return 'MEDIUM'
  return 'HARD'
}

const mapStatusToFront = (state: number): ProblemStatus => {
  if (state === 2) return 'PASSED'
  if (state === 1) return 'ATTEMPTED'
  return 'NOT_STARTED'
}

// 获取题目列表 (支持分页与多条件查询)
export async function getProblemList(params: ProblemQuery): Promise<PageResult<Problem>> {
  const backendParams = {
    current: params.pageNum,
    pageSize: params.pageSize,
    keyword: params.keyword,
    difficulty: mapDifficultyToBackend(params.difficulty),
    tags: params.tags?.join(','),
  }

  const res = await request<any>({
    url: '/api/problems',
    method: 'GET',
    params: backendParams,
  })

  return {
    list: (res?.records || []).map((item: any) => ({
      id: item.id,
      displayId: item.displayId || `P-${1000 + Number(item.id)}`,
      title: item.title,
      difficulty: mapDifficultyToFront(item.difficulty),
      tags: typeof item.tags === 'string' ? JSON.parse(item.tags) : item.tags || [],
      passRate: item.passRate ? Number((item.passRate * 100).toFixed(1)) : 0,
      status: mapStatusToFront(item.userState ?? 0),
      updatedTime: item.updateTime || item.createTime,
      content: item.content || '',
      examples: typeof item.examples === 'string' ? JSON.parse(item.examples) : item.examples || [],
    })),
    total: res?.total || 0,
    pageNum: res?.current || params.pageNum,
    pageSize: res?.size || params.pageSize,
  }
}

// 获取题目详情
export async function getProblemDetail(id: number | string): Promise<Problem> {
  const item = await request<any>({
    url: `/api/problems/${id}`,
    method: 'GET',
  })

  return {
    id: item.id,
    displayId: item.displayId || `P-${1000 + Number(item.id)}`,
    title: item.title,
    difficulty: mapDifficultyToFront(item.difficulty),
    tags: typeof item.tags === 'string' ? JSON.parse(item.tags) : item.tags || [],
    passRate: item.passRate ? Number((item.passRate * 100).toFixed(1)) : 0,
    status: mapStatusToFront(item.userState ?? 0),
    updatedTime: item.updateTime || item.createTime,
    content: item.content || '',
    timeLimit: item.timeLimit,
    memoryLimit: item.memoryLimit,
    examples: typeof item.examples === 'string' ? JSON.parse(item.examples) : item.examples || [],
  }
}

// 获取官方题解
// 获取题目的所有题解列表 (注意方法名是复数 Solutions)
export async function getProblemSolutions(problemId: number | string): Promise<ProblemSolution[]> {
  return await request<ProblemSolution[]>({
    url: `/api/problems/${problemId}/solutions`,
    method: 'GET',
  })
}

// 获取提交记录 (带分页封装)
export async function getProblemSubmissions(
  problemId: number | string,
  params: { current?: number; pageSize?: number } = { current: 1, pageSize: 10 },
): Promise<PageResult<SubmissionHistory>> {
  const res = await request<any>({
    url: `/api/problems/${problemId}/submissions`,
    method: 'GET',
    params,
  })

  return {
    list: res?.records || [],
    total: res?.total || 0,
    pageNum: res?.current || params.current || 1,
    pageSize: res?.size || params.pageSize || 10,
  }
}

// ==========================================
// 新增：执行代码 (自测)
// ==========================================
export async function runCode(
  problemId: number | string,
  data: RunCodeRequest,
): Promise<RunCodeResult[]> {
  return await request<RunCodeResult[]>({
    url: `/api/problems/${problemId}/run`,
    method: 'POST',
    data,
  })
}

// ==========================================
// 新增：提交代码 (返回 submissionId)
// ==========================================
export async function submitCode(
  problemId: number | string,
  data: { code: string; language: string },
): Promise<number> {
  return await request<number>({
    url: `/api/problems/${problemId}/submit`,
    method: 'POST',
    data,
  })
}

// ==========================================
// 新增：轮询判题进度
// ==========================================
export async function getSubmissionStatus(
  submissionId: number | string,
): Promise<PollSubmissionResult> {
  return await request<PollSubmissionResult>({
    url: `/api/submissions/${submissionId}/status`,
    method: 'GET',
  })
}

// 获取每日推荐题目（返回题目 ID 及是否已 AC）
export async function getDailyRecommendProblem(): Promise<{
  problemId: number
  alreadySolved: boolean
}> {
  const res = await request<{ problemId: number; alreadySolved: boolean }>({
    url: '/api/problems/daily-practice',
    method: 'GET',
  })
  return res
}

export function getRandomProblem() {
  return request.get('/problems/random')
}

// 新增：发布题解
export async function publishProblemSolution(
  problemId: number | string,
  data: { title: string; content: string },
): Promise<void> {
  return await request<void>({
    url: `/api/problems/${problemId}/solutions`,
    method: 'POST',
    data,
  })
}

// 修改我的题解
export async function updateProblemSolution(
  solutionId: number | string,
  data: { title: string; content: string },
): Promise<void> {
  return await request<void>({
    url: `/api/problems/solutions/${solutionId}`,
    method: 'PUT',
    data,
  })
}

// 删除我的题解
export async function deleteProblemSolution(solutionId: number | string): Promise<void> {
  return await request<void>({
    url: `/api/problems/solutions/${solutionId}`,
    method: 'DELETE',
  })
}

// 记录题解阅读量 (后端包含防刷机制)
export async function recordSolutionView(solutionId: number | string): Promise<void> {
  return await request<void>({
    url: `/api/problems/solutions/${solutionId}/view`,
    method: 'POST',
  })
}

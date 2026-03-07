/**
 * src/api/problem.ts
 * 题库模块 API 定义
 * -----------------
 * 已根据后端 ProblemController 真实接口规范全面重构
 */

// 【修复 1】：使用具名导入 { request }，它能正确推断 Promise<T>，彻底解决 AxiosResponse 类型报错
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

export type CheckpointStatus = 'PENDING' | 'AC' | 'WA' | 'TLE' | 'RE' | 'MLE'

export interface SubmissionCheckpoint {
  id: number
  status: CheckpointStatus
  time: string
  memory: string
}

export interface SubmitResponse {
  status: 'OK' | 'COMPILE_ERROR'
  message?: string
  checkpoints?: SubmissionCheckpoint[]
}

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
      displayId: item.displayId || `P-${item.id}`,
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
    displayId: item.displayId || `P-${item.id}`,
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

// 模拟代码提交与判题接口 (后端暂未实现，保留前端 Mock)
export function submitCode(problemId: number, code: string): Promise<SubmitResponse> {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        status: 'OK',
        checkpoints: [
          { id: 1, status: 'AC', time: '12ms', memory: '4.2MB' },
          { id: 2, status: 'AC', time: '14ms', memory: '4.3MB' },
        ],
      })
    }, 500)
  })
}

// 补充缺失的导出方法，解决 dashboard 引入报错的问题
export async function getDailyRecommendProblem(): Promise<number> {
  const res = await request<number>({
    url: '/api/problem/recommend/daily',
    method: 'GET',
  })
  return res
}

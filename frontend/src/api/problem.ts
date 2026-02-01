/**
 * src/api/problem.ts
 * 题库模块 API 定义
 * -----------------
 * [汉化版] 题目数据与筛选条件
 */

import type { PageResult } from '@/types/api'

// --- Types ---

export type ProblemDifficulty = 'EASY' | 'MEDIUM' | 'HARD'
export type ProblemStatus = 'PASSED' | 'ATTEMPTED' | 'NOT_STARTED'

export interface Problem {
  id: number
  displayId: string // 例如 "1001", "P-024"
  title: string
  difficulty: ProblemDifficulty
  tags: string[]
  passRate: number // 0 - 100
  status: ProblemStatus
  updatedTime: string
}

export interface ProblemQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  difficulty?: ProblemDifficulty | ''
  tags?: string[]
}

// --- Mock Data Generator (汉化版) ---

const MOCK_PROBLEMS: Problem[] = [
  {
    id: 1,
    displayId: '1001',
    title: '两数之和 (Two Sum)',
    difficulty: 'EASY',
    tags: ['数组', '哈希表'],
    passRate: 48.5,
    status: 'PASSED',
    updatedTime: '2023-10-01',
  },
  {
    id: 2,
    displayId: '1002',
    title: '两数相加 (Add Two Numbers)',
    difficulty: 'MEDIUM',
    tags: ['链表', '数学'],
    passRate: 39.0,
    status: 'ATTEMPTED',
    updatedTime: '2023-10-02',
  },
  {
    id: 3,
    displayId: '1003',
    title: '无重复字符的最长子串',
    difficulty: 'MEDIUM',
    tags: ['字符串', '滑动窗口'],
    passRate: 32.8,
    status: 'NOT_STARTED',
    updatedTime: '2023-10-05',
  },
  {
    id: 4,
    displayId: '1004',
    title: '寻找两个正序数组的中位数',
    difficulty: 'HARD',
    tags: ['数组', '二分查找'],
    passRate: 28.2,
    status: 'NOT_STARTED',
    updatedTime: '2023-10-06',
  },
  {
    id: 5,
    displayId: '1005',
    title: '最长回文子串',
    difficulty: 'MEDIUM',
    tags: ['字符串', '动态规划'],
    passRate: 31.5,
    status: 'PASSED',
    updatedTime: '2023-10-08',
  },
  {
    id: 6,
    displayId: '1006',
    title: 'N 字形变换',
    difficulty: 'MEDIUM',
    tags: ['字符串'],
    passRate: 42.1,
    status: 'NOT_STARTED',
    updatedTime: '2023-10-09',
  },
  {
    id: 7,
    displayId: '1007',
    title: '整数反转',
    difficulty: 'MEDIUM',
    tags: ['数学'],
    passRate: 26.4,
    status: 'NOT_STARTED',
    updatedTime: '2023-10-10',
  },
  {
    id: 8,
    displayId: '1008',
    title: '字符串转换整数 (atoi)',
    difficulty: 'MEDIUM',
    tags: ['字符串'],
    passRate: 16.5,
    status: 'ATTEMPTED',
    updatedTime: '2023-10-11',
  },
  {
    id: 9,
    displayId: '1009',
    title: '回文数',
    difficulty: 'EASY',
    tags: ['数学'],
    passRate: 52.3,
    status: 'PASSED',
    updatedTime: '2023-10-12',
  },
  {
    id: 10,
    displayId: '1010',
    title: '正则表达式匹配',
    difficulty: 'HARD',
    tags: ['字符串', '动态规划', '递归'],
    passRate: 27.8,
    status: 'NOT_STARTED',
    updatedTime: '2023-10-15',
  },
]

// --- API Function ---

export function getProblemList(params: ProblemQuery): Promise<PageResult<Problem>> {
  return new Promise((resolve) => {
    console.log('[Mock API] Fetching problems with params:', params)

    setTimeout(() => {
      let result = MOCK_PROBLEMS

      if (params.difficulty) {
        result = result.filter((p) => p.difficulty === params.difficulty)
      }
      if (params.keyword) {
        const k = params.keyword.toLowerCase()
        result = result.filter(
          (p) =>
            p.title.toLowerCase().includes(k) ||
            p.displayId.includes(k) ||
            p.tags.some((t) => t.toLowerCase().includes(k)),
        )
      }

      resolve({
        list: result,
        total: result.length,
        pageNum: params.pageNum,
        pageSize: params.pageSize,
      })
    }, 500)
  })
}

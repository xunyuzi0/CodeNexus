/**
 * src/api/problem.ts
 * 题库模块 API 定义
 * -----------------
 * [Mock版] 包含题目数据、筛选逻辑及增强的判题模拟
 */

import type { PageResult } from '@/types/api'

// --- Types ---

export type ProblemDifficulty = 'EASY' | 'MEDIUM' | 'HARD'
export type ProblemStatus = 'PASSED' | 'ATTEMPTED' | 'NOT_STARTED'

// 题目详情接口
export interface Problem {
  id: number
  displayId: string // 例如 "1001", "P-024"
  title: string
  difficulty: ProblemDifficulty
  tags: string[]
  passRate: number // 0 - 100
  status: ProblemStatus
  updatedTime: string
  content: string // HTML 格式题目描述
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

// 判题系统相关类型
export type CheckpointStatus = 'PENDING' | 'AC' | 'WA' | 'TLE' | 'RE' | 'MLE'

export interface SubmissionCheckpoint {
  id: number
  status: CheckpointStatus
  time: string // e.g. "12ms"
  memory: string // e.g. "4.2MB"
}

// 提交响应接口
export interface SubmitResponse {
  status: 'OK' | 'COMPILE_ERROR'
  message?: string // 编译错误信息或系统提示
  checkpoints?: SubmissionCheckpoint[] // 正常判题时的检测点详情
}

// --- Mock Data Generator ---

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
    content: `
      <p>给定一个整数数组 <code>nums</code> 和一个整数目标值 <code>target</code>，请你在该数组中找出 <strong>和为目标值</strong> <em><code>target</code></em>  的那 <strong>两个</strong> 整数，并返回它们的数组下标。</p>
      <p>你可以假设每种输入只会对应一个答案。但是，数组中同一个元素在答案里不能重复出现。</p>
    `,
    examples: [
      {
        input: 'nums = [2,7,11,15], target = 9',
        output: '[0,1]',
        explanation: '因为 nums[0] + nums[1] == 9 ，返回 [0, 1] 。',
      },
      { input: 'nums = [3,2,4], target = 6', output: '[1,2]' },
    ],
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
    content: `<p>给你两个 <strong>非空</strong> 的链表，表示两个非负的整数。它们每位数字都是按照 <strong>逆序</strong> 的方式存储的，并且每个节点只能存储 <strong>一位</strong> 数字。</p>`,
    examples: [{ input: 'l1 = [2,4,3], l2 = [5,6,4]', output: '[7,0,8]' }],
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
    content: `<p>给定一个字符串 <code>s</code> ，请你找出其中不含有重复字符的 <strong>最长子串</strong> 的长度。</p>`,
    examples: [
      { input: 's = "abcabcbb"', output: '3' },
      { input: 's = "bbbbb"', output: '1' },
    ],
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
    content: `<p>给定两个大小分别为 <code>m</code> 和 <code>n</code> 的正序（从小到大）数组 <code>nums1</code> 和 <code>nums2</code>。请你找出并返回这两个正序数组的 <strong>中位数</strong> 。</p>`,
    examples: [{ input: 'nums1 = [1,3], nums2 = [2]', output: '2.00000' }],
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
    content: `<p>给你一个字符串 <code>s</code>，找到 <code>s</code> 中最长的回文子串。</p>`,
    examples: [{ input: 's = "babad"', output: '"bab"' }],
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
    content: `<p>将一个给定字符串 <code>s</code> 根据给定的行数 <code>numRows</code> ，以从上往下、从左到右进行 Z 字形排列。</p>`,
    examples: [{ input: 's = "PAYPALISHIRING", numRows = 3', output: '"PAHNAPLSIIGYIR"' }],
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
    content: `<p>给你一个 32 位的有符号整数 <code>x</code> ，返回将 <code>x</code> 中的数字部分反转后的结果。</p>`,
    examples: [{ input: 'x = 123', output: '321' }],
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
    content: `<p>请你来实现一个 <code>myAtoi(string s)</code> 函数，使其能将字符串转换成一个 32 位有符号整数。</p>`,
    examples: [{ input: 's = "42"', output: '42' }],
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
    content: `<p>给你一个整数 <code>x</code> ，如果 <code>x</code> 是一个回文整数，返回 <code>true</code> ；否则，返回 <code>false</code> 。</p>`,
    examples: [{ input: 'x = 121', output: 'true' }],
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
    content: `<p>请你实现一个支持 <code>'.'</code> 和 <code>'*'</code> 的正则表达式匹配。</p>`,
    examples: [{ input: 's = "aa", p = "a"', output: 'false' }],
  },
]

// --- API Functions ---

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

// 模拟代码提交与判题接口
export function submitCode(problemId: number, code: string): Promise<SubmitResponse> {
  return new Promise((resolve) => {
    console.log(`[Mock API] Submitting code for problem ${problemId}...`)

    // 延迟 500ms 响应
    setTimeout(() => {
      const rand = Math.random()

      // 场景 1: 模拟编译错误 (5% 概率)
      if (rand < 0.05) {
        resolve({
          status: 'COMPILE_ERROR',
          message: `Main.java:14: error: cannot find symbol
        return new Sloution().solve(nums);
                   ^
  symbol:   class Sloution
  location: class Main
1 error`,
        })
        return
      }

      // 场景 2: 正常判题 (95% 概率)
      // 生成 5 到 10 个测试点
      const checkpointCount = Math.floor(Math.random() * 6) + 5
      const checkpoints: SubmissionCheckpoint[] = []

      // 80% 概率全 AC，20% 概率出现问题
      const isAllAc = Math.random() < 0.8

      // 错误类型池
      const errorTypes: CheckpointStatus[] = ['WA', 'TLE', 'RE', 'MLE']

      for (let i = 1; i <= checkpointCount; i++) {
        let status: CheckpointStatus = 'AC'
        let timeVal = Math.floor(Math.random() * 50) + 10 // 10-60ms
        let memVal = (Math.random() * 20 + 10).toFixed(1) // 10.0-30.0MB

        if (!isAllAc) {
          // 如果不是全 AC，后面的点有概率出错 (越靠后出错概率越大)
          if (i > 2 && Math.random() > 0.6) {
            // [修复]: 增加 ?? 'WA' 兜底，解决 TypeScript 认为数组访问可能为 undefined 的报错
            const randomError = errorTypes[Math.floor(Math.random() * errorTypes.length)]
            status = randomError ?? 'WA'
          }
        }

        // 根据状态调整时间和内存显示的真实感
        if (status === 'TLE') timeVal = 2000 + Math.floor(Math.random() * 1000)
        if (status === 'MLE') memVal = (256 + Math.random() * 50).toFixed(1)

        checkpoints.push({
          id: i,
          status,
          time: status === 'TLE' ? '>2000ms' : `${timeVal}ms`,
          memory: status === 'MLE' ? '>256MB' : `${memVal}MB`,
        })
      }

      resolve({
        status: 'OK',
        checkpoints,
      })
    }, 500)
  })
}

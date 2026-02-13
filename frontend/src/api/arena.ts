/**
 * src/api/arena.ts
 * 竞技场模块 API 定义
 * -----------------
 * [Security] 包含房间校验、反作弊检查等安全接口
 */

/**
 * 检查房间有效性 (模拟接口)
 * @param roomId 房间 ID
 * @returns Promise<boolean>
 */
export function checkRoomValidity(roomId: string): Promise<boolean> {
  return new Promise((resolve) => {
    // 模拟网络延迟，展示 "Verifying..." 动效
    setTimeout(() => {
      // 模拟校验逻辑：
      // 1. 判空
      if (!roomId) {
        resolve(false)
        return
      }

      // 2. 模拟无效 ID (例如包含 'INVALID')
      if (roomId.includes('INVALID')) {
        resolve(false)
        return
      }

      // 3. 随机模拟：50% 的概率房间有效，50% 概率房间已过期
      const isValid = Math.random() > 0.5
      resolve(isValid)
    }, 800)
  })
}

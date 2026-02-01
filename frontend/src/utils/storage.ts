/**
 * src/utils/storage.ts
 * Zeekr Infrastructure - Storage Engine
 * -------------------------------------
 * 封装 localStorage，提供强类型支持与命名空间隔离。
 * 每一个字节的存储都应当清晰、可控。
 */

const NAMESPACE = 'NEXUS_'
const TOKEN_KEY = 'ACCESS_TOKEN'

export const storage = {
  /**
   * 存入数据
   * @param key 键名（自动拼接命名空间）
   * @param value 数据载荷
   */
  set(key: string, value: unknown) {
    try {
      const serializedValue = JSON.stringify(value)
      localStorage.setItem(NAMESPACE + key, serializedValue)
    } catch (err) {
      console.error('[Nexus Storage] Set Error:', err)
    }
  },

  /**
   * 读取数据
   * @param key 键名
   * @returns 解析后的数据 T 或 null
   */
  get<T>(key: string): T | null {
    try {
      const rawValue = localStorage.getItem(NAMESPACE + key)
      if (!rawValue) return null
      return JSON.parse(rawValue) as T
    } catch (err) {
      console.error('[Nexus Storage] Get Error:', err)
      return null
    }
  },

  /**
   * 移除特定数据
   */
  remove(key: string) {
    localStorage.removeItem(NAMESPACE + key)
  },

  /**
   * 清空当前命名空间下的所有数据（安全清空）
   */
  clear() {
    Object.keys(localStorage).forEach((key) => {
      if (key.startsWith(NAMESPACE)) {
        localStorage.removeItem(key)
      }
    })
  },

  // --- Token 专属通道 ---

  setToken(token: string) {
    this.set(TOKEN_KEY, token)
  },

  getToken(): string | null {
    return this.get<string>(TOKEN_KEY)
  },

  removeToken() {
    this.remove(TOKEN_KEY)
  },
}

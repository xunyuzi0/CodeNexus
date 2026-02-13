/**
 * src/utils/storage.ts
 * Zeekr Infrastructure - Storage Engine
 * -------------------------------------
 * 封装 localStorage 和 sessionStorage，提供强类型支持与命名空间隔离。
 * [Updated] 已修复：增加 type 参数以支持 SessionStorage
 */

const NAMESPACE = 'NEXUS_'
const TOKEN_KEY = 'ACCESS_TOKEN'

type StorageType = 'local' | 'session'

export const storage = {
  /**
   * 存入数据
   * @param key 键名（自动拼接命名空间）
   * @param value 数据载荷
   * @param type 存储类型 ('local' | 'session')，默认为 'local'
   */
  set(key: string, value: unknown, type: StorageType = 'local') {
    try {
      const serializedValue = JSON.stringify(value)
      const store = type === 'session' ? sessionStorage : localStorage
      store.setItem(NAMESPACE + key, serializedValue)
    } catch (err) {
      console.error('[Nexus Storage] Set Error:', err)
    }
  },

  /**
   * 读取数据
   * @param key 键名
   * @param type 存储类型 ('local' | 'session')，默认为 'local'
   * @returns 解析后的数据 T 或 null
   */
  get<T>(key: string, type: StorageType = 'local'): T | null {
    try {
      const store = type === 'session' ? sessionStorage : localStorage
      const rawValue = store.getItem(NAMESPACE + key)
      if (!rawValue) return null
      return JSON.parse(rawValue) as T
    } catch (err) {
      console.error('[Nexus Storage] Get Error:', err)
      return null
    }
  },

  /**
   * 移除特定数据
   * @param key 键名
   * @param type 存储类型 ('local' | 'session')，默认为 'local'
   */
  remove(key: string, type: StorageType = 'local') {
    const store = type === 'session' ? sessionStorage : localStorage
    store.removeItem(NAMESPACE + key)
  },

  /**
   * 清空当前命名空间下的所有数据（安全清空）
   * @param type 存储类型 ('local' | 'session')，默认为 'local'
   */
  clear(type: StorageType = 'local') {
    const store = type === 'session' ? sessionStorage : localStorage
    Object.keys(store).forEach((key) => {
      if (key.startsWith(NAMESPACE)) {
        store.removeItem(key)
      }
    })
  },

  // --- Token 专属通道 (始终使用 localStorage) ---

  setToken(token: string) {
    this.set(TOKEN_KEY, token, 'local')
  },

  getToken(): string | null {
    return this.get<string>(TOKEN_KEY, 'local')
  },

  removeToken() {
    this.remove(TOKEN_KEY, 'local')
  },
}

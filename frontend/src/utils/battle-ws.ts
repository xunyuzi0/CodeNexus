export type BattleWsAction =
  | 'SYNC_ROOM'
  | 'PLAYER_JOINED'
  | 'PLAYER_READY'
  | 'GAME_START'
  | 'BATTLE_LOG'
  | 'TELEMETRY_SYNC'
  | 'READY' // 客户端发送准备
  | 'ESCAPE_LOBBY' // 客户端主动退出大厅
  | 'SURRENDER' // 客户端主动投降
  | 'PLAYER_ESCAPED' // 后端广播逃跑
  | 'MATCH_SETTLED' // 后端广播结算

export interface BattleWsPayload<T = any> {
  type: BattleWsAction
  data: T
}

export class BattleWebSocket {
  private ws: WebSocket | null = null
  private roomCode: string
  private token: string
  private isConnected: boolean = false
  private reconnectAttempts: number = 0
  private maxReconnectAttempts: number = 5

  // 核心：事件订阅字典
  private listeners: Map<BattleWsAction, ((data: any) => void)[]> = new Map()

  constructor(roomCode: string, token: string) {
    this.roomCode = roomCode
    this.token = token
  }

  /**
   * 动态获取 WS 地址 (完美适配 Vite Proxy)
   */
  private getWsUrl(): string {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host // 例如 localhost:5173

    // 【核心修复】：强制净化 Token，利用正则忽略大小写，剥离任何可能存在的 "Bearer " 前缀
    // 确保拼接到 URL 上的绝对是极其纯净的 JWT 字符串！
    const pureToken = this.token.replace(/^Bearer\s+/i, '').trim()

    return `${protocol}//${host}/api/ws/arena/${this.roomCode}?token=${pureToken}`
  }

  /**
   * 建立连接
   */
  public connect() {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) return

    const url = this.getWsUrl()
    console.log(`[BattleWS] Connecting to ${url.replace(/(token=)[^&]+/, '$1***HIDDEN***')}...`) // 日志脱敏

    this.ws = new WebSocket(url)

    this.ws.onopen = () => {
      console.log(`[BattleWS] Connected to Room: ${this.roomCode}`)
      this.isConnected = true
      this.reconnectAttempts = 0
    }

    this.ws.onmessage = (event) => {
      try {
        const payload: BattleWsPayload = JSON.parse(event.data)
        this.dispatch(payload.type, payload.data)
      } catch (err) {
        console.error('[BattleWS] Failed to parse message:', err)
      }
    }

    this.ws.onclose = (event) => {
      this.isConnected = false
      console.warn(`[BattleWS] Disconnected. Code: ${event.code}, Reason: ${event.reason}`)
      this.handleReconnect()
    }

    this.ws.onerror = (error) => {
      console.error('[BattleWS] WebSocket Error:', error)
    }
  }

  /**
   * 发送指令到后端
   */
  public send(type: BattleWsAction, data: any = {}) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.warn(`[BattleWS] Cannot send ${type}, socket is not open.`)
      return
    }
    const payload = JSON.stringify({ type, data })
    this.ws.send(payload)
  }

  /**
   * 注册事件监听器
   */
  public on(type: BattleWsAction, callback: (data: any) => void) {
    if (!this.listeners.has(type)) {
      this.listeners.set(type, [])
    }
    this.listeners.get(type)!.push(callback)
  }

  /**
   * 卸载事件监听器
   */
  public off(type: BattleWsAction, callback?: (data: any) => void) {
    if (!callback) {
      this.listeners.delete(type)
    } else {
      const callbacks = this.listeners.get(type)
      if (callbacks) {
        this.listeners.set(
          type,
          callbacks.filter((cb) => cb !== callback),
        )
      }
    }
  }

  /**
   * 内部事件派发
   */
  private dispatch(type: BattleWsAction, data: any) {
    const callbacks = this.listeners.get(type)
    if (callbacks) {
      callbacks.forEach((cb) => cb(data))
    }
  }

  /**
   * 断线重连机制
   */
  private handleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('[BattleWS] Max reconnect attempts reached. Connection failed.')
      return
    }
    this.reconnectAttempts++
    const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 10000)
    console.log(
      `[BattleWS] Attempting to reconnect in ${delay}ms... (Attempt ${this.reconnectAttempts})`,
    )

    setTimeout(() => {
      this.connect()
    }, delay)
  }

  /**
   * 手动销毁连接 (组件卸载时调用)
   */
  public disconnect() {
    if (this.ws) {
      this.ws.onclose = null // 防止触发重连
      this.ws.close()
      this.ws = null
    }
    this.isConnected = false
    this.listeners.clear()
    console.log(`[BattleWS] Connection destroyed.`)
  }
}

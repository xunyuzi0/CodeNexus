import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { storage } from '@/utils/storage'

export interface SettingsState {
  // 编辑器核心设置
  editorFontSize: number
  editorTheme: 'vs-dark' | 'dracula' | 'one-dark'
  defaultLanguage: 'java' | 'python' | 'cpp'
}

export const useSettingsStore = defineStore('settings', () => {
  // 默认配置
  const defaultSettings: SettingsState = {
    editorFontSize: 14,
    editorTheme: 'vs-dark',
    defaultLanguage: 'java',
  }

  // 从缓存读取或使用默认值
  const cached = storage.get<SettingsState>('USER_SETTINGS')

  // 使用 ref 定义响应式状态
  const settings = ref<SettingsState>(cached ? { ...defaultSettings, ...cached } : defaultSettings)

  // 监听变化并自动持久化
  watch(
    settings,
    (newVal) => {
      storage.set('USER_SETTINGS', newVal)
    },
    { deep: true },
  )

  // 重置功能
  function resetSettings() {
    settings.value = { ...defaultSettings }
  }

  return {
    settings,
    resetSettings,
  }
})

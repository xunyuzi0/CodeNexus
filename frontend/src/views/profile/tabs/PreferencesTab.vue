<template>
  <div class="space-y-8" v-motion-slide-visible-bottom>
    <div class="space-y-4">
      <h3 class="text-sm font-bold text-zinc-500 uppercase tracking-wider">编辑器主题</h3>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div
          v-for="theme in themes"
          :key="theme.id"
          @click="selectTheme(theme)"
          class="relative group rounded-xl border-2 p-3 transition-all duration-300 overflow-hidden flex flex-col gap-3"
          :class="[
            currentTheme === theme.id && !theme.disabled
              ? 'bg-[#FF4C00]/10 border-[#FF4C00] shadow-[0_0_30px_rgba(255,76,0,0.15)] ring-2 ring-[#FF4C00]/20'
              : 'bg-zinc-50 dark:bg-zinc-900/50 border-zinc-200 dark:border-white/5',
            theme.disabled
              ? 'opacity-30 grayscale cursor-not-allowed'
              : 'cursor-pointer hover:scale-[1.02]',
          ]"
        >
          <div
            class="w-full aspect-[4/3] rounded-lg p-2 flex flex-col gap-2 shadow-inner"
            :style="{ background: theme.color }"
          >
            <div class="flex items-center gap-1.5 opacity-50">
              <div class="w-1.5 h-1.5 rounded-full bg-red-500"></div>
              <div class="w-1.5 h-1.5 rounded-full bg-yellow-500"></div>
              <div class="w-1.5 h-1.5 rounded-full bg-emerald-500"></div>
            </div>
            <div class="space-y-1.5 mt-1 opacity-40">
              <div class="h-1 w-1/2 bg-white/50 rounded-full"></div>
              <div class="h-1 w-3/4 bg-white/30 rounded-full"></div>
              <div class="h-1 w-2/3 bg-white/30 rounded-full"></div>
              <div class="h-1 w-full bg-white/20 rounded-full"></div>
            </div>
          </div>

          <div
            v-if="currentTheme === theme.id && !theme.disabled"
            class="absolute top-2 right-2 w-5 h-5 rounded-full bg-[#FF4C00] flex items-center justify-center"
          >
            <Check class="w-3 h-3 text-white" />
          </div>

          <p
            class="text-xs font-bold text-center tracking-wide"
            :class="
              currentTheme === theme.id && !theme.disabled ? 'text-[#FF4C00]' : 'text-zinc-400'
            "
          >
            {{ theme.name }}
          </p>
        </div>
      </div>
    </div>

    <div class="space-y-4">
      <h3 class="text-sm font-bold text-zinc-500 uppercase tracking-wider">偏好语言</h3>
      <div class="grid grid-cols-3 gap-4">
        <button
          v-for="lang in languages"
          :key="lang.id"
          @click="selectLang(lang)"
          class="relative py-4 px-6 rounded-xl font-mono font-bold border-2 transition-all duration-300 overflow-hidden"
          :class="[
            currentLang === lang.id && !lang.disabled
              ? 'bg-[#FF4C00]/10 border-[#FF4C00] text-[#FF4C00] shadow-[0_0_30px_rgba(255,76,0,0.15)] ring-2 ring-[#FF4C00]/20'
              : 'bg-zinc-50 dark:bg-zinc-900/50 text-zinc-600 dark:text-zinc-500 border-zinc-200 dark:border-white/5',
            lang.disabled
              ? 'opacity-30 grayscale cursor-not-allowed'
              : 'cursor-pointer hover:scale-[1.02]',
          ]"
        >
          <div
            v-if="currentLang === lang.id && !lang.disabled"
            class="absolute top-1.5 right-1.5 w-4 h-4 rounded-full bg-[#FF4C00] flex items-center justify-center"
          >
            <Check class="w-2.5 h-2.5 text-white" />
          </div>
          {{ lang.name }}
        </button>
      </div>
    </div>

    <div class="flex justify-end pt-4">
      <button
        @click="handleSave"
        :disabled="saving"
        class="flex items-center gap-2 px-8 py-3 bg-[#FF4C00] hover:bg-[#FF4C00]/90 text-white rounded-xl font-bold transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-[#FF4C00]/20 active:scale-95"
      >
        <Save v-if="!saving" class="w-5 h-5" />
        <div
          v-else
          class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"
        ></div>
        <span>{{ saving ? '保存中...' : '保存配置' }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Save, Check } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'
import { useSettingsStore } from '@/stores/settings'
import { updatePreferences } from '@/api/user'

const userStore = useUserStore()
const settingsStore = useSettingsStore()
const saving = ref(false)

const currentTheme = ref('github-light')
const currentLang = ref('java')

interface OptionItem {
  id: string
  name: string
  color?: string
  disabled: boolean
}

const themes: OptionItem[] = [
  { id: 'github-light', name: 'Github Light', color: '#e1e1e1', disabled: false },
  { id: 'vs-dark', name: 'VS Dark', color: '#1e1e1e', disabled: true },
  { id: 'monokai', name: 'Monokai', color: '#272822', disabled: true },
  { id: 'dracula', name: 'Dracula', color: '#282a36', disabled: true },
]

const languages: OptionItem[] = [
  { id: 'java', name: 'Java', disabled: false },
  { id: 'python', name: 'Python', disabled: true },
  { id: 'cpp', name: 'C++', disabled: true },
]

// 数据回显逻辑
onMounted(() => {
  const prefs = (userStore.userInfo as any)?.preferences
  if (prefs) {
    currentTheme.value = prefs.editorTheme || 'github-light'
    if (prefs.extraSettings) {
      try {
        const extra = JSON.parse(prefs.extraSettings)
        if (extra.defaultLanguage) currentLang.value = extra.defaultLanguage
      } catch (e) {
        console.warn('Failed to parse extraSettings')
      }
    }
  } else {
    // 降级：如果云端没配过，取本地 settingsStore
    currentTheme.value = settingsStore.settings.editorTheme || 'github-light'
    currentLang.value = settingsStore.settings.defaultLanguage || 'java'
  }

  // 如果回显的主题或语言已被禁用，强制回退到默认值
  const themeExists = themes.find((t) => t.id === currentTheme.value && !t.disabled)
  if (!themeExists) currentTheme.value = 'github-light'

  const langExists = languages.find((l) => l.id === currentLang.value && !l.disabled)
  if (!langExists) currentLang.value = 'java'
})

const selectTheme = (theme: OptionItem) => {
  if (theme.disabled) return
  currentTheme.value = theme.id
}

const selectLang = (lang: OptionItem) => {
  if (lang.disabled) return
  currentLang.value = lang.id
}

// 提交云端并同步本地
const handleSave = async () => {
  saving.value = true
  try {
    await updatePreferences({
      editorTheme: currentTheme.value,
      fontSize: settingsStore.settings.editorFontSize || 14,
      systemNotifications: 1, // 默认开启
      extraSettings: JSON.stringify({ defaultLanguage: currentLang.value }),
    })

    // 同步到本地 Store (这会触发所有监听了 settingStore 的组件响应)
    settingsStore.settings.editorTheme = currentTheme.value as any
    settingsStore.settings.defaultLanguage = currentLang.value as any

    // 刷新全量信息
    await userStore.fetchUserProfile()
  } catch (error) {
    console.error('Update preferences failed:', error)
  } finally {
    saving.value = false
  }
}
</script>

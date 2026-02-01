/**
 * src/composables/useTheme.ts
 * Zeekr Aesthetics - Theme Core
 * -------------------------------------
 * 管理日间/夜间模式切换。
 * 实现 "无感平滑切换" (Seamless Transition) 算法。
 */

import { useDark, useToggle } from '@vueuse/core'
import { nextTick } from 'vue'

// 1. 初始化暗黑模式状态 (自动读取系统偏好或 LocalStorage)
export const isDark = useDark({
  storageKey: 'NEXUS_THEME', // 保持与 Storage 规范一致
  selector: 'html',
  attribute: 'class',
  valueDark: 'dark',
  valueLight: '',
})

// 2. 获取基础切换函数
const toggleDark = useToggle(isDark)

/**
 * 带有平滑过渡效果的主题切换函数
 * @description 在切换瞬间注入 CSS Transition 类，完成后移除，避免全局性能损耗
 */
export const toggleTheme = async (event?: MouseEvent) => {
  const isAppearanceTransition =
    // @ts-expect-error: View Transitions API 类型兼容
    document.startViewTransition && !window.matchMedia('(prefers-reduced-motion: reduce)').matches

  // 核心 Hack: 注入过渡类名
  document.documentElement.classList.add('theme-transition')

  // 如果浏览器支持 View Transitions API (未来感扩展)
  if (isAppearanceTransition && event) {
    // 这里保留扩展口，未来可做类似 Telegram 的圆形扩散动效
    toggleDark()
  } else {
    // 降级方案：经典平滑过渡
    toggleDark()
  }

  await nextTick()

  // 300ms 对应 CSS 中的 transition-duration，确保动画播放完毕后清理 DOM
  setTimeout(() => {
    document.documentElement.classList.remove('theme-transition')
  }, 300)
}

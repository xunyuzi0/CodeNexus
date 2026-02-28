<template>
  <div
    ref="editorWrapperRef"
    class="flex flex-col w-full h-full rounded-xl overflow-hidden bg-zinc-950/50 backdrop-blur-xl border border-white/5 transition-colors duration-300"
    :class="{ 'ring-1 ring-[#FF4C00]/50': isFocused }"
  >
    <div
      class="flex items-center justify-between px-4 py-2 bg-zinc-900/80 border-b border-white/5 select-none z-10"
    >
      <div class="relative" ref="langDropdownRef">
        <button
          @click="toggleLangDropdown"
          class="flex items-center gap-2 text-xs font-mono font-bold text-zinc-300 hover:text-white transition-colors px-2 py-1.5 rounded hover:bg-white/5"
        >
          <div class="w-2 h-2 rounded-full bg-[#FF4C00]"></div>
          {{ currentLanguage.toUpperCase() }}
          <ChevronDown class="w-3 h-3 text-zinc-500" :class="{ 'rotate-180': isLangOpen }" />
        </button>

        <div
          v-if="isLangOpen"
          class="absolute top-full left-0 mt-2 w-32 bg-zinc-900 border border-zinc-700 rounded-lg shadow-xl py-1 z-50 animate-in fade-in slide-in-from-top-2 duration-200"
        >
          <div
            v-for="lang in availableLanguages"
            :key="lang"
            @click="lang === 'java' ? selectLanguage(lang) : null"
            class="px-3 py-2 text-xs flex items-center justify-between group transition-colors"
            :class="
              lang === 'java' 
                ? 'text-zinc-400 hover:text-white hover:bg-white/5 cursor-pointer' 
                : 'text-zinc-600 cursor-not-allowed bg-zinc-950/30'
            "
          >
            <span class="font-mono" :class="{ 'opacity-50': lang !== 'java' }">{{ lang }}</span>
            
            <Check v-if="currentLanguage === lang" class="w-3 h-3 text-[#FF4C00]" />
            <Lock v-else-if="lang !== 'java'" class="w-3 h-3 text-zinc-700 group-hover:text-zinc-600 transition-colors" />
          </div>
        </div>
      </div>

      <div class="flex items-center gap-1">
        <div
          v-if="readOnly"
          class="flex items-center gap-1.5 px-3 py-1 rounded-full bg-amber-500/10 border border-amber-500/20 text-amber-500 text-[10px] font-bold tracking-wider mr-2"
        >
          <Lock class="w-3 h-3" />
          <span>READ ONLY</span>
        </div>

        <button
          @click="copyCode"
          class="p-1.5 text-zinc-500 hover:text-white hover:bg-white/5 rounded transition-colors group relative"
          title="复制"
        >
          <Copy class="w-4 h-4" />
        </button>
        
        <button
          @click="$emit('toggle-maximize')"
          class="p-1.5 text-zinc-500 hover:text-white hover:bg-white/5 rounded transition-colors"
          :title="isMaximized ? '退出最大化' : '最大化代码区'"
        >
          <Minimize2 v-if="isMaximized" class="w-4 h-4" />
          <Maximize2 v-else class="w-4 h-4" />
        </button>
      </div>
    </div>

    <div class="relative flex-1 w-full min-h-0 overflow-hidden group">
      <div
        v-if="loading"
        class="absolute inset-0 z-20 flex items-center justify-center bg-zinc-950/80 backdrop-blur-sm"
      >
        <Loader2 class="w-8 h-8 text-[#FF4C00] animate-spin" />
      </div>

      <div ref="editorContainer" class="w-full h-full"></div>
    </div>

    <div
      class="flex items-center justify-between px-4 py-1 bg-zinc-900/90 border-t border-white/5 text-[10px] font-mono text-zinc-500 select-none"
    >
      <div class="flex gap-4">
        <span>Ln {{ cursorPosition.lineNumber }}, Col {{ cursorPosition.column }}</span>
        <span>{{ modelValue.length }} Chars</span>
      </div>
      <div class="flex items-center gap-2">
        <span
          class="w-1.5 h-1.5 rounded-full bg-zinc-600"
          :class="{ 'bg-emerald-500': !readOnly }"
        ></span>
        <span>{{ readOnly ? 'Locked' : 'Ready' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as monaco from 'monaco-editor'
import { useSettingsStore } from '@/stores/settings'
import { Copy, ChevronDown, Check, Loader2, Lock, Maximize2, Minimize2 } from 'lucide-vue-next'
import { useClipboard, onClickOutside } from '@vueuse/core'

interface Props {
  modelValue: string
  language?: string
  readOnly?: boolean
  loading?: boolean
  isMaximized?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  language: 'java',
  readOnly: false,
  loading: false,
  isMaximized: false
})

const emit = defineEmits(['update:modelValue', 'change', 'toggle-maximize'])

const settingsStore = useSettingsStore()
const editorContainer = ref<HTMLElement | null>(null)
let editorInstance: monaco.editor.IStandaloneCodeEditor | null = null

const isFocused = ref(false)
const cursorPosition = ref({ lineNumber: 1, column: 1 })
const currentLanguage = ref(props.language)
const { copy } = useClipboard()

const isLangOpen = ref(false)
const langDropdownRef = ref(null)
const availableLanguages = ['java', 'python', 'cpp', 'javascript', 'go']

onClickOutside(langDropdownRef, () => (isLangOpen.value = false))

onMounted(() => {
  if (!editorContainer.value) return

  monaco.editor.defineTheme('zeekr-dark', {
    base: 'vs-dark',
    inherit: true,
    rules: [
      { token: 'comment', foreground: '606060', fontStyle: 'italic' },
      { token: 'keyword', foreground: 'FF4C00', fontStyle: 'bold' },
      { token: 'string', foreground: '10B981' }, 
      { token: 'number', foreground: '3B82F6' },
    ],
    colors: {
      'editor.background': '#00000000', 
      'editor.foreground': '#D4D4D8', 
      'editor.lineHighlightBackground': '#FFFFFF05',
      'editorCursor.foreground': '#FF4C00',
      'editorLineNumber.foreground': '#52525b', 
      'editor.selectionBackground': '#FF4C0033', 
      'editorIndentGuide.background': '#27272a',
      'editorIndentGuide.activeBackground': '#52525b',
    },
  })

  editorInstance = monaco.editor.create(editorContainer.value, {
    value: props.modelValue,
    language: props.language,
    theme: 'zeekr-dark',
    readOnly: props.readOnly,
    fontSize: settingsStore.editorFontSize || 14,
    fontFamily: "'JetBrains Mono', 'Fira Code', Consolas, monospace",
    automaticLayout: true,
    scrollBeyondLastLine: false,
    minimap: { enabled: false }, 
    lineNumbersMinChars: 3,
    padding: { top: 16, bottom: 16 },
    cursorBlinking: 'smooth',
    cursorSmoothCaretAnimation: 'on',
    smoothScrolling: true,
    scrollbar: {
      useShadows: false,
      vertical: 'visible',
      horizontal: 'visible',
      verticalScrollbarSize: 10,
      horizontalScrollbarSize: 10,
      verticalSliderSize: 6, 
      horizontalSliderSize: 6,
    },
  })

  editorInstance.onDidChangeModelContent(() => {
    const value = editorInstance?.getValue() || ''
    emit('update:modelValue', value)
    emit('change', value)
  })

  editorInstance.onDidChangeCursorPosition((e) => {
    cursorPosition.value = {
      lineNumber: e.position.lineNumber,
      column: e.position.column,
    }
  })

  editorInstance.onDidFocusEditorText(() => (isFocused.value = true))
  editorInstance.onDidBlurEditorText(() => (isFocused.value = false))
})

watch(() => props.modelValue, (newValue) => {
  if (editorInstance && newValue !== editorInstance.getValue()) {
    editorInstance.setValue(newValue)
  }
})

watch(() => settingsStore.editorFontSize, (newSize) => {
  editorInstance?.updateOptions({ fontSize: newSize })
})

watch(() => props.language, (newLang) => {
  if (editorInstance && newLang) {
    monaco.editor.setModelLanguage(editorInstance.getModel()!, newLang)
    currentLanguage.value = newLang
  }
})

watch(() => props.readOnly, (val) => {
  editorInstance?.updateOptions({ readOnly: val })
})

const toggleLangDropdown = () => (isLangOpen.value = !isLangOpen.value)

const selectLanguage = (lang: string) => {
  currentLanguage.value = lang
  monaco.editor.setModelLanguage(editorInstance!.getModel()!, lang)
  isLangOpen.value = false
}

const copyCode = () => {
  copy(props.modelValue)
}

onUnmounted(() => {
  if (editorInstance) editorInstance.dispose()
})
</script>

<style>
.monaco-editor .scroll-decoration {
  box-shadow: none !important;
}
.monaco-editor .slider {
  background: #3f3f46 !important; 
  border-radius: 9999px !important;
}
.monaco-editor .slider:hover {
  background: #52525b !important; 
}
</style>
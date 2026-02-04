<template>
  <Teleport to="body">
    <Transition name="dialog">
      <div
        v-if="modelValue"
        class="fixed inset-0 z-[100] flex items-start justify-end p-4 sm:p-6 pointer-events-none"
      >
        <div class="absolute inset-0 bg-transparent pointer-events-auto" @click="close"></div>

        <div
          class="pointer-events-auto w-full max-w-sm bg-[#09090b] border border-white/10 rounded-2xl shadow-2xl overflow-hidden flex flex-col mt-14 mr-0 sm:mr-4 origin-top-right backdrop-blur-xl"
          v-motion
          :initial="{ opacity: 0, scale: 0.95, y: -10 }"
          :enter="{ opacity: 1, scale: 1, y: 0, transition: { duration: 200, type: 'spring' } }"
          @click.stop
        >
          <div
            class="px-5 py-4 border-b border-white/5 bg-zinc-900/50 flex items-center justify-between"
          >
            <h3 class="text-sm font-bold text-white tracking-wide">快速设置</h3>
            <button @click="close" class="text-zinc-500 hover:text-white transition-colors">
              <X class="w-4 h-4" />
            </button>
          </div>

          <div class="p-5 space-y-6 bg-black/40">
            <div class="space-y-3">
              <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider"
                >界面语言</label
              >
              <div
                class="flex items-center justify-between p-3 bg-zinc-900/50 rounded-lg border border-white/5"
              >
                <span class="text-sm text-zinc-200 font-medium">简体中文</span>
                <span
                  class="text-[10px] bg-zinc-800 text-zinc-500 px-2 py-0.5 rounded border border-white/5"
                  >Default</span
                >
              </div>
            </div>

            <div class="space-y-3">
              <div class="flex items-center justify-between">
                <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider"
                  >编辑器字号</label
                >
                <span class="text-xs font-mono text-[#FF4C00]"
                  >{{ settingsStore.settings.editorFontSize }}px</span
                >
              </div>

              <div class="relative w-full h-6 flex items-center">
                <input
                  type="range"
                  min="12"
                  max="24"
                  step="1"
                  v-model.number="settingsStore.settings.editorFontSize"
                  class="w-full h-1.5 bg-zinc-800 rounded-lg appearance-none cursor-pointer accent-[#FF4C00] hover:accent-[#ff6b2b] transition-all"
                />
              </div>

              <div
                class="w-full bg-black/40 border border-white/5 rounded-lg p-3 overflow-hidden transition-all duration-200"
              >
                <div
                  class="font-mono leading-relaxed transition-all duration-200"
                  :style="{ fontSize: settingsStore.settings.editorFontSize + 'px' }"
                >
                  <span class="text-pink-500">public</span> <span class="text-blue-400">class</span>
                  <span class="text-yellow-200">Hello</span> {<br />
                  &nbsp;&nbsp;<span class="text-blue-400">void</span>
                  <span class="text-yellow-200">world</span>() {<br />
                  &nbsp;&nbsp;&nbsp;&nbsp;<span class="text-zinc-400">// CodeNexus</span><br />
                  &nbsp;&nbsp;}<br />
                  }
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { X } from 'lucide-vue-next'
import { useSettingsStore } from '@/stores/settings'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits(['update:modelValue'])
const settingsStore = useSettingsStore()

const close = () => {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.dialog-enter-active,
.dialog-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}
.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

/* Slider Styling refinement */
input[type='range']::-webkit-slider-thumb {
  -webkit-appearance: none;
  height: 16px;
  width: 16px;
  border-radius: 50%;
  background: #ff4c00;
  cursor: pointer;
  margin-top: -6px;
  box-shadow: 0 0 10px rgba(255, 76, 0, 0.5);
  transition: transform 0.1s;
}
input[type='range']::-webkit-slider-thumb:hover {
  transform: scale(1.2);
}
input[type='range']::-webkit-slider-runnable-track {
  width: 100%;
  height: 4px;
  cursor: pointer;
  background: #27272a;
  border-radius: 2px;
}
</style>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div
        v-if="modelValue"
        class="fixed inset-0 z-[100] flex items-center justify-center bg-zinc-50/80 dark:bg-black/80 backdrop-blur-sm px-4"
        @click="close"
      >
        <div
          class="w-full max-w-md p-8 rounded-3xl bg-white dark:bg-[#09090b] border border-zinc-200 dark:border-white/10 shadow-xl dark:shadow-[0_0_50px_rgba(0,0,0,0.8)] relative overflow-hidden"
          v-motion
          :initial="{ opacity: 0, scale: 0.95, y: 20 }"
          :enter="{
            opacity: 1,
            scale: 1,
            y: 0,
            transition: { type: 'spring', stiffness: 300, damping: 25 },
          }"
          @click.stop
        >
          <div
            class="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-[#FF4C00] to-transparent opacity-50"
          ></div>
          <div
            class="absolute -top-20 -right-20 w-40 h-40 bg-[#FF4C00]/10 blur-[50px] rounded-full pointer-events-none"
          ></div>

          <div class="relative z-10 mb-6 text-center">
            <h3 class="text-2xl font-bold text-zinc-900 dark:text-white mb-2">{{ title }}</h3>
          </div>

          <div class="relative z-10 mb-8">
            <slot></slot>
          </div>

          <div class="relative z-10 flex items-center gap-4">
            <button
              @click="close"
              class="flex-1 py-3 rounded-xl border border-zinc-200 dark:border-white/10 text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-white hover:bg-zinc-50 dark:hover:bg-white/5 transition-colors text-sm font-medium"
            >
              {{ cancelText }}
            </button>
            <button
              @click="confirm"
              :disabled="confirmDisabled"
              class="flex-1 py-3 rounded-xl bg-[#FF4C00] text-white font-bold text-sm tracking-wide shadow-[0_0_20px_rgba(255,76,0,0.3)] hover:bg-[#ff5f1f] hover:shadow-[0_0_30px_rgba(255,76,0,0.5)] transition-all transform active:scale-95 flex items-center justify-center gap-2 group disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
            >
              <span>{{ confirmText }}</span>
              <slot name="icon">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  class="w-4 h-4 group-hover:translate-x-1 transition-transform"
                >
                  <path d="M5 12h14" />
                  <path d="m12 5 7 7-7 7" />
                </svg>
              </slot>
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    modelValue: boolean
    title: string
    cancelText?: string
    confirmText?: string
    confirmDisabled?: boolean
  }>(),
  {
    cancelText: '取消',
    confirmText: '确定',
    confirmDisabled: false,
  },
)

// 开放 cancel 事件给外层捕获
const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const close = () => {
  emit('update:modelValue', false)
  emit('cancel')
}

const confirm = () => {
  if (!props.confirmDisabled) {
    emit('confirm')
  }
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

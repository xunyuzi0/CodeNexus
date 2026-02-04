<template>
  <div class="absolute top-6 left-6 z-50">
    <button
      @click="handleClick"
      class="group flex items-center gap-2 px-3 py-2 rounded-lg text-zinc-500 hover:text-white hover:bg-white/5 transition-all duration-300"
    >
      <ArrowLeft class="w-4 h-4 transition-transform group-hover:-translate-x-1" />
      <span class="text-xs font-mono font-bold tracking-widest">EXIT</span>
    </button>

    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="showDialog"
          class="fixed inset-0 z-[100] flex items-center justify-center bg-black/60 backdrop-blur-[2px]"
          @click.self="showDialog = false"
        >
          <div
            class="w-full max-w-md mx-4 p-6 rounded-3xl bg-[#09090b] border border-white/10 shadow-[0_0_50px_rgba(0,0,0,0.8)]"
            v-motion
            :initial="{ opacity: 0, scale: 0.95, y: 10 }"
            :enter="{ opacity: 1, scale: 1, y: 0 }"
          >
            <div class="flex flex-col gap-2 mb-6">
              <h3 class="text-xl font-bold text-white flex items-center gap-2">
                <AlertTriangle class="w-5 h-5 text-red-500" />
                确认离开战场?
              </h3>
              <p class="text-sm text-zinc-500 leading-relaxed">
                当前对局将<span class="text-red-400/80">立即结束</span
                >。未保存的代码进度将会丢失，排位积分可能会受到惩罚。
              </p>
            </div>

            <div class="flex items-center justify-end gap-3">
              <button
                @click="showDialog = false"
                class="px-4 py-2 rounded-xl text-sm font-medium text-zinc-400 hover:text-white hover:bg-white/5 transition-colors"
              >
                取消
              </button>
              <button
                @click="confirmExit"
                class="px-4 py-2 rounded-xl text-sm font-bold bg-red-600 hover:bg-red-500 text-white shadow-[0_0_15px_rgba(220,38,38,0.4)] transition-all transform active:scale-95"
              >
                确认离开
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, AlertTriangle } from 'lucide-vue-next'

const props = withDefaults(
  defineProps<{
    needsConfirm?: boolean
  }>(),
  {
    needsConfirm: false,
  },
)

const router = useRouter()
const showDialog = ref(false)

const handleClick = () => {
  if (props.needsConfirm) {
    showDialog.value = true
  } else {
    goBack()
  }
}

const confirmExit = () => {
  showDialog.value = false
  goBack()
}

const goBack = () => {
  // 统一返回竞技场入口，而不是 Dashboard
  router.push('/arena')
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

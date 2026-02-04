<template>
  <div
    class="relative min-h-screen flex flex-col items-center bg-black overflow-hidden selection:bg-[#FF4C00] selection:text-white"
  >
    <div
      class="absolute inset-0 bg-[linear-gradient(to_right,#80808012_1px,transparent_1px),linear-gradient(to_bottom,#80808012_1px,transparent_1px)] bg-[size:40px_40px] [mask-image:radial-gradient(ellipse_80%_50%_at_50%_0%,#000_70%,transparent_110%)] pointer-events-none"
    ></div>

    <div class="w-full relative z-20 h-20 flex items-center px-6">
      <ArenaExitButton />
      <div v-if="!isRankedMode" class="absolute left-1/2 -translate-x-1/2">
        <div
          class="flex items-center gap-3 bg-zinc-900/80 border border-white/5 px-4 py-2 rounded-full cursor-pointer hover:border-[#FF4C00]/50 transition-colors group"
          @click="copyRoomId"
        >
          <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
          <span class="text-zinc-400 text-xs font-bold tracking-wider">ROOM ID:</span>
          <span
            class="text-white font-mono font-bold text-lg tracking-widest group-hover:text-[#FF4C00] transition-colors"
            >{{ roomId }}</span
          >
          <Copy class="w-3 h-3 text-zinc-600 ml-1 group-hover:text-white transition-colors" />
        </div>
      </div>
      <div class="ml-auto"></div>
    </div>

    <div class="flex-1 flex w-full max-w-6xl items-center justify-center relative z-10 mt-[-40px]">
      <div
        class="flex-1 flex flex-col items-center justify-center"
        v-motion
        :initial="{ opacity: 0, x: -50 }"
        :enter="{ opacity: 1, x: 0 }"
      >
        <div class="relative group">
          <div
            class="absolute inset-0 bg-[#FF4C00]/20 blur-[40px] rounded-full group-hover:bg-[#FF4C00]/30 transition-all duration-500"
          ></div>
          <div
            class="relative w-32 h-32 md:w-40 md:h-40 rounded-full border-2 border-[#FF4C00] p-1 shadow-[0_0_20px_#FF4C00] transition-transform duration-500 group-hover:scale-105"
          >
            <img
              :src="userStore.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'"
              class="w-full h-full rounded-full object-cover bg-zinc-900"
            />
            <div
              v-if="!isRankedMode"
              class="absolute -bottom-3 left-1/2 -translate-x-1/2 bg-[#FF4C00] text-black text-[10px] font-black px-3 py-0.5 rounded-sm uppercase tracking-widest shadow-lg"
            >
              HOST
            </div>
          </div>
        </div>
        <h2 class="mt-6 text-2xl font-bold text-white">{{ userStore.nickname || 'Commander' }}</h2>
        <p class="text-[#FF4C00] font-mono text-sm tracking-widest mt-1">READY</p>
      </div>

      <div class="shrink-0 relative mx-12 md:mx-20 flex flex-col items-center min-w-[150px]">
        <div
          class="text-[120px] md:text-[180px] font-black italic leading-none text-transparent bg-clip-text bg-gradient-to-b from-white to-zinc-800 tracking-tighter opacity-20 select-none pr-4 md:pr-8"
        >
          VS
        </div>
      </div>

      <div class="flex-1 flex flex-col items-center justify-center">
        <div
          v-if="!opponent"
          class="flex flex-col items-center relative"
          v-motion
          :initial="{ opacity: 0 }"
          :enter="{ opacity: 1 }"
        >
          <div
            class="w-32 h-32 md:w-40 md:h-40 rounded-full border border-dashed border-zinc-700 flex items-center justify-center relative overflow-hidden bg-black/40"
          >
            <div
              class="absolute inset-0 bg-gradient-to-br from-transparent via-transparent to-white/10 animate-[spin_4s_linear_infinite]"
            ></div>
            <span class="text-zinc-600 font-mono text-xs z-10 animate-pulse">SCANNING...</span>
          </div>
          <h2 class="mt-6 text-xl font-bold text-zinc-600">等待加入...</h2>
        </div>

        <div
          v-else
          class="flex flex-col items-center"
          v-motion
          :initial="{ opacity: 0, scale: 1.5, filter: 'blur(10px)' }"
          :enter="{
            opacity: 1,
            scale: 1,
            filter: 'blur(0px)',
            transition: { type: 'spring', stiffness: 200, damping: 20 },
          }"
        >
          <div class="relative group">
            <div class="absolute inset-0 bg-white/10 blur-[40px] rounded-full"></div>
            <div
              class="relative w-32 h-32 md:w-40 md:h-40 rounded-full border-2 border-zinc-400 p-1 shadow-[0_0_20px_rgba(255,255,255,0.2)]"
            >
              <img
                :src="opponent.avatar"
                class="w-full h-full rounded-full object-cover bg-zinc-900 grayscale-[0.5]"
              />
              <div
                class="absolute -bottom-3 left-1/2 -translate-x-1/2 bg-zinc-200 text-black text-[10px] font-black px-3 py-0.5 rounded-sm uppercase tracking-widest shadow-lg"
              >
                CHALLENGER
              </div>
            </div>
          </div>
          <h2 class="mt-6 text-2xl font-bold text-white">{{ opponent.name }}</h2>
          <p class="text-emerald-500 font-mono text-sm tracking-widest mt-1">CONNECTED</p>
        </div>
      </div>
    </div>

    <div class="absolute bottom-12 w-full flex justify-center z-20">
      <div v-if="!starting">
        <button
          @click="startBattle"
          :disabled="!opponent"
          class="group relative px-12 py-4 bg-zinc-900 border border-white/10 rounded-xl overflow-hidden disabled:opacity-50 disabled:cursor-not-allowed transition-all hover:border-[#FF4C00]/50"
        >
          <div
            class="absolute inset-0 bg-[#FF4C00] translate-y-[101%] group-hover:translate-y-0 transition-transform duration-300 ease-out"
            v-if="opponent"
          ></div>
          <span
            class="relative z-10 font-black text-lg tracking-[0.2em] uppercase transition-colors group-hover:text-black"
            :class="opponent ? 'text-white' : 'text-zinc-600'"
          >
            {{ opponent ? 'Start Battle' : 'Waiting...' }}
          </span>
        </button>
      </div>
      <div v-else class="text-[#FF4C00] font-mono text-sm tracking-[0.5em] animate-pulse z-[60]">
        INITIALIZING SANDBOX ENVIRONMENT...
      </div>
    </div>

    <div
      class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 transition-opacity duration-500 pointer-events-none flex items-center justify-center"
      :class="starting ? 'opacity-100' : 'opacity-0'"
    ></div>

    <div
      v-if="starting"
      class="fixed inset-0 flex items-center justify-center z-[100] pointer-events-none"
    >
      <div
        class="text-[12rem] md:text-[16rem] font-black text-[#FF4C00] font-mono animate-ping drop-shadow-[0_0_50px_rgba(255,76,0,0.8)]"
      >
        {{ countdownNum }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useClipboard } from '@vueuse/core'
import { useUserStore } from '@/stores/user'
import { Copy } from 'lucide-vue-next'
import ArenaExitButton from '@/components/arena/ArenaExitButton.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const roomId = route.params.roomId as string
const { copy } = useClipboard()
const isRankedMode = computed(() => route.query.mode === 'RANKED')

const opponent = ref<any>(null)
const starting = ref(false)
const countdownNum = ref(3)

const copyRoomId = () => {
  copy(roomId)
}

onMounted(() => {
  const delay = isRankedMode.value ? 800 : 2000
  setTimeout(() => {
    opponent.value = {
      name: isRankedMode.value ? 'DeepSeeker_AI' : 'AlgorithmMaster_99',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=John',
    }
  }, delay)
})

const startBattle = () => {
  if (!opponent.value) return

  starting.value = true

  // 3, 2, 1 每一秒都清晰展示
  const timer = setInterval(() => {
    countdownNum.value--
    if (countdownNum.value <= 0) {
      clearInterval(timer)
      router.push({
        name: 'ArenaBattle',
        params: { roomId },
        query: { mode: isRankedMode.value ? 'RANKED' : undefined },
      })
    }
  }, 1000)
}
</script>

<style scoped>
@keyframes scan {
  0%,
  100% {
    top: 0%;
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    top: 100%;
    opacity: 0;
  }
}
</style>

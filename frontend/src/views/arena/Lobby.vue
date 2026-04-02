<template>
  <div
    class="h-screen w-full bg-[#050505] text-white flex flex-col relative overflow-hidden select-none"
  >
    <div
      v-if="isVerifying"
      class="absolute inset-0 z-50 bg-[#050505] flex flex-col items-center justify-center"
    >
      <Loader2 class="w-10 h-10 text-[#FF4C00] animate-spin mb-4" />
      <p class="text-zinc-500 font-mono tracking-[0.2em] animate-pulse">正在核验安全凭证...</p>
    </div>

    <header
      class="h-16 flex items-center justify-between px-4 border-b border-white/5 bg-zinc-950/80 backdrop-blur-md relative z-20"
    >
      <div class="flex items-center gap-3">
        <ArenaExitButton @click="handleLeaveRoom" />
      </div>
    </header>

    <div class="flex-1 flex items-center justify-center relative z-10 px-8">
      <div
        class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 flex flex-col items-center z-30"
      >
        <div
          class="w-[1px] h-32 bg-gradient-to-b from-transparent via-[#FF4C00]/50 to-transparent mb-4"
        ></div>
        <div
          class="w-16 h-16 rounded-full bg-zinc-950 border border-white/10 flex items-center justify-center shadow-[0_0_30px_rgba(255,76,0,0.15)]"
        >
          <Swords class="w-6 h-6 text-[#FF4C00]" />
        </div>
        <div
          class="w-[1px] h-32 bg-gradient-to-t from-transparent via-[#FF4C00]/50 to-transparent mt-4"
        ></div>
      </div>

      <div class="w-full max-w-6xl flex items-center justify-between gap-12">
        <div
          class="flex-1 flex flex-col items-center transform transition-transform duration-500 hover:scale-105"
        >
          <div class="relative w-48 h-48 mb-6">
            <div
              class="absolute inset-0 rounded-full border-4 transition-colors duration-300"
              :class="
                myStatus.isReady
                  ? 'border-emerald-500 shadow-[0_0_40px_rgba(16,185,129,0.3)]'
                  : 'border-zinc-800'
              "
            ></div>
            <img
              :src="userStore.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Me'"
              class="w-full h-full rounded-full object-cover p-2 bg-zinc-950"
            />
            <div
              v-if="myStatus.isReady"
              class="absolute bottom-2 right-2 w-8 h-8 rounded-full bg-emerald-500 border-4 border-zinc-950 flex items-center justify-center"
            >
              <Check class="w-4 h-4 text-white" />
            </div>
          </div>
          <h3 class="text-2xl font-bold tracking-tight mb-2">
            {{ userStore.nickname || '我方特工' }}
          </h3>
          <p class="text-zinc-500 font-mono text-xs tracking-widest uppercase">
            {{ myStatus.isReady ? '已准备' : '待命中' }}
          </p>
        </div>

        <div
          class="flex-1 flex flex-col items-center transform transition-transform duration-500 hover:scale-105"
        >
          <div class="relative w-48 h-48 mb-6">
            <div
              class="absolute inset-0 rounded-full border-4 transition-colors duration-300"
              :class="
                !opponent
                  ? 'border-dashed border-zinc-800 animate-spin-slow'
                  : opponent.isReady
                    ? 'border-red-500 shadow-[0_0_40px_rgba(239,68,68,0.3)]'
                    : 'border-zinc-800'
              "
            ></div>

            <template v-if="opponent">
              <img
                :src="opponent.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Enemy'"
                class="w-full h-full rounded-full object-cover p-2 bg-zinc-950"
              />
              <div
                v-if="opponent.isReady"
                class="absolute bottom-2 left-2 w-8 h-8 rounded-full bg-red-500 border-4 border-zinc-950 flex items-center justify-center"
              >
                <Check class="w-4 h-4 text-white" />
              </div>
            </template>
            <div
              v-else
              class="w-full h-full flex items-center justify-center rounded-full p-2 bg-zinc-950"
            >
              <UserMinus class="w-12 h-12 text-zinc-800" />
            </div>
          </div>

          <h3
            class="text-2xl font-bold tracking-tight mb-2"
            :class="opponent ? 'text-white' : 'text-zinc-600'"
          >
            {{ opponent ? opponent.name : '等待对手接入...' }}
          </h3>
          <p class="text-zinc-500 font-mono text-xs tracking-widest uppercase">
            <span v-if="!opponent" class="animate-pulse">正在扫描网络...</span>
            <span v-else>{{ opponent.isReady ? '已准备' : '待命中' }}</span>
          </p>
        </div>
      </div>
    </div>

    <div
      class="h-32 shrink-0 flex items-center justify-center relative z-20 border-t border-white/5 bg-zinc-950/80 backdrop-blur-md"
    >
      <button
        @click="toggleReady"
        :disabled="!opponent"
        class="relative px-16 py-4 rounded-xl font-bold text-lg tracking-widest uppercase transition-all duration-300 overflow-hidden group disabled:opacity-40 disabled:cursor-not-allowed"
        :class="
          myStatus.isReady
            ? 'bg-zinc-800 text-zinc-300 border border-white/10'
            : 'bg-[#FF4C00] text-white shadow-[0_0_20px_rgba(255,76,0,0.4)] hover:shadow-[0_0_40px_rgba(255,76,0,0.6)]'
        "
      >
        <span class="relative z-10 flex items-center gap-2">
          <Power class="w-5 h-5" />
          {{ myStatus.isReady ? '取消准备' : '准备战斗' }}
        </span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Swords, UserMinus, Check, Power, Loader2 } from 'lucide-vue-next'
// 引入 ArenaExitButton
import ArenaExitButton from '@/components/arena/ArenaExitButton.vue'
import { useUserStore } from '@/stores/user'
import { checkRoomValidity } from '@/api/arena'
import { BattleWebSocket } from '@/utils/battle-ws'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const roomCode = ref(route.params.roomId as string)
// 从地址栏拿出门票
const ticket = ref(route.query.ticket as string)

const isVerifying = ref(true)

const myStatus = reactive({
  isReady: false,
})

const opponent = ref<any>(null)
let battleWs: BattleWebSocket | null = null

const initLobby = async () => {
  if (!roomCode.value) {
    router.replace('/arena')
    return
  }

  try {
    // 验票，携带 ticket
    const validity = await checkRoomValidity(roomCode.value, ticket.value)

    // 【核心修复】：判断 isValid
    if (!validity.isValid) {
      alert(validity.message || '房间已失效或门票无效')
      router.replace('/arena')
      return
    }

    isVerifying.value = false
    setupWebSocket()
  } catch (err) {
    console.error('房间校验异常:', err)
    router.replace('/arena')
  }
}

const setupWebSocket = () => {
  const token = (userStore as any).token || userStore.token
  if (!token) {
    router.replace('/login')
    return
  }

  battleWs = new BattleWebSocket(roomCode.value, token)

  battleWs.on('SYNC_ROOM', (data: any) => {
    const enemy = data.players?.find((p: any) => p.userId !== userStore.userInfo?.id)
    if (enemy) {
      opponent.value = {
        id: enemy.userId,
        name: enemy.nickname || enemy.userName || 'Unknown Player',
        avatar: enemy.avatarUrl || enemy.userAvatar,
        isReady: enemy.isReady,
      }
    }
  })

  battleWs.on('PLAYER_JOINED', (data: any) => {
    if (data.userId !== userStore.userInfo?.id) {
      opponent.value = {
        id: data.userId,
        name: data.nickname || data.userName,
        avatar: data.avatarUrl || data.userAvatar,
        isReady: false,
      }
    }
  })

  battleWs.on('PLAYER_READY', (data: any) => {
    if (data.userId === userStore.userInfo?.id) {
      myStatus.isReady = data.isReady
    } else if (opponent.value && opponent.value.id === data.userId) {
      opponent.value.isReady = data.isReady
    }
  })

  battleWs.on('GAME_START', (data: any) => {
    setTimeout(() => {
      router.replace({
        path: `/battle/room/${roomCode.value}`,
        query: {
          problemId: data.problemId,
          startTime: data.startTime,
        },
      })
    }, 1000)
  })

  battleWs.connect()
}

const toggleReady = () => {
  if (!battleWs) return
  battleWs.send('READY', { isReady: !myStatus.isReady })
}

const handleLeaveRoom = () => {
  if (battleWs) {
    battleWs.disconnect()
  }
  router.replace('/arena')
}

onMounted(() => {
  initLobby()
})

onBeforeUnmount(() => {
  if (battleWs) {
    battleWs.disconnect()
  }
})
</script>

<style scoped>
.animate-spin-slow {
  animation: spin 8s linear infinite;
}
</style>

<template>
  <div v-if="isLoading" class="h-screen w-full bg-black flex items-center justify-center font-sans">
    <div class="flex flex-col items-center gap-6">
      <div class="relative w-16 h-16">
        <div class="absolute inset-0 rounded-full border-4 border-zinc-800"></div>
        <div class="absolute inset-0 rounded-full border-4 border-t-[#FF4C00] animate-spin"></div>
        <Loader2 class="absolute inset-0 m-auto w-6 h-6 text-[#FF4C00] animate-pulse" />
      </div>
      <div class="text-center space-y-2">
        <h3 class="text-white font-bold tracking-wider text-lg">正在同步战场数据</h3>
        <p class="text-zinc-500 font-mono text-xs tracking-[0.2em] uppercase animate-pulse">
          Synchronizing Absolute Time...
        </p>
      </div>
    </div>
  </div>

  <div v-else class="h-screen w-full bg-zinc-950 text-white flex flex-col overflow-hidden relative">
    <div class="absolute inset-0 bg-grid-white/[0.02] pointer-events-none"></div>
    <div
      class="absolute top-0 right-0 w-[500px] h-[500px] bg-[#FF4C00]/10 blur-[120px] rounded-full pointer-events-none transition-opacity duration-1000"
      :class="battleStatus === 'FIGHTING' ? 'opacity-100' : 'opacity-0'"
    ></div>

    <Transition name="toast-slide">
      <div
        v-if="toast.show"
        class="fixed top-20 left-1/2 -translate-x-1/2 z-[300] flex items-center gap-2 px-5 py-3 rounded-full shadow-2xl backdrop-blur-xl border border-white/10"
        :class="toast.type === 'error' ? 'bg-red-500/20 text-red-100' : 'bg-zinc-800/80 text-white'"
      >
        <component
          :is="toast.icon"
          class="w-4 h-4"
          :class="toast.type === 'error' ? 'text-red-400' : 'text-emerald-400'"
        />
        <span class="text-sm font-bold tracking-wide">{{ toast.message }}</span>
      </div>
    </Transition>

    <header
      class="h-14 shrink-0 flex items-center justify-between px-4 bg-zinc-950/80 backdrop-blur-md border-b border-white/5 z-50 relative select-none"
    >
      <div class="flex items-center gap-4">
        <ArenaExitButton @click="handleExitClick" :disabled="isViewingOpponentCode" />
      </div>

      <div
        class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 flex items-center gap-3 px-5 py-1.5 rounded-full backdrop-blur-xl border shadow-lg transition-all duration-500"
        :class="
          timer < 60 && battleStatus === 'FIGHTING'
            ? 'bg-red-500/10 border-red-500/50 shadow-red-500/20 animate-pulse'
            : 'bg-zinc-900/80 border-white/10'
        "
      >
        <Timer class="w-4 h-4" :class="timer < 60 ? 'text-red-500' : 'text-[#FF4C00]'" />
        <span
          class="font-mono text-base font-bold tracking-widest tabular-nums"
          :class="timer < 60 ? 'text-red-500' : 'text-white'"
        >
          {{ formattedTime }}
        </span>
        <div class="w-[1px] h-3 bg-white/20"></div>
        <span class="text-xs font-bold tracking-wider" :class="statusColor">{{ statusText }}</span>
      </div>

      <div class="flex items-center gap-2">
        <div class="relative group cursor-pointer mr-2">
          <div
            class="w-8 h-8 rounded-full bg-zinc-800 border border-white/10 overflow-hidden ring-2 ring-transparent group-hover:ring-[#FF4C00]/20 transition-all"
          >
            <img
              :src="
                (userStore as any).avatar ||
                (userStore.userInfo as any)?.avatarUrl ||
                (userStore.userInfo as any)?.userAvatar ||
                'https://api.dicebear.com/7.x/avataaars/svg?seed=Me'
              "
              alt="User"
              class="w-full h-full object-cover opacity-90 group-hover:opacity-100"
            />
          </div>
          <div
            class="absolute bottom-0 right-0 w-2.5 h-2.5 bg-emerald-500 border-2 border-zinc-950 rounded-full"
          ></div>
        </div>
      </div>
    </header>

    <div class="flex-1 min-h-0 relative z-10 w-full max-w-[1920px] mx-auto">
      <splitpanes class="zeekr-theme" horizontal>
        <pane size="100">
          <splitpanes vertical @resize="handleResize" class="h-full">
            <pane
              v-show="paneSize.left > 0"
              :size="paneSize.left"
              :min-size="maximizedPane !== 'none' ? 0 : 20"
              class="flex flex-col min-w-0 transition-[width] duration-300 ease-in-out bg-zinc-900/40 backdrop-blur-sm border-r border-white/5"
            >
              <ProblemDescription
                v-if="problem"
                :problem="problem"
                :is-maximized="maximizedPane === 'left'"
                :hide-tabs="true"
                @toggle-maximize="toggleMaximizeLeft"
              />
            </pane>

            <pane
              v-show="paneSize.middle > 0"
              :size="paneSize.middle"
              :min-size="maximizedPane !== 'none' ? 0 : 30"
              class="flex flex-col relative min-w-0 transition-[width] duration-300 ease-in-out bg-[#1e1e1e]"
            >
              <CodeWorkspace
                v-model="editorCode"
                :problem="problem"
                :read-only="isViewingOpponentCode || battleStatus !== 'FIGHTING'"
                :is-battle-mode="true"
                :is-maximized="maximizedPane === 'middle'"
                @toggle-maximize="toggleMaximizeMiddle"
                @run-start="handleRunStart"
                @run-success="handleRunSuccess"
                @run-fail="handleRunFail"
                @run-end="handleRunEnd"
                @submit-start="handleSubmitStart"
                @submit-fail="handleSubmitFail"
                @success="handleSuccess"
              />
            </pane>

            <pane
              v-show="paneSize.right > 0"
              :size="paneSize.right"
              :min-size="15"
              class="flex flex-col relative min-w-0 transition-[width] duration-300 ease-in-out bg-[#050505]"
            >
              <BattleConsole
                :players="battlePlayers"
                :logs="battleLogs"
                :room-code="roomCode"
                :is-battle-ended="battleStatus !== 'FIGHTING'"
                @ping="handleTacticalPing"
              />
            </pane>
          </splitpanes>
        </pane>
      </splitpanes>
    </div>

    <Transition name="fade">
      <div
        v-if="isViewingOpponentCode && battleStatus !== 'FIGHTING'"
        class="absolute bottom-10 left-1/2 -translate-x-1/2 z-[200]"
      >
        <Button
          @click="backToSettlement"
          class="bg-zinc-800/90 hover:bg-zinc-700 text-white border border-white/10 shadow-[0_0_30px_rgba(0,0,0,0.8)] backdrop-blur-md rounded-full px-6 py-6 font-bold flex items-center gap-2"
        >
          <RotateCcw class="w-4 h-4" /> 结束观摩，返回结算页面
        </Button>
      </div>
    </Transition>

    <Transition name="fade">
      <div
        v-if="showSettlement"
        class="absolute inset-0 z-[150] flex items-center justify-center bg-black/80 backdrop-blur-md p-6"
      >
        <div
          class="w-full max-w-[460px] rounded-3xl border p-8 flex flex-col items-center text-center shadow-2xl relative overflow-hidden"
          :class="
            battleStatus === 'VICTORY'
              ? 'bg-[#09090b] border-[#FF4C00]/30 shadow-[#FF4C00]/20'
              : 'bg-[#09090b] border-zinc-800'
          "
        >
          <div
            v-if="battleStatus === 'VICTORY'"
            class="absolute top-0 left-1/2 -translate-x-1/2 w-64 h-64 bg-[#FF4C00]/20 blur-[80px] rounded-full pointer-events-none"
          ></div>

          <div class="relative mb-6">
            <Trophy
              v-if="battleStatus === 'VICTORY'"
              class="w-20 h-20 text-[#FF4C00] drop-shadow-[0_0_15px_#FF4C00]"
            />
            <XCircle v-else class="w-20 h-20 text-zinc-600" />
          </div>

          <h2 class="text-4xl font-black italic tracking-tighter text-white mb-2">
            {{ battleStatus === 'VICTORY' ? 'VICTORY' : 'DEFEAT' }}
          </h2>
          <p class="text-zinc-400 font-medium mb-4">
            <template v-if="matchReason === 'ESCAPE'">
              {{
                battleStatus === 'VICTORY'
                  ? '对手已弃权，我方不战而胜！'
                  : '您已弃权逃跑，行动失败！'
              }}
            </template>
            <template v-else>
              {{ battleStatus === 'VICTORY' ? '恭喜！你击败了对手' : '很遗憾，对手抢先一步' }}
            </template>
          </p>

          <div v-if="myScoreChange !== null" class="mb-8 flex flex-col items-center">
            <span class="text-xs text-zinc-500 font-mono tracking-widest uppercase mb-1">
              天梯排位分 (Elo Rating)
            </span>
            <span
              class="text-5xl font-black font-mono tracking-tighter"
              :class="
                myScoreChange >= 0
                  ? 'text-emerald-400 drop-shadow-[0_0_15px_rgba(16,185,129,0.5)]'
                  : 'text-red-500 drop-shadow-[0_0_15px_rgba(239,68,68,0.5)]'
              "
            >
              {{ myScoreChange > 0 ? '+' : '' }}{{ myScoreChange }}
            </span>
          </div>
          <div v-else class="mb-8 h-[88px] flex items-center justify-center">
            <div
              class="w-5 h-5 border-2 border-zinc-700 border-t-[#FF4C00] rounded-full animate-spin"
            ></div>
          </div>

          <div class="flex flex-col gap-3 w-full">
            <Button
              class="w-full bg-[#FF4C00] hover:bg-[#ff5f1f] text-white font-bold h-12 rounded-xl"
              @click="router.replace('/battle/matchmaking')"
            >
              <Swords class="w-4 h-4 mr-2" /> 寻找新匹配
            </Button>

            <div class="grid grid-cols-2 gap-3 mt-1">
              <Button
                variant="ghost"
                class="w-full bg-zinc-800/50 hover:bg-zinc-700 text-zinc-300 h-11 rounded-xl"
                @click="handlePractice"
              >
                <ClipboardCopy class="w-4 h-4 mr-2" /> 单机研究
              </Button>
              <Button
                variant="ghost"
                class="w-full bg-zinc-800/50 hover:bg-zinc-700 text-zinc-300 h-11 rounded-xl transition-all"
                @click="openCollectDialog"
              >
                <Star
                  class="w-4 h-4 mr-2"
                  :class="hasFavorited ? 'fill-yellow-500 text-yellow-500' : ''"
                />
                {{ hasFavorited ? '管理收藏' : '收藏本题' }}
              </Button>
            </div>

            <Button
              variant="ghost"
              class="w-full text-zinc-400 hover:text-white hover:bg-white/5 h-11 rounded-xl mt-1"
              @click="router.replace('/arena')"
            >
              <Home class="w-4 h-4 mr-2" /> 返回竞技大厅
            </Button>

            <Button
              v-if="battleStatus === 'DEFEAT' && matchReason !== 'ESCAPE'"
              variant="outline"
              class="w-full border-zinc-700 text-zinc-300 hover:text-white hover:bg-zinc-800 h-11 rounded-xl mt-2"
              @click="handleViewOpponentCode"
            >
              <Loader2 v-if="isFetchingOpponent" class="w-4 h-4 mr-2 animate-spin" />
              <Eye v-else class="w-4 h-4 mr-2" /> 观摩对手代码
            </Button>
          </div>
        </div>
      </div>
    </Transition>

    <ArenaDialog
      v-model="showExitDialog"
      title="警告：正在进行对局"
      confirm-text="投降并退出"
      @confirm="confirmForceExit"
    >
      <div class="text-center space-y-4">
        <p class="text-zinc-400">
          现在离开将被视为<span class="text-red-500 font-bold">逃跑判负</span
          >，且会扣除排位积分。<br />确认要投降吗？
        </p>
      </div>
    </ArenaDialog>

    <ArenaDialog
      v-model="collectDialog.show"
      title="收藏题目 (可多选)"
      :confirm-text="isSubmitting ? '处理中...' : '保存更改'"
      @confirm="confirmCollect"
    >
      <div class="flex flex-col gap-4 min-h-[150px]">
        <div v-if="collectDialog.loading" class="flex-1 flex items-center justify-center">
          <div
            class="w-6 h-6 border-2 border-zinc-800 border-t-[#FF4C00] rounded-full animate-spin"
          ></div>
        </div>

        <template v-else>
          <div class="flex justify-between items-end">
            <p class="text-xs text-zinc-500">请选择目标文件夹：</p>
          </div>
          <p v-if="errorMessage" class="text-xs text-red-500 font-bold animate-pulse">
            {{ errorMessage }}
          </p>

          <div class="space-y-2 max-h-[200px] overflow-y-auto custom-scrollbar pr-2">
            <div
              v-for="folder in collectDialog.folders"
              :key="folder.id"
              @click="toggleFolderSelection(folder.id)"
              class="flex items-center justify-between p-3 rounded-xl border cursor-pointer transition-all select-none group"
              :class="
                collectDialog.selectedFolderIds.includes(folder.id)
                  ? 'bg-[#FF4C00]/10 border-[#FF4C00] text-white shadow-[0_0_10px_rgba(255,76,0,0.1)]'
                  : 'bg-zinc-900/50 border-white/5 text-zinc-400 hover:bg-white/5 hover:border-white/10'
              "
            >
              <div class="flex items-center gap-3">
                <Folder
                  class="w-4 h-4 transition-colors"
                  :class="
                    collectDialog.selectedFolderIds.includes(folder.id)
                      ? 'text-[#FF4C00]'
                      : 'text-zinc-600 group-hover:text-zinc-400'
                  "
                />
                <span class="text-sm font-medium">{{ folder.name }}</span>
              </div>
              <div class="relative w-4 h-4 flex items-center justify-center">
                <transition name="scale">
                  <div
                    v-if="collectDialog.selectedFolderIds.includes(folder.id)"
                    class="w-4 h-4 rounded bg-[#FF4C00] flex items-center justify-center"
                  >
                    <Check class="w-3 h-3 text-white font-bold" />
                  </div>
                  <div
                    v-else
                    class="w-4 h-4 rounded border border-zinc-700 group-hover:border-zinc-500"
                  ></div>
                </transition>
              </div>
            </div>
            <div
              v-if="collectDialog.folders.length === 0"
              class="text-center text-zinc-600 text-xs py-4"
            >
              暂无收藏夹，请在下方新建一个
            </div>
          </div>

          <div class="w-full h-[1px] bg-white/5 my-1"></div>

          <div class="space-y-2">
            <p class="text-xs text-zinc-500">或新建文件夹：</p>
            <div class="flex gap-2">
              <div class="relative flex-1">
                <Plus class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-500" />
                <input
                  v-model="collectDialog.newFolderName"
                  type="text"
                  :disabled="isSubmitting"
                  placeholder="例如：竞技场复盘录 (回车创建)"
                  class="w-full bg-zinc-900/50 border border-zinc-700 rounded-xl py-2 pl-9 pr-4 text-sm text-white focus:outline-none focus:border-[#FF4C00] transition-colors placeholder-zinc-600 disabled:opacity-50"
                  @keyup.enter="handleCreateFolder"
                />
              </div>
              <button
                @click="handleCreateFolder"
                :disabled="!collectDialog.newFolderName.trim() || isSubmitting"
                class="px-3 py-2 rounded-xl bg-zinc-800 text-xs font-bold text-zinc-400 hover:text-white hover:bg-[#FF4C00] transition-all disabled:opacity-50 disabled:hover:bg-zinc-800 disabled:hover:text-zinc-400"
              >
                创建
              </button>
            </div>
          </div>
        </template>
      </div>
    </ArenaDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router'
import { useSessionStorage } from '@vueuse/core'
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'

import { forceSettleMatch } from '@/api/arena'

import {
  Timer,
  Loader2,
  Trophy,
  XCircle,
  RotateCcw,
  Eye,
  Swords,
  ClipboardCopy,
  Home,
  Star,
  CheckCircle2,
  AlertCircle,
  Folder,
  Plus,
  Check,
} from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import ArenaExitButton from '@/components/arena/ArenaExitButton.vue'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'

import CodeWorkspace from '@/views/problem/components/CodeWorkspace.vue'
import ProblemDescription from '@/views/problem/components/ProblemDescription.vue'
import BattleConsole from '@/components/arena/BattleConsole.vue'

import { useUserStore } from '@/stores/user'
import { getProblemDetail } from '@/api/problem'
import {
  getFolders,
  createFolder,
  addFavoriteProblem,
  removeFavoriteProblem,
  getFolderDetail,
  type FavoriteFolder,
} from '@/api/favorites'
import { getOpponentCode } from '@/api/arena'
import { BattleWebSocket } from '@/utils/battle-ws'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const toast = reactive({
  show: false,
  message: '',
  type: 'success' as 'success' | 'error',
  icon: CheckCircle2,
})
let toastTimer: number | null = null
const showToast = (message: string, type: 'success' | 'error' = 'success') => {
  toast.message = message
  toast.type = type
  toast.icon = type === 'error' ? AlertCircle : CheckCircle2
  toast.show = true
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toast.show = false
  }, 3000)
}

type BattleStatus = 'FIGHTING' | 'VICTORY' | 'DEFEAT'
const BATTLE_DURATION_MS = 30 * 60 * 1000

const roomCode = ref(route.params.roomId as string)
const problemId = ref(route.query.problemId as string)
const absoluteStartTime = ref(Number(route.query.startTime) || Date.now())

const battleStatus = useSessionStorage<BattleStatus>(
  `nexus_battle_status_${roomCode.value}`,
  'FIGHTING',
)
const showSettlement = useSessionStorage<boolean>(
  `nexus_battle_settlement_${roomCode.value}`,
  false,
)
const timer = useSessionStorage<number>(`nexus_battle_timer_${roomCode.value}`, 1800)

const isLoading = ref(true)
const showExitDialog = ref(false)
const hasFavorited = ref(false)

const maximizedPane = ref<'none' | 'left' | 'middle'>('none')
const paneSize = reactive({ left: 25, middle: 55, right: 20 })
const lastSize = reactive({ left: 25, middle: 55, right: 20 })

const editorCode = useSessionStorage(
  `nexus_battle_code_${roomCode.value}`,
  `public class Solution {\n    public void solve() {\n        \n    }\n}`,
)
const isViewingOpponentCode = ref(false)
const isFetchingOpponent = ref(false)

const problem = ref<any>(null)
let battleWs: BattleWebSocket | null = null

const battlePlayers = ref<any[]>([
  {
    id: 'me',
    name: userStore.nickname || '我方',
    avatar:
      (userStore as any).avatar ||
      (userStore.userInfo as any)?.avatarUrl ||
      (userStore.userInfo as any)?.userAvatar,
    status: 'IDLE',
    loc: 0,
    cpm: 0,
    isMe: true,
  },
  { id: 'enemy', name: '连接中...', avatar: '', status: 'IDLE', loc: 0, cpm: 0, isMe: false },
])
const battleLogs = useSessionStorage<any[]>(`nexus_battle_logs_${roomCode.value}`, [])

const myScoreChange = ref<number | null>(null)
// 记录对战特殊原因
const matchReason = ref<string>('')

const formattedTime = computed(() => {
  const m = Math.floor(timer.value / 60)
    .toString()
    .padStart(2, '0')
  const s = (timer.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})
const statusText = computed(() =>
  battleStatus.value === 'FIGHTING'
    ? '绝密对抗中'
    : battleStatus.value === 'VICTORY'
      ? '战斗胜利 (锁定)'
      : '战斗失败 (锁定)',
)
const statusColor = computed(() =>
  battleStatus.value === 'FIGHTING'
    ? 'text-[#FF4C00] animate-pulse'
    : battleStatus.value === 'VICTORY'
      ? 'text-emerald-500'
      : 'text-zinc-500',
)

const collectDialog = reactive({
  show: false,
  loading: false,
  selectedFolderIds: [] as number[],
  newFolderName: '',
  folders: [] as FavoriteFolder[],
})
const isSubmitting = ref(false)
const errorMessage = ref('')
let initialSelectedFolderIds: number[] = []

const openCollectDialog = async () => {
  errorMessage.value = ''
  collectDialog.show = true
  collectDialog.loading = true
  collectDialog.newFolderName = ''
  collectDialog.selectedFolderIds = []
  try {
    const folders = await getFolders()
    collectDialog.folders = folders
    const targetProblemId = Number(problemId.value)
    const initials: number[] = []
    const promises = folders.map((f) => getFolderDetail(f.id))
    const details = await Promise.all(promises)
    details.forEach((d) => {
      if (d.list.some((p) => Number(p.id) === targetProblemId)) initials.push(d.folder.id)
    })
    collectDialog.selectedFolderIds = [...initials]
    initialSelectedFolderIds = [...initials]
    hasFavorited.value = initials.length > 0
  } catch (error) {
    errorMessage.value = '无法获取收藏夹详情，请检查网络'
  } finally {
    collectDialog.loading = false
  }
}
const toggleFolderSelection = (folderId: number) => {
  const index = collectDialog.selectedFolderIds.indexOf(folderId)
  if (index > -1) collectDialog.selectedFolderIds.splice(index, 1)
  else collectDialog.selectedFolderIds.push(folderId)
}
const handleCreateFolder = async () => {
  const name = collectDialog.newFolderName.trim()
  if (!name || isSubmitting.value) return
  isSubmitting.value = true
  errorMessage.value = ''
  try {
    const newFolder = await createFolder(name)
    collectDialog.folders.push(newFolder)
    collectDialog.selectedFolderIds.push(newFolder.id)
    collectDialog.newFolderName = ''
  } catch (error: any) {
    errorMessage.value = error.message || '新建文件夹失败，请重试'
  } finally {
    isSubmitting.value = false
  }
}
const confirmCollect = async () => {
  if (isSubmitting.value) return
  isSubmitting.value = true
  errorMessage.value = ''
  try {
    const targetProblemId = Number(problemId.value)
    const currentSelected = collectDialog.selectedFolderIds
    const toAdd = currentSelected.filter((id) => !initialSelectedFolderIds.includes(id))
    const toRemove = initialSelectedFolderIds.filter((id) => !currentSelected.includes(id))
    const promises: Promise<void>[] = []
    toAdd.forEach((id) => promises.push(addFavoriteProblem(id, targetProblemId)))
    toRemove.forEach((id) => promises.push(removeFavoriteProblem(id, targetProblemId)))
    await Promise.all(promises)
    collectDialog.show = false
    hasFavorited.value = currentSelected.length > 0
    initialSelectedFolderIds = [...currentSelected]
    showToast('收藏设置已完美同步！')
  } catch (error: any) {
    errorMessage.value = '保存收藏状态时发生错误，请稍后重试'
  } finally {
    isSubmitting.value = false
  }
}

const initBattleField = async () => {
  if (!roomCode.value || !problemId.value) {
    showToast('对局参数缺失，正在撤离...', 'error')
    router.replace('/arena')
    return
  }
  try {
    problem.value = await getProblemDetail(problemId.value)
    if (battleStatus.value === 'FIGHTING') {
      setupWebSocket()
      startAbsoluteTimeEngine()
    }
    isLoading.value = false
  } catch (err) {
    showToast('加载战局失败，可能网络波动', 'error')
    router.replace('/arena')
  }
}

let gameTimerInterval: number | null = null
const startAbsoluteTimeEngine = () => {
  const targetEndTime = absoluteStartTime.value + BATTLE_DURATION_MS
  const remainMs = Math.max(0, targetEndTime - Date.now())
  timer.value = Math.floor(remainMs / 1000)
  gameTimerInterval = window.setInterval(() => {
    if (battleStatus.value !== 'FIGHTING') {
      if (gameTimerInterval) clearInterval(gameTimerInterval)
      return
    }
    const currentRemainMs = Math.max(0, targetEndTime - Date.now())
    timer.value = Math.floor(currentRemainMs / 1000)
    if (timer.value <= 0) endGame('DEFEAT', '时间耗尽，行动失败！')
  }, 1000)
}

const setupWebSocket = () => {
  battleWs = new BattleWebSocket(roomCode.value, userStore.token!)

  // 监听后端发来的 SETTLEMENT 事件
  battleWs.on('MATCH_SETTLED' as any, (payload: any) => {
    const myId = userStore.userInfo?.id || (userStore as any).id || (userStore as any).userId
    const isWinner = String(payload.winnerId) === String(myId)
    myScoreChange.value = isWinner ? payload.winnerChange : payload.loserChange

    // 处理逃跑事件的特殊结算视图触发
    if (payload.reason === 'ESCAPE') {
      matchReason.value = 'ESCAPE'
      if (battleStatus.value === 'FIGHTING') {
        endGame(
          isWinner ? 'VICTORY' : 'DEFEAT',
          isWinner ? '对手已弃权，我方不战而胜！' : '我方已弃权',
        )
      }
    }
  })

  battleWs.on('SYNC_ROOM', (data: any) => {
    const myId = userStore.userInfo?.id || (userStore as any).id || (userStore as any).userId
    const enemy = data.players?.find((p: any) => String(p.userId) !== String(myId))
    const enemyPlayer = battlePlayers.value[1]
    if (enemy && enemyPlayer) {
      enemyPlayer.id = enemy.userId
      enemyPlayer.name = enemy.nickname
      enemyPlayer.avatar = enemy.avatarUrl || enemy.userAvatar || ''
    }
  })

  battleWs.on('PLAYER_JOINED', (data: any) => {
    const myId = userStore.userInfo?.id || (userStore as any).id || (userStore as any).userId
    const enemyPlayer = battlePlayers.value[1]
    if (enemyPlayer && String(data.userId) !== String(myId)) {
      enemyPlayer.id = data.userId
      enemyPlayer.name = data.nickname
      enemyPlayer.avatar = data.avatarUrl || data.userAvatar || ''
    }
  })

  battleWs.on('TELEMETRY_SYNC', (data: any) => {
    const enemyPlayer = battlePlayers.value[1]
    const myId = userStore.userInfo?.id || (userStore as any).id || (userStore as any).userId
    if (String(data.userId) !== String(myId) && enemyPlayer) {
      enemyPlayer.loc = data.loc
      enemyPlayer.cpm = data.cpm
      if (enemyPlayer.status === 'IDLE' || enemyPlayer.status === 'TYPING')
        enemyPlayer.status = data.cpm > 0 ? 'TYPING' : 'IDLE'
    }
  })

  battleWs.on('BATTLE_LOG', (payload: any) => {
    battleLogs.value.push({
      id: Date.now(),
      timestamp: payload.timestamp || getNowTime(),
      prefix: payload.logType || payload.level || 'INFO',
      message: payload.message,
    })
    const enemyPlayer = battlePlayers.value[1]
    const myId = userStore.userInfo?.id || (userStore as any).id || (userStore as any).userId

    if (payload.userId && String(payload.userId) !== String(myId) && enemyPlayer) {
      if (payload.logType === 'EXEC') enemyPlayer.status = 'RUNNING_TESTS'
      if (payload.logType === 'FAIL') {
        enemyPlayer.status = 'TEST_FAILED'
        setTimeout(() => {
          if (enemyPlayer.status === 'TEST_FAILED') enemyPlayer.status = 'IDLE'
        }, 2500)
      }
      if (payload.logType === 'PASS') {
        enemyPlayer.status = 'TEST_PASSED'
        setTimeout(() => {
          if (enemyPlayer.status === 'TEST_PASSED') enemyPlayer.status = 'IDLE'
        }, 2500)
      }
      if (payload.logType === 'SUB ') enemyPlayer.status = 'SUBMITTING'
      if (payload.logType === 'ERR ') {
        enemyPlayer.status = 'SUBMIT_FAILED'
        setTimeout(() => {
          if (enemyPlayer.status === 'SUBMIT_FAILED') enemyPlayer.status = 'IDLE'
        }, 2500)
      }
      if (payload.logType === ' AC ') {
        enemyPlayer.status = 'PASSED'
        if (payload.code)
          sessionStorage.setItem(`nexus_battle_enemy_code_${roomCode.value}`, payload.code)
        endGame('DEFEAT', '对手已率先通过所有测试用例！')
      }
      if (payload.logType === 'MSG ') {
        enemyPlayer.status = 'PING'
        setTimeout(() => {
          if (enemyPlayer.status === 'PING') enemyPlayer.status = 'IDLE'
        }, 2500)
      }
    }
  })
  battleWs.connect()
}

let keystrokeCount = 0
let lastTelemetryTime = Date.now()
let idleTimeout: number | null = null
const handleEditorChange = () => {
  keystrokeCount++
  const now = Date.now()
  const mePlayer = battlePlayers.value[0]
  if (mePlayer && (mePlayer.status === 'IDLE' || mePlayer.status === 'TYPING'))
    mePlayer.status = 'TYPING'
  if (now - lastTelemetryTime >= 2000) {
    const loc = editorCode.value.split('\n').length
    const timeDiffMin = (now - lastTelemetryTime) / 60000
    const cpm = Math.floor(keystrokeCount / timeDiffMin)
    if (mePlayer) {
      mePlayer.loc = loc
      mePlayer.cpm = cpm
    }
    if (battleWs && battleStatus.value === 'FIGHTING') battleWs.send('TELEMETRY_SYNC', { loc, cpm })
    keystrokeCount = 0
    lastTelemetryTime = now
  }
  if (idleTimeout) clearTimeout(idleTimeout)
  idleTimeout = window.setTimeout(() => {
    if (mePlayer) {
      mePlayer.cpm = 0
      if (mePlayer.status === 'TYPING') mePlayer.status = 'IDLE'
    }
    if (battleWs && battleStatus.value === 'FIGHTING') {
      const loc = editorCode.value.split('\n').length
      battleWs.send('TELEMETRY_SYNC', { loc, cpm: 0 })
    }
  }, 2500)
}
watch(editorCode, () => {
  if (battleStatus.value === 'FIGHTING' && !isViewingOpponentCode.value) handleEditorChange()
})

const pushLocalLog = (logType: any, msg: string) => {
  battleLogs.value.push({ id: Date.now(), timestamp: getNowTime(), prefix: logType, message: msg })
}
const handleRunStart = () => {
  if (battleStatus.value !== 'FIGHTING') return
  pushLocalLog('EXEC', '我方启动了本地测试用例...')
  if (battlePlayers.value[0]) battlePlayers.value[0].status = 'RUNNING_TESTS'
  battleWs?.send('BATTLE_LOG', { logType: 'EXEC', message: '对方启动了本地测试用例...' })
}
const handleRunSuccess = () => {
  if (battleStatus.value !== 'FIGHTING') return
  pushLocalLog('PASS', '我方本地测试全部通过！')
  if (battlePlayers.value[0]) battlePlayers.value[0].status = 'TEST_PASSED'
  battleWs?.send('BATTLE_LOG', { logType: 'PASS', message: '对方本地测试全部通过！' })
  setTimeout(() => {
    if (battlePlayers.value[0]?.status === 'TEST_PASSED') battlePlayers.value[0].status = 'IDLE'
  }, 2500)
}
const handleRunFail = () => {
  if (battleStatus.value !== 'FIGHTING') return
  pushLocalLog('FAIL', '我方本地测试未通过！')
  if (battlePlayers.value[0]) battlePlayers.value[0].status = 'TEST_FAILED'
  battleWs?.send('BATTLE_LOG', { logType: 'FAIL', message: '对方本地测试未通过！' })
  setTimeout(() => {
    if (battlePlayers.value[0]?.status === 'TEST_FAILED') battlePlayers.value[0].status = 'IDLE'
  }, 2500)
}
const handleRunEnd = () => {}
const handleSubmitStart = () => {
  if (battleStatus.value !== 'FIGHTING') return
  pushLocalLog('SUB ', '我方发起了最终解答提交...')
  if (battlePlayers.value[0]) battlePlayers.value[0].status = 'SUBMITTING'
  battleWs?.send('BATTLE_LOG', { logType: 'SUB ', message: '对方发起了最终解答提交...' })
}
const handleSubmitFail = () => {
  if (battleStatus.value !== 'FIGHTING') return
  pushLocalLog('ERR ', '我方解答错误，遭遇滑铁卢！')
  if (battlePlayers.value[0]) battlePlayers.value[0].status = 'SUBMIT_FAILED'
  battleWs?.send('BATTLE_LOG', { logType: 'ERR ', message: '对方解答错误，遭遇滑铁卢！' })
  setTimeout(() => {
    if (battlePlayers.value[0]?.status === 'SUBMIT_FAILED') battlePlayers.value[0].status = 'IDLE'
  }, 2500)
}

const handleSuccess = async () => {
  if (battleStatus.value !== 'FIGHTING') return
  pushLocalLog(' AC ', '我方突破防线，成功 AC！')
  if (battlePlayers.value[0]) battlePlayers.value[0].status = 'PASSED'
  battleWs?.send('BATTLE_LOG', {
    logType: ' AC ',
    message: '对方突破防线，成功 AC！',
    code: editorCode.value,
  })

  try {
    const myId = userStore.userInfo?.id || (userStore as any).id || (userStore as any).userId
    await forceSettleMatch({ roomCode: roomCode.value, winnerId: Number(myId) })
  } catch (e) {
    console.error('前端请求结算系统失败', e)
  }

  setTimeout(() => endGame('VICTORY', '您已率先攻破题目！'), 1500)
}

const handleTacticalPing = (type: string) => {
  if (battleStatus.value !== 'FIGHTING') return
  let message = '发出了未知战术信号'
  if (type === 'bug') message = '向你发送了一只 Bug 🐛'
  if (type === 'coffee') message = '觉得你需要喝杯咖啡冷静一下 ☕'
  if (type === 'rocket') message = '暗示他即将发起最终提交 🚀'
  if (battlePlayers.value[0]) battlePlayers.value[0].status = 'PING'
  battleWs?.send('BATTLE_LOG', { logType: 'MSG ', message: '对方' + message })
  setTimeout(() => {
    if (battlePlayers.value[0]?.status === 'PING') battlePlayers.value[0].status = 'IDLE'
  }, 2500)
}

const endGame = (result: BattleStatus, reason?: string) => {
  if (battleStatus.value !== 'FIGHTING') return
  battleStatus.value = result
  showSettlement.value = true
  if (gameTimerInterval) clearInterval(gameTimerInterval)

  if (battleWs) {
    setTimeout(() => {
      battleWs!.disconnect()
    }, 2000)
  }
}

const getNowTime = () => {
  const now = new Date()
  return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
}

onMounted(() => initBattleField())
onBeforeUnmount(() => {
  if (gameTimerInterval) clearInterval(gameTimerInterval)
  if (idleTimeout) clearTimeout(idleTimeout)
  if (battleWs) battleWs.disconnect()
})
onBeforeRouteLeave((to, from, next) => {
  if (battleStatus.value === 'FIGHTING') {
    showExitDialog.value = true
    next(false)
  } else next()
})

const handleExitClick = () => {
  if (battleStatus.value === 'FIGHTING') showExitDialog.value = true
  else router.replace('/arena')
}

// 核心修复：投降时向后端发送弃权协议
const confirmForceExit = () => {
  if (battleWs) battleWs.send('SURRENDER')
  matchReason.value = 'ESCAPE' // 标记自己是逃跑者
  showExitDialog.value = false
  endGame('DEFEAT', '我方特工主动投降')
}

const handlePractice = async () => {
  try {
    await navigator.clipboard.writeText(editorCode.value)
    showToast('✅ 您的代码已自动复制到剪贴板！正在跳转刷题页...')
  } catch (e) {
    showToast('浏览器限制了剪贴板权限，请手动复制', 'error')
  }
  setTimeout(() => router.replace(`/problems/${problemId.value}`), 1000)
}

const handleViewOpponentCode = async () => {
  isFetchingOpponent.value = true
  try {
    let resCode = ''
    try {
      resCode = await getOpponentCode(roomCode.value)
    } catch (err) {
      resCode = sessionStorage.getItem(`nexus_battle_enemy_code_${roomCode.value}`) || ''
    }
    if (!resCode) throw new Error('Code is empty')
    editorCode.value = resCode
    isViewingOpponentCode.value = true
    showSettlement.value = false
  } catch (err) {
    showToast('无法截获对手情报代码！可能对方根本没写', 'error')
  } finally {
    isFetchingOpponent.value = false
  }
}

const backToSettlement = () => {
  isViewingOpponentCode.value = false
  showSettlement.value = true
}

const handleResize = (event: { min: number; max: number; size: number }[]) => {
  /* Resize 代码略 */
}
const toggleMaximizeLeft = () => {
  /* 左侧放大代码略 */
}
const toggleMaximizeMiddle = () => {
  /* 中间放大代码略 */
}
</script>

<style scoped>
:deep(.splitpanes__pane) {
  overflow: hidden;
}
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}
.scale-enter-active,
.scale-leave-active {
  transition: all 0.2s ease;
}
.scale-enter-from,
.scale-leave-to {
  transform: scale(0);
  opacity: 0;
}
.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.toast-slide-enter-from,
.toast-slide-leave-to {
  opacity: 0;
  transform: translate(-50%, -20px) scale(0.9);
}
</style>

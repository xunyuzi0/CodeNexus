<template>
  <div class="min-h-[85vh] w-full relative flex flex-col items-center pb-32 overflow-hidden">
    <div
      class="absolute top-[-200px] left-1/2 -translate-x-1/2 w-[800px] h-[500px] bg-[#FF4C00]/20 blur-[150px] rounded-full pointer-events-none z-0"
    ></div>
    <div class="absolute inset-0 bg-grid-white/[0.02] pointer-events-none z-0"></div>

    <div
      class="relative z-10 mt-12 mb-16 flex items-end justify-center gap-4 md:gap-12 px-4 w-full max-w-4xl select-none"
    >
      <div
        class="flex flex-col items-center group"
        v-motion
        :initial="{ opacity: 0, y: 50 }"
        :enter="{ opacity: 1, y: 0, transition: { delay: 200, duration: 600, type: 'spring' } }"
      >
        <div class="relative mb-4 transition-transform duration-500 group-hover:-translate-y-2">
          <div
            class="w-20 h-20 rounded-full border-2 border-zinc-400 p-1 shadow-[0_0_20px_rgba(161,161,170,0.3)] bg-zinc-900"
          >
            <img
              :src="topThree[1]?.avatar"
              class="w-full h-full rounded-full object-cover grayscale-[0.3]"
            />
          </div>
          <div
            class="absolute -bottom-3 left-1/2 -translate-x-1/2 bg-zinc-800 text-zinc-300 text-[10px] font-bold px-2 py-0.5 rounded-full border border-zinc-600 whitespace-nowrap"
          >
            第二名
          </div>
        </div>
        <div class="text-center mb-2">
          <h3 class="text-zinc-200 font-bold text-sm truncate max-w-[100px]">
            {{ topThree[1]?.name }}
          </h3>
          <p class="text-zinc-500 font-mono text-xs">{{ topThree[1]?.score }} 分</p>
        </div>
        <div
          class="w-24 h-24 md:w-32 md:h-32 bg-gradient-to-b from-zinc-700/50 to-zinc-900/10 backdrop-blur-md border-t border-zinc-500/30 rounded-t-xl relative overflow-hidden"
        >
          <div
            class="absolute inset-0 bg-gradient-to-b from-zinc-400/10 to-transparent opacity-50"
          ></div>
          <div
            class="absolute bottom-4 left-1/2 -translate-x-1/2 text-6xl font-black text-zinc-600/30 font-mono"
          >
            2
          </div>
        </div>
      </div>

      <div
        class="flex flex-col items-center group relative z-20"
        v-motion
        :initial="{ opacity: 0, y: -50 }"
        :enter="{
          opacity: 1,
          y: 0,
          transition: { delay: 0, duration: 800, type: 'spring', stiffness: 100 },
        }"
      >
        <div class="relative mb-6 transition-transform duration-500 group-hover:-translate-y-4">
          <Crown
            class="absolute -top-10 left-1/2 -translate-x-1/2 w-10 h-10 text-yellow-400 drop-shadow-[0_0_15px_rgba(250,204,21,0.6)] animate-bounce-slow"
            fill="currentColor"
          />

          <div
            class="absolute -inset-4 rounded-full border border-[#FF4C00]/30 border-dashed animate-[spin_10s_linear_infinite]"
          ></div>
          <div
            class="absolute -inset-4 rounded-full border border-[#FF4C00]/10 animate-[spin_15s_linear_infinite_reverse]"
          ></div>

          <div
            class="w-28 h-28 rounded-full border-4 border-[#FF4C00] p-1 shadow-[0_0_50px_rgba(255,76,0,0.5)] bg-zinc-900 relative z-10"
          >
            <img :src="topThree[0]?.avatar" class="w-full h-full rounded-full object-cover" />
          </div>
          <div
            class="absolute -bottom-4 left-1/2 -translate-x-1/2 bg-gradient-to-r from-yellow-600 to-[#FF4C00] text-white text-xs font-black px-4 py-1 rounded-full shadow-lg border border-yellow-500/50 tracking-wider whitespace-nowrap"
          >
            算力之王
          </div>
        </div>
        <div class="text-center mb-2 relative z-20">
          <h3 class="text-white font-black text-xl truncate max-w-[150px] drop-shadow-md">
            {{ topThree[0]?.name }}
          </h3>
          <p class="text-[#FF4C00] font-mono font-bold text-sm">{{ topThree[0]?.score }} 分</p>
        </div>
        <div
          class="w-32 h-36 md:w-40 md:h-48 bg-gradient-to-b from-[#FF4C00]/20 to-zinc-900/10 backdrop-blur-xl border-t border-[#FF4C00]/50 rounded-t-2xl relative overflow-hidden shadow-[0_-10px_40px_rgba(255,76,0,0.15)]"
        >
          <div
            class="absolute inset-0 bg-[linear-gradient(45deg,transparent_25%,rgba(255,255,255,0.05)_50%,transparent_75%,transparent_100%)] bg-[length:250%_250%] animate-shine"
          ></div>
          <div
            class="absolute bottom-6 left-1/2 -translate-x-1/2 text-8xl font-black text-[#FF4C00]/20 font-mono"
          >
            1
          </div>
        </div>
      </div>

      <div
        class="flex flex-col items-center group"
        v-motion
        :initial="{ opacity: 0, y: 50 }"
        :enter="{ opacity: 1, y: 0, transition: { delay: 400, duration: 600, type: 'spring' } }"
      >
        <div class="relative mb-4 transition-transform duration-500 group-hover:-translate-y-2">
          <div
            class="w-20 h-20 rounded-full border-2 border-orange-800 p-1 shadow-[0_0_20px_rgba(154,52,18,0.3)] bg-zinc-900"
          >
            <img
              :src="topThree[2]?.avatar"
              class="w-full h-full rounded-full object-cover grayscale-[0.3]"
            />
          </div>
          <div
            class="absolute -bottom-3 left-1/2 -translate-x-1/2 bg-zinc-800 text-orange-700 text-[10px] font-bold px-2 py-0.5 rounded-full border border-orange-900/50 whitespace-nowrap"
          >
            第三名
          </div>
        </div>
        <div class="text-center mb-2">
          <h3 class="text-zinc-200 font-bold text-sm truncate max-w-[100px]">
            {{ topThree[2]?.name }}
          </h3>
          <p class="text-zinc-500 font-mono text-xs">{{ topThree[2]?.score }} 分</p>
        </div>
        <div
          class="w-24 h-20 md:w-32 md:h-28 bg-gradient-to-b from-orange-900/40 to-zinc-900/10 backdrop-blur-md border-t border-orange-700/30 rounded-t-xl relative overflow-hidden"
        >
          <div
            class="absolute inset-0 bg-gradient-to-b from-orange-500/10 to-transparent opacity-30"
          ></div>
          <div
            class="absolute bottom-4 left-1/2 -translate-x-1/2 text-6xl font-black text-orange-900/40 font-mono"
          >
            3
          </div>
        </div>
      </div>
    </div>

    <div class="w-full max-w-4xl px-4 z-10">
      <div
        class="grid grid-cols-[1fr_4fr_2fr_2fr] md:grid-cols-[0.8fr_3fr_2fr_1.5fr_1.5fr] px-6 py-3 mb-2 text-xs font-bold text-zinc-500 uppercase tracking-widest border-b border-white/5"
      >
        <div class="text-center md:text-left">排名</div>
        <div>选手信息</div>
        <div class="hidden md:block">段位称号</div>
        <div>近期胜率</div>
        <div class="text-right">战力积分</div>
      </div>

      <div class="space-y-3">
        <div
          v-for="(user, index) in listData"
          :key="user.id"
          class="group relative grid grid-cols-[1fr_4fr_2fr_2fr] md:grid-cols-[0.8fr_3fr_2fr_1.5fr_1.5fr] items-center h-16 md:h-18 px-6 bg-zinc-900/40 backdrop-blur-md border border-white/5 rounded-2xl transition-all duration-300 hover:bg-white/5 hover:border-[#FF4C00]/30 hover:scale-[1.01] hover:shadow-[0_4px_20px_rgba(0,0,0,0.5)] cursor-default"
          v-motion
          :initial="{ opacity: 0, x: -20 }"
          :enter="{ opacity: 1, x: 0, transition: { delay: index * 50 } }"
        >
          <div
            class="font-mono text-lg font-bold text-center md:text-left"
            :class="index + 4 <= 10 ? 'text-white' : 'text-zinc-600'"
          >
            #{{ index + 4 }}
          </div>

          <div class="flex items-center gap-3 md:gap-4 overflow-hidden">
            <div
              class="w-8 h-8 md:w-10 md:h-10 rounded-full bg-zinc-800 shrink-0 border border-white/10 group-hover:border-[#FF4C00] transition-colors"
            >
              <img :src="user.avatar" class="w-full h-full rounded-full object-cover" />
            </div>
            <div class="flex flex-col min-w-0">
              <span
                class="text-sm font-bold text-zinc-200 group-hover:text-white truncate transition-colors"
                >{{ user.name }}</span
              >
              <span class="text-[10px] text-zinc-500 font-mono truncate md:hidden">{{
                user.tier
              }}</span>
            </div>
          </div>

          <div class="hidden md:flex">
            <span
              class="px-2.5 py-1 rounded-md text-[10px] font-bold uppercase tracking-wider border"
              :class="getTierStyle(user.score)"
            >
              {{ user.tier }}
            </span>
          </div>

          <div class="flex flex-col justify-center gap-1 pr-4">
            <div class="flex justify-between text-[10px] font-mono text-zinc-400">
              <span>胜</span>
              <span>{{ user.winRate }}%</span>
            </div>
            <div class="h-1.5 w-full bg-zinc-800 rounded-full overflow-hidden">
              <div
                class="h-full rounded-full transition-all duration-1000"
                :class="
                  user.winRate >= 60 ? 'bg-emerald-500 shadow-[0_0_8px_#10b981]' : 'bg-zinc-500'
                "
                :style="{ width: `${user.winRate}%` }"
              ></div>
            </div>
          </div>

          <div
            class="text-right font-mono text-lg md:text-xl font-black text-[#FF4C00] tracking-tight tabular-nums group-hover:drop-shadow-[0_0_8px_rgba(255,76,0,0.5)] transition-all"
          >
            {{ user.score }}
          </div>

          <div
            class="absolute left-0 top-0 bottom-0 w-1 bg-[#FF4C00] rounded-l-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-300"
          ></div>
        </div>
      </div>
    </div>

    <div
      class="fixed bottom-6 left-1/2 -translate-x-1/2 z-50 w-[90%] max-w-4xl"
      v-motion
      :initial="{ opacity: 0, y: 50 }"
      :enter="{ opacity: 1, y: 0, transition: { delay: 1000, duration: 600 } }"
    >
      <div
        class="relative h-16 md:h-20 bg-[#09090b]/90 backdrop-blur-xl border border-[#FF4C00]/50 shadow-[0_0_30px_rgba(255,76,0,0.15)] rounded-full px-6 md:px-8 flex items-center justify-between overflow-hidden group"
      >
        <div
          class="absolute inset-0 bg-gradient-to-r from-transparent via-[#FF4C00]/5 to-transparent animate-shine pointer-events-none"
        ></div>

        <div class="flex items-center gap-4 md:gap-6 z-10">
          <div class="flex flex-col items-center">
            <span class="text-[10px] text-zinc-500 font-bold uppercase tracking-wider"
              >我的排名</span
            >
            <span class="text-xl md:text-2xl font-black text-white font-mono italic"
              >#{{ myRank.rank }}</span
            >
          </div>

          <div class="w-[1px] h-8 bg-white/10"></div>

          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-full border-2 border-[#FF4C00] p-0.5">
              <img :src="myRank.avatar" class="w-full h-full rounded-full object-cover" />
            </div>
            <div class="hidden md:block">
              <p class="text-sm font-bold text-white">{{ myRank.name }}</p>
              <p class="text-xs text-zinc-400">
                即将晋级 <span class="text-[#FF4C00]">荣耀宗师</span>
              </p>
            </div>
          </div>
        </div>

        <div class="flex items-center gap-4 z-10">
          <div class="text-right">
            <p class="text-[10px] text-zinc-500 font-bold uppercase tracking-wider">当前积分</p>
            <p class="text-xl md:text-3xl font-black text-[#FF4C00] font-mono tabular-nums">
              {{ myRank.score }}
            </p>
          </div>
          <ChevronUp class="w-5 h-5 text-emerald-500 animate-bounce" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Crown, ChevronUp } from 'lucide-vue-next'

// --- Interfaces ---
interface RankUser {
  id: number
  name: string
  avatar: string
  score: number
  winRate: number
  tier: string
}

// --- State ---
const topThree = ref<RankUser[]>([])
const listData = ref<RankUser[]>([])
const myRank = ref({
  rank: 1204,
  name: 'Nexus Commander',
  avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix',
  score: 1450,
  tier: 'Diamond',
})

// --- Mock Data Generator ---
const generateMockData = () => {
  const avatars = [
    'https://api.dicebear.com/7.x/avataaars/svg?seed=King',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=Queen',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=Jack',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=Ace',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=Joker',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=Sora',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=Luna',
    'https://api.dicebear.com/7.x/avataaars/svg?seed=Max',
  ]

  const tiers = ['天梯传说', '荣耀宗师', '超凡大师', '璀璨钻石', '华贵铂金']

  const users: RankUser[] = []

  for (let i = 0; i < 20; i++) {
    const score = 3200 - i * Math.floor(Math.random() * 50 + 50)

    // [修复] 确保 tier 永远有值
    let tier = tiers[4] || '华贵铂金'

    // [修复] 使用 || 兜底数组取值
    const tier0 = tiers[0] || '天梯传说'
    const tier1 = tiers[1] || '荣耀宗师'
    const tier2 = tiers[2] || '超凡大师'
    const tier3 = tiers[3] || '璀璨钻石'

    if (score > 3000) tier = tier0
    else if (score > 2800) tier = tier1
    else if (score > 2500) tier = tier2
    else if (score > 2000) tier = tier3

    users.push({
      id: i + 1,
      name: `User_${Math.random().toString(36).substring(2, 8).toUpperCase()}`,
      // [修复] 确保 avatar 是字符串
      avatar: avatars[i % avatars.length] || '',
      score,
      winRate: Math.floor(Math.random() * 40 + 40),
      tier,
    })
  }

  // 手动修饰前三名，并使用 [修复] if 检查确保对象存在
  if (users[0]) {
    users[0].name = '算法之神'
    users[0].tier = '天梯传说'
    users[0].winRate = 88
  }

  if (users[1]) {
    users[1].name = '代码忍者'
    users[1].winRate = 76
  }

  if (users[2]) {
    users[2].name = '字节大师'
    users[2].winRate = 72
  }

  topThree.value = users.slice(0, 3)
  listData.value = users.slice(3)
}

// --- Utils ---
const getTierStyle = (score: number) => {
  if (score > 3000)
    return 'bg-[#FF4C00]/10 text-[#FF4C00] border-[#FF4C00]/20 shadow-[0_0_10px_#FF4C00]'
  if (score > 2800) return 'bg-purple-500/10 text-purple-400 border-purple-500/20'
  if (score > 2500) return 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20'
  return 'bg-zinc-800 text-zinc-400 border-zinc-700'
}

onMounted(() => {
  generateMockData()
})
</script>

<style scoped>
.animate-bounce-slow {
  animation: bounce 3s infinite;
}
@keyframes shine {
  from {
    transform: translateX(-100%);
  }
  to {
    transform: translateX(100%);
  }
}
.animate-shine {
  animation: shine 3s infinite linear;
}
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>

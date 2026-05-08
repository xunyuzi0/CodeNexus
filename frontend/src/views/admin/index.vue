<template>
  <div
    class="min-h-[calc(100vh-80px)] bg-zinc-50 dark:bg-zinc-950 bg-grid-black/[0.02] dark:bg-grid-white/[0.02] p-8"
  >
    <!-- 背景光晕 -->
    <div
      class="fixed top-[-20%] left-[10%] w-[500px] h-[500px] bg-[#FF4C00]/8 blur-[120px] rounded-full pointer-events-none z-0 mix-blend-screen"
    ></div>
    <div
      class="fixed bottom-[-10%] right-[5%] w-[600px] h-[600px] bg-purple-900/8 blur-[100px] rounded-full pointer-events-none z-0 mix-blend-screen"
    ></div>

    <div class="relative z-10 max-w-7xl mx-auto space-y-8">
      <!-- 页面标题 -->
      <div
        class="flex items-center gap-3"
        v-motion
        :initial="{ opacity: 0, y: -50 }"
        :visible="{ opacity: 1, y: 0, transition: { duration: 400 } }"
      >
        <div
          class="p-2 bg-zinc-100 dark:bg-zinc-900/50 rounded-xl border border-zinc-200 dark:border-white/10 shadow-sm backdrop-blur-sm"
        >
          <Shield class="w-6 h-6 text-[#FF4C00]" />
        </div>
        <div>
          <h1 class="text-2xl font-bold text-zinc-900 dark:text-white">管理后台</h1>
          <p class="text-zinc-500 dark:text-zinc-500 text-sm mt-0.5">系统全局概览与数据监控</p>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div
          v-for="(card, index) in statCards"
          :key="card.title"
          v-motion
          :initial="{ opacity: 0, y: -50 }"
          :visible="{
            opacity: 1,
            y: 0,
            transition: { duration: 400, delay: 50 + index * 50 },
          }"
        >
          <AdminStatCard
            :title="card.title"
            :value="card.value"
            :icon="card.icon"
            :color="card.color"
          />
        </div>
      </div>

      <!-- 提交趋势折线图 -->
      <div
        class="bg-white dark:bg-zinc-900/60 backdrop-blur-xl border border-zinc-200 dark:border-white/5 rounded-2xl p-6 shadow-sm dark:shadow-none"
        v-motion
        :initial="{ opacity: 0, y: -50 }"
        :visible="{ opacity: 1, y: 0, transition: { duration: 400, delay: 300 } }"
      >
        <h3
          class="text-sm font-bold text-zinc-500 dark:text-zinc-400 tracking-wider mb-4 flex items-center gap-2"
        >
          <TrendingUp class="w-4 h-4" /> 近 30 天提交趋势
        </h3>
        <div class="h-[300px]">
          <Line v-if="trendChartData" :data="trendChartData!" :options="lineChartOptions" />
        </div>
      </div>

      <!-- 第三行：饼图 + 柱状图 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- 难度分布饼图 -->
        <div
          class="bg-white dark:bg-zinc-900/60 backdrop-blur-xl border border-zinc-200 dark:border-white/5 rounded-2xl p-6 shadow-sm dark:shadow-none"
          v-motion
          :initial="{ opacity: 0, y: -50 }"
          :visible="{ opacity: 1, y: 0, transition: { duration: 400, delay: 400 } }"
        >
          <h3
            class="text-sm font-bold text-zinc-500 dark:text-zinc-400 tracking-wider mb-4 flex items-center gap-2"
          >
            <PieChart class="w-4 h-4" /> 难度分布
          </h3>
          <div class="h-[280px] flex items-center justify-center">
            <Pie
              v-if="difficultyChartData"
              :data="difficultyChartData!"
              :options="pieChartOptions"
            />
          </div>
        </div>

        <!-- 热门题目柱状图 -->
        <div
          class="bg-white dark:bg-zinc-900/60 backdrop-blur-xl border border-zinc-200 dark:border-white/5 rounded-2xl p-6 shadow-sm dark:shadow-none"
          v-motion
          :initial="{ opacity: 0, y: -50 }"
          :visible="{ opacity: 1, y: 0, transition: { duration: 400, delay: 450 } }"
        >
          <h3
            class="text-sm font-bold text-zinc-500 dark:text-zinc-400 tracking-wider mb-4 flex items-center gap-2"
          >
            <BarChart3 class="w-4 h-4" /> 热门题目 Top 10
          </h3>
          <div class="h-[280px]">
            <Bar
              v-if="topProblemsChartData"
              :data="topProblemsChartData!"
              :options="barChartOptions"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  Shield,
  TrendingUp,
  PieChart,
  BarChart3,
  Users,
  FileCode,
  Send,
  Percent,
} from 'lucide-vue-next'
import { Line, Pie, Bar } from 'vue-chartjs'
import { isDark } from '@/composables/useTheme'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  ArcElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js'
import AdminStatCard from '@/components/admin/AdminStatCard.vue'
import { getAdminStatsOverview, getAdminStatsTrends, getAdminStatsActivity } from '@/api/admin'
import type { AdminStatsOverviewVO, DailyTrendVO, ActivityAnalysisVO } from '@/types/api'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  ArcElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler,
)

// --- 数据 ---
const overview = ref<AdminStatsOverviewVO>({
  totalUsers: 0,
  totalProblems: 0,
  totalSubmissions: 0,
  todayNewUsers: 0,
  todaySubmissions: 0,
  overallPassRate: 0,
  activeUserCount: 0,
  onlineRoomCount: 0,
})
const trends = ref<DailyTrendVO[]>([])
const activity = ref<ActivityAnalysisVO>({
  difficultyDistribution: {},
  topProblems: [],
  languageDistribution: {},
})

// --- 统计卡片配置 ---
const statCards = computed(() => [
  { title: '总用户数', value: String(overview.value.totalUsers), icon: Users, color: '#3b82f6' },
  {
    title: '总题目数',
    value: String(overview.value.totalProblems),
    icon: FileCode,
    color: '#a855f7',
  },
  {
    title: '今日提交',
    value: String(overview.value.todaySubmissions),
    icon: Send,
    color: '#FF4C00',
  },
  { title: '通过率', value: `${overview.value.overallPassRate}%`, icon: Percent, color: '#22c55e' },
])

// --- 折线图 ---
const trendChartData = computed(() => {
  if (!trends.value.length) return null
  return {
    labels: trends.value.map((t) => t.date.slice(5)),
    datasets: [
      {
        label: '提交数',
        data: trends.value.map((t) => t.submissionCount),
        borderColor: '#FF4C00',
        backgroundColor: 'rgba(255,76,0,0.1)',
        fill: true,
        tension: 0.4,
        pointRadius: 2,
        pointHoverRadius: 5,
      },
      {
        label: '通过数',
        data: trends.value.map((t) => t.acceptedCount),
        borderColor: '#22c55e',
        backgroundColor: 'rgba(34,197,94,0.05)',
        fill: true,
        tension: 0.4,
        pointRadius: 2,
        pointHoverRadius: 5,
      },
    ],
  }
})

const lineChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      labels: {
        color: isDark.value ? 'rgba(255,255,255,0.6)' : 'rgba(0,0,0,0.6)',
        usePointStyle: true,
        pointStyle: 'circle',
      },
    },
    tooltip: {
      backgroundColor: isDark.value ? '#18181b' : '#ffffff',
      borderColor: isDark.value ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)',
      borderWidth: 1,
      titleColor: isDark.value ? '#fff' : '#000',
      bodyColor: isDark.value ? '#fff' : '#000',
    },
  },
  scales: {
    x: {
      grid: { color: isDark.value ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.05)' },
      ticks: {
        color: isDark.value ? 'rgba(255,255,255,0.4)' : 'rgba(0,0,0,0.4)',
        maxRotation: 0,
        autoSkip: true,
        maxTicksLimit: 10,
      },
    },
    y: {
      grid: { color: isDark.value ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.05)' },
      ticks: { color: isDark.value ? 'rgba(255,255,255,0.4)' : 'rgba(0,0,0,0.4)' },
      beginAtZero: true,
    },
  },
}))

// --- 饼图 ---
const difficultyChartData = computed(() => {
  const dist = activity.value.difficultyDistribution
  const keys = Object.keys(dist)
  if (!keys.length) return null
  return {
    labels: keys,
    datasets: [
      {
        data: keys.map((k) => dist[k] ?? 0),
        backgroundColor: keys.map((k) => {
          const lower = k.toLowerCase()
          if (lower.includes('easy') || lower === '1') return '#22c55e'
          if (lower.includes('medium') || lower === '2') return '#eab308'
          return '#ef4444'
        }),
        borderColor: 'transparent',
        borderWidth: 0,
      },
    ],
  }
})

const pieChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom' as const,
      labels: {
        color: isDark.value ? 'rgba(255,255,255,0.6)' : 'rgba(0,0,0,0.6)',
        usePointStyle: true,
        pointStyle: 'circle',
        padding: 16,
      },
    },
    tooltip: {
      backgroundColor: isDark.value ? '#18181b' : '#ffffff',
      borderColor: isDark.value ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)',
      borderWidth: 1,
      titleColor: isDark.value ? '#fff' : '#000',
      bodyColor: isDark.value ? '#fff' : '#000',
    },
  },
}))

// --- 柱状图 ---
const topProblemsChartData = computed(() => {
  const list = activity.value.topProblems
  if (!list?.length) return null
  return {
    labels: list.map((p) => p.displayId || p.title.slice(0, 8)),
    datasets: [
      {
        label: '提交次数',
        data: list.map((p) => p.submitCount),
        backgroundColor: '#FF4C00',
        borderRadius: 6,
        barThickness: 24,
      },
    ],
  }
})

const barChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  indexAxis: 'y' as const,
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: isDark.value ? '#18181b' : '#ffffff',
      borderColor: isDark.value ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)',
      borderWidth: 1,
      titleColor: isDark.value ? '#fff' : '#000',
      bodyColor: isDark.value ? '#fff' : '#000',
    },
  },
  scales: {
    x: {
      grid: { color: isDark.value ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.05)' },
      ticks: { color: isDark.value ? 'rgba(255,255,255,0.4)' : 'rgba(0,0,0,0.4)' },
      beginAtZero: true,
    },
    y: {
      grid: { display: false },
      ticks: {
        color: isDark.value ? 'rgba(255,255,255,0.6)' : 'rgba(0,0,0,0.6)',
        font: { size: 11 },
      },
    },
  },
}))

// --- 加载数据 ---
onMounted(async () => {
  const [overviewRes, trendsRes, activityRes] = await Promise.allSettled([
    getAdminStatsOverview(),
    getAdminStatsTrends(30),
    getAdminStatsActivity(),
  ])
  if (overviewRes.status === 'fulfilled') overview.value = overviewRes.value
  if (trendsRes.status === 'fulfilled') trends.value = trendsRes.value
  if (activityRes.status === 'fulfilled') activity.value = activityRes.value
})
</script>

<template>
  <div class="min-h-screen w-full relative p-6 md:p-12 pb-32 bg-zinc-950 text-zinc-400">
    <div class="absolute inset-0 bg-grid-white/[0.02] pointer-events-none fixed z-0"></div>
    <div
      class="absolute top-0 left-0 w-[600px] h-[600px] bg-zinc-800/30 blur-[150px] rounded-full pointer-events-none fixed z-0"
    ></div>

    <div class="relative z-10 max-w-[1200px] mx-auto">
      <div v-if="loading" class="animate-pulse flex flex-col gap-4 mb-10">
        <div class="h-10 bg-zinc-900 rounded-lg w-1/3"></div>
        <div class="h-4 bg-zinc-900 rounded w-1/4"></div>
      </div>

      <div
        v-else-if="folderData"
        class="mb-10 flex flex-col md:flex-row md:items-end justify-between gap-4"
        v-motion-slide-visible-top
      >
        <div>
          <div class="flex items-center gap-3 mb-2">
            <button
              @click="router.back()"
              class="p-2 bg-zinc-900/50 rounded-xl border border-white/10 shadow-sm backdrop-blur-sm hover:bg-white/5 hover:border-white/20 transition-all text-zinc-400 hover:text-white"
            >
              <ArrowLeft class="w-5 h-5" />
            </button>
            <h1 class="text-3xl font-black tracking-tight text-white flex items-center gap-3">
              <Folder class="w-8 h-8 text-[#FF4C00]" />
              {{ folderData.folder.name }}
            </h1>
          </div>
          <p class="text-zinc-500 text-sm font-medium tracking-wide pl-[52px]">
            创建于 {{ folderData.folder.createTime }} · 共 {{ folderData.folder.count }} 道题目
          </p>
        </div>

        <div class="flex items-center gap-3">
          <button
            v-if="!folderData.folder.isDefault"
            @click="openDeleteDialog"
            class="flex items-center justify-center px-4 py-2.5 bg-zinc-900/50 border border-red-900/30 text-red-500 hover:bg-red-500/10 hover:text-red-400 hover:border-red-500/50 transition-all rounded-xl text-sm font-bold shadow-sm"
          >
            <Trash2 class="w-4 h-4 mr-2" />
            删除收藏夹
          </button>
        </div>
      </div>

      <div v-if="loading" class="flex flex-col items-center justify-center h-64 space-y-4">
        <div
          class="w-10 h-10 border-2 border-zinc-800 border-t-[#FF4C00] rounded-full animate-spin"
        ></div>
        <p class="text-zinc-600 text-sm font-mono animate-pulse">正在加载收藏序列...</p>
      </div>

      <div
        v-else-if="folderData?.list.length === 0"
        class="flex flex-col items-center justify-center h-64 text-zinc-600 border border-white/5 rounded-2xl bg-zinc-900/20 backdrop-blur-sm"
        v-motion
        :initial="{ opacity: 0, scale: 0.95 }"
        :enter="{ opacity: 1, scale: 1, transition: { duration: 400 } }"
      >
        <FolderOpen class="w-12 h-12 mb-4 opacity-20" />
        <p>此收藏夹尚为空白，去题库中心探索吧</p>
      </div>

      <template v-else>
        <ProblemList
          :problems="folderData!.list"
          action-type="remove"
          @click="handleEnterProblem"
          @action="handleRemoveProblem"
        />
      </template>
    </div>

    <ArenaDialog
      v-model="deleteDialog.show"
      title="警告：删除收藏夹"
      :confirm-text="deleteDialog.loading ? '正在删除...' : '确认删除'"
      @confirm="confirmDeleteFolder"
    >
      <div class="flex flex-col gap-4 py-2">
        <div
          class="flex items-start gap-3 p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400"
        >
          <AlertCircle class="w-5 h-5 shrink-0 mt-0.5" />
          <div class="text-sm leading-relaxed">
            确定要删除
            <span class="text-white font-bold">"{{ folderData?.folder.name }}"</span> 吗？<br />
            此操作不可逆。删除后，该收藏夹内的题目仍保留在题库中，但将丢失此处的分类记录。
          </div>
        </div>
      </div>
    </ArenaDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Folder, FolderOpen, Trash2, AlertCircle } from 'lucide-vue-next'
import ProblemList from '@/components/problem/ProblemList.vue'
import ArenaDialog from '@/components/arena/ArenaDialog.vue' // 引入统一个毛玻璃弹窗

// API 引入
import {
  getFolderDetail,
  deleteFolder,
  removeFavoriteProblem,
  type FavoriteFolder,
  type FavoriteProblem,
} from '@/api/favorites'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const folderId = Number(route.params.id)
const folderData = ref<{ folder: FavoriteFolder; list: FavoriteProblem[] } | null>(null)

// --- 弹窗状态管理 ---
const deleteDialog = reactive({
  show: false,
  loading: false,
})

// --- Methods ---

const fetchDetail = async () => {
  if (!folderId) return
  loading.value = true
  try {
    const res = await getFolderDetail(folderId)
    folderData.value = res
  } catch (error) {
    console.error('获取收藏夹详情失败', error)
  } finally {
    loading.value = false
  }
}

const handleEnterProblem = (id: number) => {
  router.push({ name: 'ProblemDetail', params: { id: id.toString() } })
}

const handleRemoveProblem = async (problemId: number) => {
  try {
    await removeFavoriteProblem(folderId, problemId)
    // 乐观更新：直接从前端列表中移除，无需重新请求接口
    if (folderData.value) {
      folderData.value.list = folderData.value.list.filter((p) => p.id !== problemId)
      folderData.value.folder.count -= 1
    }
  } catch (error) {
    console.error('移除题目失败', error)
  }
}

// 唤起删除弹窗
const openDeleteDialog = () => {
  deleteDialog.show = true
}

// 执行真正的删除请求
const confirmDeleteFolder = async () => {
  if (deleteDialog.loading) return

  deleteDialog.loading = true
  try {
    await deleteFolder(folderId)
    deleteDialog.show = false
    // 删除成功后重定向到收藏夹主页 (替换路由历史)
    router.replace('/favorites')
  } catch (error) {
    console.error('删除收藏夹失败', error)
  } finally {
    deleteDialog.loading = false
  }
}

// --- Lifecycle ---
onMounted(() => {
  fetchDetail()
})
</script>

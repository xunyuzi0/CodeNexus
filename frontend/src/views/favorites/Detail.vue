<template>
  <div class="min-h-screen w-full relative p-6 md:p-12 pb-32 text-zinc-600 dark:text-zinc-400">
    <div class="relative z-10 max-w-[1200px] mx-auto">
      <div v-if="loading" class="animate-pulse flex flex-col gap-4 mb-10">
        <div class="h-10 bg-zinc-200 dark:bg-zinc-900 rounded-lg w-1/3"></div>
        <div class="h-4 bg-zinc-200 dark:bg-zinc-900 rounded w-1/4"></div>
      </div>

      <div
        v-else-if="folderData"
        class="mb-10 flex flex-col md:flex-row md:items-end justify-between gap-4"
        v-motion-slide-visible-top
      >
        <div>
          <div class="flex items-center gap-3 mb-2">
            <button
              @click="router.push('/favorites')"
              class="p-2 bg-zinc-100 dark:bg-zinc-900/50 rounded-xl border border-zinc-200 dark:border-white/10 shadow-sm backdrop-blur-sm hover:bg-zinc-200 dark:hover:bg-white/5 hover:border-zinc-300 dark:hover:border-white/20 transition-all text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-white"
            >
              <ArrowLeft class="w-5 h-5" />
            </button>
            <h1
              class="text-3xl font-black tracking-tight text-zinc-900 dark:text-white flex items-center gap-3"
            >
              <Folder class="w-8 h-8 text-[#FF4C00]" />
              {{ folderData.folder.name }}
              <button
                @click="openRenameDialog"
                class="p-1.5 rounded-lg text-zinc-400 dark:text-zinc-500 hover:text-[#FF4C00] hover:bg-zinc-100 dark:hover:bg-white/10 transition-all"
                title="修改名称"
              >
                <Pencil class="w-4 h-4" />
              </button>
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
            class="flex items-center justify-center px-4 py-2.5 bg-zinc-100 dark:bg-zinc-900/50 border border-red-200 dark:border-red-900/30 text-red-500 hover:bg-red-500/10 hover:text-red-400 hover:border-red-500/50 transition-all rounded-xl text-sm font-bold shadow-sm"
          >
            <Trash2 class="w-4 h-4 mr-2" />
            删除收藏夹
          </button>
        </div>
      </div>

      <div v-if="loading" class="flex flex-col items-center justify-center h-64 space-y-4">
        <div
          class="w-10 h-10 border-2 border-zinc-200 dark:border-zinc-800 border-t-[#FF4C00] rounded-full animate-spin"
        ></div>
        <p class="text-zinc-600 text-sm font-mono animate-pulse">正在加载收藏序列...</p>
      </div>

      <div
        v-else-if="folderData?.list.length === 0"
        class="flex flex-col items-center justify-center h-64 text-zinc-500 dark:text-zinc-600 border border-zinc-200 dark:border-white/5 rounded-2xl bg-zinc-100/50 dark:bg-zinc-900/20 backdrop-blur-sm"
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

    <!-- 修改名称弹窗 -->
    <ArenaDialog
      v-model="renameDialog.show"
      title="修改收藏夹名称"
      :confirm-text="renameDialog.saving ? '保存中...' : '确认修改'"
      @confirm="confirmRename"
    >
      <div class="py-2">
        <input
          ref="renameInputRef"
          v-model="renameDialog.name"
          @keydown.enter="confirmRename"
          class="w-full bg-zinc-50 dark:bg-zinc-900/80 border border-zinc-200 dark:border-white/15 rounded-xl px-4 py-3 text-zinc-900 dark:text-white text-sm outline-none focus:border-[#FF4C00] transition-colors placeholder-zinc-400 dark:placeholder-zinc-600"
          placeholder="请输入新的收藏夹名称"
          maxlength="30"
        />
        <p v-if="renameDialog.error" class="mt-2 text-red-400 text-xs">
          {{ renameDialog.error }}
        </p>
      </div>
    </ArenaDialog>

    <!-- 移除题目弹窗 -->
    <ArenaDialog
      v-model="removeProblemDialog.show"
      title="移除题目"
      :confirm-text="removeProblemDialog.loading ? '移除中...' : '确认移除'"
      @confirm="confirmRemoveProblem"
    >
      <div class="flex flex-col gap-4 py-2">
        <div
          class="flex items-start gap-3 p-4 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-500"
        >
          <AlertCircle class="w-5 h-5 shrink-0 mt-0.5" />
          <div class="text-sm leading-relaxed">
            确定要将题目
            <span class="text-zinc-900 dark:text-white font-bold"
              >"{{ removeProblemDialog.problemTitle }}"</span
            >
            从该收藏夹中移除吗？<br />
            <span class="text-zinc-500 dark:text-zinc-400"
              >题目本身不会被删除，仅取消收藏分类。</span
            >
          </div>
        </div>
      </div>
    </ArenaDialog>

    <!-- 删除收藏夹弹窗 -->
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
            <span class="text-zinc-900 dark:text-white font-bold"
              >"{{ folderData?.folder.name }}"</span
            >
            吗？<br />
            此操作不可逆。删除后，该收藏夹内的题目仍保留在题库中，但将丢失此处的分类记录。
          </div>
        </div>
      </div>
    </ArenaDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Folder, FolderOpen, Trash2, AlertCircle, Pencil } from 'lucide-vue-next'
import ProblemList from '@/components/problem/ProblemList.vue'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'

import {
  getFolderDetail,
  deleteFolder,
  removeFavoriteProblem,
  updateFolderName,
  type FavoriteFolder,
  type FavoriteProblem,
} from '@/api/favorites'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const folderId = Number(route.params.id)
const folderData = ref<{ folder: FavoriteFolder; list: FavoriteProblem[] } | null>(null)

const deleteDialog = reactive({ show: false, loading: false })
const removeProblemDialog = reactive({
  show: false,
  loading: false,
  problemId: 0,
  problemTitle: '',
})
const renameDialog = reactive({ show: false, name: '', saving: false, error: '' })
const renameInputRef = ref<HTMLInputElement | null>(null)

const fetchDetail = async () => {
  if (!folderId) return
  loading.value = true
  try {
    folderData.value = await getFolderDetail(folderId)
  } catch (error) {
    console.error('获取收藏夹详情失败', error)
  } finally {
    loading.value = false
  }
}

const handleEnterProblem = (id: number) => {
  router.push({
    name: 'ProblemDetail',
    params: { id: id.toString() },
    query: { from: 'favorites', folderId: String(folderId) },
  })
}

const handleRemoveProblem = (problemId: number) => {
  const problem = folderData.value?.list.find((p) => p.id === problemId)
  removeProblemDialog.problemId = problemId
  removeProblemDialog.problemTitle = problem?.title || '未知题目'
  removeProblemDialog.show = true
}

const confirmRemoveProblem = async () => {
  if (removeProblemDialog.loading) return
  removeProblemDialog.loading = true
  try {
    await removeFavoriteProblem(folderId, removeProblemDialog.problemId)
    if (folderData.value) {
      folderData.value.list = folderData.value.list.filter(
        (p) => p.id !== removeProblemDialog.problemId,
      )
      folderData.value.folder.count -= 1
    }
    removeProblemDialog.show = false
  } catch (error) {
    console.error('移除题目失败', error)
  } finally {
    removeProblemDialog.loading = false
  }
}

// 修改名称
const openRenameDialog = () => {
  if (!folderData.value) return
  renameDialog.name = folderData.value.folder.name
  renameDialog.error = ''
  renameDialog.show = true
  nextTick(() => renameInputRef.value?.focus())
}

const confirmRename = async () => {
  const trimmed = renameDialog.name.trim()
  if (!trimmed) {
    renameDialog.error = '名称不能为空'
    return
  }
  if (folderData.value && trimmed === folderData.value.folder.name) {
    renameDialog.show = false
    return
  }

  renameDialog.saving = true
  renameDialog.error = ''
  try {
    await updateFolderName(folderId, trimmed)
    if (folderData.value) {
      folderData.value.folder.name = trimmed
    }
    renameDialog.show = false
  } catch (error) {
    renameDialog.error = '修改失败，请重试'
    console.error('修改名称失败', error)
  } finally {
    renameDialog.saving = false
  }
}

// 删除收藏夹
const openDeleteDialog = () => {
  deleteDialog.show = true
}

const confirmDeleteFolder = async () => {
  if (deleteDialog.loading) return
  deleteDialog.loading = true
  try {
    await deleteFolder(folderId)
    deleteDialog.show = false
    router.replace('/favorites')
  } catch (error) {
    console.error('删除收藏夹失败', error)
  } finally {
    deleteDialog.loading = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="min-h-[85vh] w-full relative p-6 md:p-12 overflow-hidden">
    <div class="absolute inset-0 bg-grid-white/[0.02] pointer-events-none"></div>
    <div
      class="absolute top-0 right-1/4 w-[600px] h-[600px] bg-[#FF4C00]/5 blur-[120px] rounded-full pointer-events-none"
    ></div>

    <div class="relative z-10 mb-12 flex flex-col justify-center gap-1" v-motion-slide-visible-top>
      <div class="flex items-center gap-3 mb-1">
        <div
          class="p-2 bg-zinc-900/50 rounded-xl border border-white/10 shadow-sm backdrop-blur-sm"
        >
          <Star class="w-6 h-6 text-[#FF4C00]" />
        </div>
        <h1 class="text-3xl font-black tracking-tight text-white">我的收藏</h1>
      </div>
      <p class="text-zinc-500 text-sm font-medium tracking-wide pl-1">
        管理您的算法知识库，构建专属技能树
      </p>
    </div>

    <div
      v-if="loading"
      class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6 relative z-10"
    >
      <div
        v-for="i in 4"
        :key="i"
        class="aspect-[4/3] rounded-2xl bg-zinc-900/40 border border-white/5 p-6 flex flex-col items-center justify-center animate-pulse"
      >
        <div class="w-16 h-16 bg-zinc-800 rounded-xl mb-4"></div>
        <div class="w-24 h-5 bg-zinc-800 rounded-md mb-2"></div>
        <div class="w-16 h-3 bg-zinc-800 rounded-md"></div>
      </div>
    </div>

    <div
      v-else
      class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6 relative z-10"
    >
      <div
        v-for="(folder, index) in folders"
        :key="folder.id"
        class="group relative aspect-[4/3] rounded-2xl bg-zinc-900/40 backdrop-blur-md border border-white/5 p-6 flex flex-col items-center justify-center cursor-pointer transition-all duration-300 hover:border-[#FF4C00]/50 hover:bg-zinc-800/60 hover:shadow-[0_10px_30px_rgba(0,0,0,0.3)]"
        v-motion
        :initial="{ opacity: 0, scale: 0.9 }"
        :enter="{ opacity: 1, scale: 1, transition: { delay: index * 50 } }"
        @click="goToDetail(folder.id)"
      >
        <button
          v-if="!folder.isDefault"
          @click.stop="triggerDelete(folder)"
          class="absolute top-3 right-3 p-2 rounded-lg text-zinc-600 hover:text-red-500 hover:bg-red-500/10 transition-colors opacity-0 group-hover:opacity-100"
          title="删除收藏夹"
        >
          <Trash2 class="w-4 h-4" />
        </button>

        <div class="mb-4 relative">
          <div
            class="absolute inset-0 bg-[#FF4C00]/20 blur-xl rounded-full opacity-0 group-hover:opacity-100 transition-opacity duration-500"
          ></div>
          <Folder
            class="w-16 h-16 text-zinc-500 group-hover:text-[#FF4C00] group-hover:-translate-y-2 transition-all duration-300"
            stroke-width="1.5"
          />
        </div>

        <h3
          class="text-lg font-bold text-white mb-1 group-hover:text-[#FF4C00] transition-colors truncate w-full text-center"
        >
          {{ folder.name }}
        </h3>
        <p class="text-xs text-zinc-500 font-mono tracking-wider">{{ folder.count }} 个挑战</p>

        <div
          class="absolute bottom-0 left-0 w-full h-[2px] bg-gradient-to-r from-transparent via-[#FF4C00] to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"
        ></div>
      </div>

      <button
        @click="openCreateDialog"
        class="aspect-[4/3] rounded-2xl border-2 border-dashed border-zinc-800 hover:border-[#FF4C00]/50 hover:bg-[#FF4C00]/5 flex flex-col items-center justify-center text-zinc-600 hover:text-[#FF4C00] transition-all duration-300 group"
      >
        <div
          class="w-12 h-12 rounded-full bg-zinc-900 border border-zinc-700 group-hover:border-[#FF4C00] flex items-center justify-center mb-3 transition-colors"
        >
          <Plus class="w-6 h-6" />
        </div>
        <span class="text-sm font-bold">新建收藏夹</span>
      </button>
    </div>

    <ArenaDialog
      v-model="dialogState.show"
      :title="dialogState.type === 'CREATE' ? '新建收藏空间' : '删除收藏夹'"
      :confirm-text="
        dialogState.type === 'CREATE'
          ? isSubmitting
            ? '创建中...'
            : '确认创建'
          : isSubmitting
            ? '删除中...'
            : '确认删除'
      "
      @confirm="handleConfirm"
    >
      <div v-if="dialogState.type === 'CREATE'" class="space-y-4">
        <div class="relative group">
          <input
            v-model="newFolderName"
            type="text"
            placeholder="请输入收藏夹名称"
            :disabled="isSubmitting"
            class="w-full bg-zinc-900/50 border border-zinc-700 text-white rounded-xl py-3 px-4 focus:outline-none focus:border-[#FF4C00] focus:ring-1 focus:ring-[#FF4C00] transition-all placeholder-zinc-600 disabled:opacity-50"
            @keyup.enter="handleConfirm"
          />
        </div>

        <p v-if="errorMessage" class="text-xs text-red-500 font-bold animate-pulse">
          {{ errorMessage }}
        </p>
        <p v-else class="text-xs text-zinc-500">
          * 请为您的知识库起一个响亮的名字，例如“动态规划精选”。
        </p>
      </div>

      <div v-else class="text-center">
        <p v-if="errorMessage" class="text-xs text-red-500 font-bold animate-pulse mb-2">
          {{ errorMessage }}
        </p>
        <p class="text-zinc-300 text-sm mb-2">
          您确定要删除
          <span class="text-white font-bold">"{{ dialogState.target?.name }}"</span> 吗？
        </p>
        <p class="text-red-500/80 text-xs bg-red-500/10 p-2 rounded-lg inline-block">
          注意：该操作不可恢复，且会移除其中所有题目的收藏状态。
        </p>
      </div>
    </ArenaDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Folder, Star, Plus, Trash2 } from 'lucide-vue-next'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'

// 引入真实的 API 接口
import { getFolders, createFolder, deleteFolder, type FavoriteFolder } from '@/api/favorites'

const router = useRouter()
const loading = ref(false)
const isSubmitting = ref(false) // 提交防抖状态
const folders = ref<FavoriteFolder[]>([])
const newFolderName = ref('')
const errorMessage = ref('') // 响应式错误提示

// 弹窗状态管理
const dialogState = reactive({
  show: false,
  type: 'CREATE' as 'CREATE' | 'DELETE',
  target: null as FavoriteFolder | null,
})

// 初始化：拉取真实数据
const initData = async () => {
  loading.value = true
  try {
    folders.value = await getFolders()
  } catch (error) {
    console.error('获取收藏夹列表失败', error)
  } finally {
    loading.value = false
  }
}

// 路由跳转
const goToDetail = (id: number) => {
  router.push(`/favorites/${id}`)
}

// 触发新建
const openCreateDialog = () => {
  newFolderName.value = ''
  errorMessage.value = '' // 每次打开清空错误提示
  dialogState.type = 'CREATE'
  dialogState.show = true
}

// 触发删除
const triggerDelete = (folder: FavoriteFolder) => {
  errorMessage.value = '' // 每次打开清空错误提示
  dialogState.type = 'DELETE'
  dialogState.target = folder
  dialogState.show = true
}

// 统一确认逻辑 (带完善的 UI 错误反馈)
const handleConfirm = async () => {
  if (isSubmitting.value) return // 防止网络慢时重复点击
  errorMessage.value = '' // 每次提交前清空历史错误

  if (dialogState.type === 'CREATE') {
    // 1. 前端表单非空校验
    if (!newFolderName.value.trim()) {
      errorMessage.value = '收藏夹名称不能为空哦！'
      return
    }

    isSubmitting.value = true
    try {
      // 2. 发起真实请求
      const newFolder = await createFolder(newFolderName.value.trim())
      folders.value.push(newFolder)
      dialogState.show = false // 成功才关闭弹窗
    } catch (error: any) {
      // 3. 将后端拦截或报错直观地显示在 UI 上
      console.error('新建收藏夹失败', error)
      errorMessage.value = error.message || '接口请求失败，请按 F12 检查控制台 (Network)'
    } finally {
      isSubmitting.value = false
    }
  } else if (dialogState.type === 'DELETE' && dialogState.target) {
    isSubmitting.value = true
    try {
      await deleteFolder(dialogState.target.id)
      folders.value = folders.value.filter((f) => f.id !== dialogState.target?.id)
      dialogState.show = false
    } catch (error: any) {
      console.error('删除收藏夹失败', error)
      errorMessage.value = error.message || '删除失败，请检查网络或刷新重试'
    } finally {
      isSubmitting.value = false
    }
  }
}

onMounted(() => {
  initData()
})
</script>

<style scoped>
/* 组件自带优雅过渡动画，若需要特定样式可在此扩展 */
</style>

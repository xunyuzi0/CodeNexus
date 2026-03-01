<template>
  <div class="space-y-8" v-motion-slide-visible-bottom>
    <div class="space-y-4">
      <h3 class="text-lg font-bold text-white flex items-center gap-2">
        <Lock class="w-5 h-5 text-[#FF4C00]" /> 修改密码
      </h3>
      <div class="grid grid-cols-1 gap-4 max-w-lg">
        <input
          v-model="pwdForm.old"
          type="password"
          placeholder="当前密码"
          class="w-full bg-zinc-900/50 border border-zinc-700 text-white rounded-xl px-4 py-3 focus:border-[#FF4C00] outline-none transition-colors"
        />
        <input
          v-model="pwdForm.new"
          type="password"
          placeholder="新密码"
          class="w-full bg-zinc-900/50 border border-zinc-700 text-white rounded-xl px-4 py-3 focus:border-[#FF4C00] outline-none transition-colors"
        />
        <input
          v-model="pwdForm.confirm"
          type="password"
          placeholder="确认新密码"
          class="w-full bg-zinc-900/50 border border-zinc-700 text-white rounded-xl px-4 py-3 focus:border-[#FF4C00] outline-none transition-colors"
        />
        <button
          @click="onUpdatePassword"
          :disabled="isProcessing"
          class="bg-zinc-800 hover:bg-zinc-700 text-white font-bold py-3 rounded-xl transition-all border border-white/5 disabled:opacity-50 disabled:cursor-not-allowed flex justify-center items-center gap-2"
        >
          <div
            v-if="isProcessing && currentAction === 'UPDATE_PWD'"
            class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"
          ></div>
          {{ isProcessing && currentAction === 'UPDATE_PWD' ? '处理中...' : '更新密码' }}
        </button>
      </div>
    </div>

    <div class="h-[1px] bg-white/5 w-full"></div>

    <div class="space-y-4">
      <h3 class="text-lg font-bold text-red-500 flex items-center gap-2">
        <AlertTriangle class="w-5 h-5" /> 危险区域
      </h3>
      <div
        class="flex items-center justify-between p-4 border border-red-500/20 bg-red-500/5 rounded-xl"
      >
        <div>
          <p class="text-white font-bold">注销账号</p>
          <p class="text-xs text-zinc-500 mt-1">一旦注销，您的所有数据将被永久删除，无法恢复。</p>
        </div>
        <button
          @click="onDeleteAccount"
          :disabled="isProcessing"
          class="px-4 py-2 bg-red-500/10 hover:bg-red-500 text-red-500 hover:text-white rounded-lg text-sm font-bold transition-all border border-red-500/50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ isProcessing && currentAction === 'DELETE_ACCOUNT' ? '注销中...' : '注销' }}
        </button>
      </div>
    </div>

    <ArenaDialog
      v-model="dialogState.show"
      :title="dialogState.title"
      :confirm-text="dialogState.actionType === 'ALERT' ? '我知道了' : '确认执行'"
      @confirm="handleConfirm"
    >
      <div v-html="dialogState.content" class="text-sm text-zinc-400 leading-relaxed"></div>
    </ArenaDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Lock, AlertTriangle } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'
import { updatePassword, deleteAccount } from '@/api/user'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'

const router = useRouter()
const userStore = useUserStore()

const isProcessing = ref(false)
const currentAction = ref('')

const pwdForm = reactive({
  old: '',
  new: '',
  confirm: '',
})

const dialogState = reactive({
  show: false,
  title: '',
  content: '',
  actionType: '' as 'UPDATE_PWD' | 'DELETE_ACCOUNT' | 'ALERT',
})

// 点击更新密码
const onUpdatePassword = () => {
  if (!pwdForm.old || !pwdForm.new || !pwdForm.confirm) {
    dialogState.title = '校验失败'
    dialogState.content = '请完整填写当前密码、新密码及确认密码。'
    dialogState.actionType = 'ALERT'
    dialogState.show = true
    return
  }

  // 【新增】密码长度校验
  if (pwdForm.new.length < 8) {
    dialogState.title = '密码强度不足'
    dialogState.content = '新密码长度必须至少为 8 个字符，请重新输入。'
    dialogState.actionType = 'ALERT'
    dialogState.show = true
    return
  }

  if (pwdForm.new !== pwdForm.confirm) {
    dialogState.title = '校验失败'
    dialogState.content = '两次输入的新密码不一致，请重新输入。'
    dialogState.actionType = 'ALERT'
    dialogState.show = true
    return
  }

  dialogState.title = '更新安全设置'
  dialogState.content =
    '您正在修改账户密码。修改成功后，系统将退出当前会话，您需要使用新密码重新登录。'
  dialogState.actionType = 'UPDATE_PWD'
  dialogState.show = true
}

// 点击注销账号
const onDeleteAccount = () => {
  dialogState.title = '危险操作警告'
  dialogState.content =
    '<span class="text-red-500 font-bold">此操作不可恢复！</span><br>您的账号、提交记录、积分排名等所有数据将被永久抹除。请再次确认。'
  dialogState.actionType = 'DELETE_ACCOUNT'
  dialogState.show = true
}

// 统一确认处理
const handleConfirm = async () => {
  dialogState.show = false

  if (dialogState.actionType === 'ALERT') {
    return
  }

  isProcessing.value = true
  currentAction.value = dialogState.actionType

  try {
    if (dialogState.actionType === 'UPDATE_PWD') {
      await updatePassword({
        oldPassword: pwdForm.old,
        newPassword: pwdForm.new,
      })
      // 成功后强制登出
      await userStore.logout()
      router.push('/login')
    } else if (dialogState.actionType === 'DELETE_ACCOUNT') {
      await deleteAccount()
      // 成功后强制登出
      await userStore.logout()
      router.push('/login')
    }
  } catch (error) {
    console.error('Security action failed:', error)
  } finally {
    isProcessing.value = false
    currentAction.value = ''
    pwdForm.old = ''
    pwdForm.new = ''
    pwdForm.confirm = ''
  }
}
</script>

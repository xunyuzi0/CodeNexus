<template>
  <header
    class="sticky top-0 z-40 w-full h-20 flex items-center justify-between px-10 bg-transparent pointer-events-none"
  >
    <div
      class="pointer-events-auto flex items-center space-x-2 text-sm text-zinc-500/80 font-medium"
    >
      <span>CodeNexus</span>
      <span class="text-zinc-700">/</span>
      <span class="text-zinc-700 dark:text-zinc-200">{{ route.meta.title || 'Dashboard' }}</span>
    </div>

    <div class="pointer-events-auto flex items-center gap-6">
      <button
        @click="toggleTheme"
        class="relative w-10 h-10 rounded-full flex items-center justify-center text-zinc-500 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-white hover:bg-zinc-100 dark:hover:bg-white/5 transition-all duration-300 focus:outline-none"
      >
        <div class="relative w-5 h-5 overflow-hidden">
          <Sun
            class="absolute inset-0 w-full h-full transition-all duration-500 ease-[cubic-bezier(0.23,1,0.32,1)]"
            :class="isDark ? 'rotate-90 opacity-0 scale-50' : 'rotate-0 opacity-100 scale-100'"
          />
          <Moon
            class="absolute inset-0 w-full h-full transition-all duration-500 ease-[cubic-bezier(0.23,1,0.32,1)]"
            :class="isDark ? 'rotate-0 opacity-100 scale-100' : '-rotate-90 opacity-0 scale-50'"
          />
        </div>
      </button>

      <div
        class="flex items-center gap-3 pl-6 border-l border-zinc-200 dark:border-white/10 relative"
        ref="dropdownRef"
      >
        <div class="text-right hidden md:block">
          <p class="text-sm font-medium text-zinc-900 dark:text-white">
            {{ userStore.nickname || 'Developer' }}
          </p>
          <p class="text-xs text-zinc-500">{{ isAdmin ? 'Admin' : 'User' }}</p>
        </div>

        <div class="relative group cursor-pointer" @click="toggleDropdown">
          <div
            class="h-10 w-10 rounded-full overflow-hidden ring-2 ring-zinc-200 dark:ring-white/10 transition-all duration-300"
            :class="{
              'ring-[#FF4C00]/50 shadow-[0_0_15px_rgba(255,76,0,0.3)]': isDropdownOpen,
              'group-hover:ring-[#FF4C00]/50': !isDropdownOpen,
            }"
          >
            <img
              :src="userStore.avatar || defaultAvatar"
              alt="Avatar"
              class="h-full w-full object-cover"
            />
          </div>
          <span
            class="absolute bottom-0 right-0 block h-2.5 w-2.5 rounded-full bg-emerald-500 ring-2 ring-white dark:ring-[#09090b]"
          ></span>
        </div>

        <Transition
          enter-active-class="transition ease-out duration-200"
          enter-from-class="opacity-0 translate-y-2 scale-95"
          enter-to-class="opacity-100 translate-y-0 scale-100"
          leave-active-class="transition ease-in duration-150"
          leave-from-class="opacity-100 translate-y-0 scale-100"
          leave-to-class="opacity-0 translate-y-2 scale-95"
        >
          <div
            v-if="isDropdownOpen"
            class="absolute right-0 top-14 w-56 rounded-xl border border-zinc-200 dark:border-white/10 bg-white/90 dark:bg-[#18181B]/90 backdrop-blur-xl shadow-xl z-50 overflow-hidden"
          >
            <div class="p-2 space-y-1">
              <div class="px-3 py-2 border-b border-zinc-200 dark:border-white/5 md:hidden">
                <p class="text-sm font-medium text-zinc-900 dark:text-white truncate">
                  {{ userStore.nickname || 'Developer' }}
                </p>
                <p class="text-xs text-zinc-500 truncate">
                  {{ userStore.userInfo?.userAccount || 'user@codenexus.com' }}
                </p>
              </div>

              <button
                v-if="!isAdmin"
                @click="handleProfile"
                class="w-full flex items-center px-3 py-2.5 text-sm rounded-lg text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-white/5 transition-colors group"
              >
                <User
                  class="w-4 h-4 mr-3 text-zinc-500 group-hover:text-[#FF4C00] transition-colors"
                />
                个人档案
              </button>

              <button
                v-if="isAdmin"
                @click="openPasswordDialog"
                class="w-full flex items-center px-3 py-2.5 text-sm rounded-lg text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-white/5 transition-colors group"
              >
                <KeyRound
                  class="w-4 h-4 mr-3 text-zinc-500 group-hover:text-[#FF4C00] transition-colors"
                />
                修改密码
              </button>

              <div class="h-[1px] bg-zinc-200 dark:bg-white/5 my-1"></div>

              <button
                @click="handleLogout"
                class="w-full flex items-center px-3 py-2.5 text-sm rounded-lg text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors group"
              >
                <LogOut class="w-4 h-4 mr-3 group-hover:scale-110 transition-transform" />
                退出登录
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <ArenaDialog
      v-model="showPasswordDialog"
      title="修改密码"
      confirm-text="确认修改"
      @confirm="handlePasswordConfirm"
    >
      <form autocomplete="off" @submit.prevent>
        <!-- 隐藏 dummy 输入框吸收浏览器自动填充 -->
        <input type="text" value="" class="absolute opacity-0 pointer-events-none" tabindex="-1" />
        <input
          type="password"
          value=""
          class="absolute opacity-0 pointer-events-none"
          tabindex="-1"
        />
        <div class="space-y-4">
          <input
            v-model="pwdForm.old"
            type="password"
            placeholder="当前密码"
            autocomplete="new-password"
            class="w-full bg-zinc-50 dark:bg-zinc-900/50 border border-zinc-200 dark:border-white/10 text-zinc-900 dark:text-white rounded-xl px-4 py-3 text-sm outline-none focus:border-[#FF4C00] transition-colors"
          />
          <input
            v-model="pwdForm.new"
            type="password"
            placeholder="新密码（至少 8 位）"
            autocomplete="new-password"
            class="w-full bg-zinc-50 dark:bg-zinc-900/50 border border-zinc-200 dark:border-white/10 text-zinc-900 dark:text-white rounded-xl px-4 py-3 text-sm outline-none focus:border-[#FF4C00] transition-colors"
          />
          <input
            v-model="pwdForm.confirm"
            type="password"
            placeholder="确认新密码"
            autocomplete="new-password"
            class="w-full bg-zinc-50 dark:bg-zinc-900/50 border border-zinc-200 dark:border-white/10 text-zinc-900 dark:text-white rounded-xl px-4 py-3 text-sm outline-none focus:border-[#FF4C00] transition-colors"
          />
          <p v-if="pwdError" class="text-rose-400 text-xs">{{ pwdError }}</p>
        </div>
      </form>
    </ArenaDialog>
  </header>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Sun, Moon, User, LogOut, KeyRound } from 'lucide-vue-next'
import { toggleTheme, isDark } from '@/composables/useTheme'
import { useUserStore } from '@/stores/user'
import { updatePassword } from '@/api/user'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'

const showPasswordDialog = ref(false)
const pwdError = ref('')
const pwdForm = reactive({ old: '', new: '', confirm: '' })
const isAdmin = computed(() => userStore.roles.includes('admin'))

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isDropdownOpen = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'

const toggleDropdown = () => {
  isDropdownOpen.value = !isDropdownOpen.value
}

const closeDropdown = (e: MouseEvent) => {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target as Node)) {
    isDropdownOpen.value = false
  }
}

const handleProfile = () => {
  isDropdownOpen.value = false
  router.push('/profile')
}

const handleLogout = async () => {
  isDropdownOpen.value = false
  await userStore.logout()
  router.push('/login')
}

const openPasswordDialog = () => {
  isDropdownOpen.value = false
  pwdError.value = ''
  pwdForm.old = ''
  pwdForm.new = ''
  pwdForm.confirm = ''
  showPasswordDialog.value = true
}

const handlePasswordConfirm = async () => {
  pwdError.value = ''

  if (!pwdForm.old || !pwdForm.new || !pwdForm.confirm) {
    pwdError.value = '请完整填写当前密码、新密码及确认密码'
    return
  }
  if (pwdForm.new.length < 8) {
    pwdError.value = '新密码长度必须至少为 8 个字符'
    return
  }
  if (pwdForm.new !== pwdForm.confirm) {
    pwdError.value = '两次输入的新密码不一致'
    return
  }

  try {
    await updatePassword({ oldPassword: pwdForm.old, newPassword: pwdForm.new })
    showPasswordDialog.value = false
    await userStore.logout()
    router.push('/login')
  } catch {
    pwdError.value = '密码修改失败，请检查当前密码是否正确'
  }
}

onMounted(() => {
  document.addEventListener('click', closeDropdown)
})

onUnmounted(() => {
  document.removeEventListener('click', closeDropdown)
})
</script>

<style scoped>
/* 覆盖 Chrome 自动填充的背景色 */
input:-webkit-autofill,
input:-webkit-autofill:hover,
input:-webkit-autofill:focus {
  -webkit-box-shadow: 0 0 0 1000px #f4f4f5 inset !important;
  -webkit-text-fill-color: #18181b !important;
  caret-color: #18181b;
  transition: background-color 5000s ease-in-out 0s;
}

:root.dark input:-webkit-autofill,
:root.dark input:-webkit-autofill:hover,
:root.dark input:-webkit-autofill:focus {
  -webkit-box-shadow: 0 0 0 1000px #18181b inset !important;
  -webkit-text-fill-color: #fff !important;
  caret-color: #fff;
}
</style>

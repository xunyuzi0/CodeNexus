import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUiStore = defineStore('ui', () => {
  // State
  const isSidebarCollapsed = ref(false)

  // Actions
  function toggleSidebar() {
    isSidebarCollapsed.value = !isSidebarCollapsed.value
  }

  function setSidebar(value: boolean) {
    isSidebarCollapsed.value = value
  }

  return {
    isSidebarCollapsed,
    toggleSidebar,
    setSidebar,
  }
})

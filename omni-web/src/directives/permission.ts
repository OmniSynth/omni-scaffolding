import type { App, Directive } from 'vue'
import { useUserStore } from '@/stores/user'

function check(el: HTMLElement, value: unknown) {
  const userStore = useUserStore()
  const codes = Array.isArray(value) ? value.map(String) : [String(value || '')]
  const ok = codes.some((code) => code && userStore.hasPermission(code))
  if (!ok && el.parentNode) {
    el.parentNode.removeChild(el)
  }
}

const permissionDirective: Directive<HTMLElement, string | string[]> = {
  mounted(el, binding) {
    check(el, binding.value)
  },
  updated(el, binding) {
    check(el, binding.value)
  },
}

export function setupPermissionDirective(app: App) {
  app.directive('permission', permissionDirective)
}

import { defineStore } from 'pinia'
import { authApi } from '@/api'

/** 登录态与权限：菜单、按钮权限、个人资料统一在此维护 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    roles: [],
    perms: [],
    menus: []
  }),
  getters: {
    isLogin: (s) => !!s.token,
    isPortal: (s) => s.roles.includes('LAB_USER') && !s.roles.some((r) => ['ADMIN', 'SAFETY', 'LAB_MANAGER', 'CHEM_KEEPER'].includes(r)),
    isAdmin: (s) => s.roles.includes('ADMIN'),
    avatar: (s) => s.user?.avatar || ''
  },
  actions: {
    async login(form) {
      const r = await authApi.login(form)
      this.token = r.data.token
      this.user = r.data.user
      this.roles = r.data.roles || []
      this.perms = r.data.perms || []
      this.menus = r.data.menus || []
      localStorage.setItem('token', this.token)
      localStorage.setItem('user', JSON.stringify(this.user))
      return r.data
    },
    async fetchMe() {
      const r = await authApi.me()
      this.user = r.data.user
      this.roles = r.data.roles || []
      this.perms = r.data.perms || []
      this.menus = r.data.menus || []
      localStorage.setItem('user', JSON.stringify(this.user))
      return r.data
    },
    setUser(u) {
      this.user = u
      localStorage.setItem('user', JSON.stringify(u))
    },
    has(perm) {
      return this.perms.includes('*:*:*') || this.perms.includes(perm)
    },
    logout() {
      this.token = ''
      this.user = null
      this.roles = []
      this.perms = []
      this.menus = []
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})

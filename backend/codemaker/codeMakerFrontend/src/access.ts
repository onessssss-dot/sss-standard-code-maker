import { useLoginUserStore } from '@/stores/loginUser'

interface AccessResult {
  hasAccess: boolean
  redirectPath?: string
}

const ADMIN_ROUTE_PREFIX = '/admin'

export function checkAccess(path: string): AccessResult {
  // 默认允许访问
  const result: AccessResult = {
    hasAccess: true,
  }

  if (path.startsWith(ADMIN_ROUTE_PREFIX)) {
    const loginUserStore = useLoginUserStore()
    const loginUser = loginUserStore.loginUser
    // 需要管理员权限
    if (!loginUser.id) {
      return {
        hasAccess: false,
        redirectPath: '/user/login',
      }
    }
    if (loginUser.userRole !== 'admin') {
      return {
        hasAccess: false,
        redirectPath: '/',
      }
    }
  }

  return result
}


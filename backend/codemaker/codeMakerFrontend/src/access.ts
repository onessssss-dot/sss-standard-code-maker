import { useLoginUserStore } from './stores/loginUser'
import { message } from 'ant-design-vue'

/**
 * 权限检查结果
 */
export interface AccessResult {
  /** 是否有权限访问 */
  hasAccess: boolean
  /** 如果需要重定向，重定向的路径 */
  redirectPath?: string
}

/**
 * 检查用户是否有权限访问指定路径
 * @param path 要访问的路径
 * @returns 权限检查结果
 */
export function checkAccess(path: string): AccessResult {
  const loginUserStore = useLoginUserStore()
  const loginUser = loginUserStore.loginUser

  // 如果路径以 /admin 开头，需要管理员权限
  if (path.startsWith('/admin')) {
    // 检查是否已登录
    if (!loginUser.id) {
      message.warning('请先登录')
      return {
        hasAccess: false,
        redirectPath: `/user/login?redirect=${encodeURIComponent(path)}`,
      }
    }

    // 检查用户角色，只有 admin 用户可以访问
    // user 用户或其他角色用户无法访问
    if (loginUser.userRole !== 'admin') {
      message.error('无权限访问，仅管理员可访问')
      return {
        hasAccess: false,
        redirectPath: '/',
      }
    }
  }

  return {
    hasAccess: true,
  }
}

/**
 * 检查用户是否为管理员
 * @returns 是否为管理员
 */
export function isAdmin(): boolean {
  const loginUserStore = useLoginUserStore()
  const loginUser = loginUserStore.loginUser
  return loginUser.userRole === 'admin'
}

/**
 * 检查用户是否已登录
 * @returns 是否已登录
 */
export function isLogin(): boolean {
  const loginUserStore = useLoginUserStore()
  return !!loginUserStore.loginUser.id
}


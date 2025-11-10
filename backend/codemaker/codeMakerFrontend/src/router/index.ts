import { createRouter, createWebHistory } from 'vue-router'
import { checkAccess } from '../access'
import { useLoginUserStore } from '../stores/loginUser'
import HomePage from '../pages/HomePage.vue'
import UserLoginPage from '../pages/user/UserLoginPage.vue'
import UserRegisterPage from '../pages/user/UserRegisterPage.vue'
import UserManagePage from '../pages/admin/UserManagePage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: HomePage,
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
    },
  ],

})

// 全局路由守卫，实现权限控制
router.beforeEach(async (to, from, next) => {
  // 如果访问 /admin 路径，需要先确保用户信息已加载
  if (to.path.startsWith('/admin')) {
    const loginUserStore = useLoginUserStore()
    // 如果用户信息还没有加载，先尝试获取
    if (!loginUserStore.loginUser.id) {
      await loginUserStore.fetchLoginUser()
    }
  }

  // 检查是否有权限访问目标路由
  const accessResult = checkAccess(to.path)
  if (accessResult.hasAccess) {
    next()
  } else {
    // 无权限访问，重定向到指定路径（如果没有指定，则重定向到首页）
    next(accessResult.redirectPath || '/')
  }
})

export default router

<template>
  <a-layout-header class="header">
    <a-row :wrap="false">
      <!-- 左侧：Logo和标题 -->
      <a-col flex="200px">
        <router-link to="/">
          <div class="header-left">
             <img class="logo" src="@/assets/logo.svg" alt="Logo" />
            <h1 class="site-title">零代码生成平台</h1>
          </div>
        </router-link>
      </a-col>
      <!-- 中间：导航菜单 -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </a-col>
      <!-- 右侧：用户操作区域 -->

      <a-col>
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown :trigger="['hover']">
              <template #overlay>
                <a-menu @click="handleUserMenuClick">
                  <a-menu-item key="logout">
                    <span>退出登录</span>
                  </a-menu-item>
                </a-menu>
              </template>
              <a-space style="cursor: pointer;">
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? '这是一个默认名字' }}
              </a-space>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>

      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { userLogout } from '../api/userController'

// JS 中引入 Store
import { useLoginUserStore } from '../stores/loginUser.ts'
const loginUserStore = useLoginUserStore()

const router = useRouter()
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

// 基础菜单配置项
const baseMenuItems = [
  {
    key: '/',
    label: '首页',
    title: '首页',
  },
  {
    key: '/about',
    label: '关于',
    title: '关于我们',
  },
]

// 根据用户权限动态生成菜单项
const menuItems = computed(() => {
  const items = [...baseMenuItems]
  // 如果是管理员，添加用户管理入口
  // 直接访问 store 中的 loginUser 以确保响应式追踪
  if (loginUserStore.loginUser.userRole === 'admin') {
    items.push({
      key: '/admin/userManage',
      label: '用户管理',
      title: '用户管理',
    })
  }
  return items
})

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

// 处理用户下拉菜单点击
const handleUserMenuClick: MenuProps['onClick'] = async (e) => {
  const key = e.key as string
  if (key === 'logout') {
    await handleLogout()
  }
}

/**
 * 退出登录
 */
const handleLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    // 清空登录用户信息
    loginUserStore.clearLoginUser()
    message.success('退出登录成功')
    // 跳转到首页
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.header {
  background: #fff;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  height: 48px;
  width: 48px;
}

.site-title {
  margin: 0;
  font-size: 18px;
  color: #1890ff;
}

.ant-menu-horizontal {
  border-bottom: none !important;
}
</style>

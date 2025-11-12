import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUser } from '@api/userController'

export const useLoginUserStore = defineStore('loginUser', () => {
  // 默认值
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  // 用于防止重复请求
  let fetching = false

  // 获取登录用户信息
  async function fetchLoginUser() {
    // 如果正在请求中，直接返回，避免重复请求
    if (fetching) {
      return
    }
    fetching = true
    try {
      const res = await getLoginUser()
      if (res.data.code === 0 && res.data.data) {
        loginUser.value = res.data.data
      }
    } finally {
      fetching = false
    }
  }
  // 更新登录用户信息
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  // 清空登录用户信息
  function clearLoginUser() {
    loginUser.value = {
      userName: '未登录',
    }
  }

  return { loginUser, setLoginUser, fetchLoginUser, clearLoginUser }
})

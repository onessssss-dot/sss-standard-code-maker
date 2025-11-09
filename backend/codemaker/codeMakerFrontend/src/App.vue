<script setup lang="ts">
import { onMounted } from 'vue'
import BasicLayout from '@/layouts/BasicLayout.vue'
import { useLoginUserStore } from '../src/stores/loginUser.ts'

const loginUserStore = useLoginUserStore()

// 只在组件挂载时获取一次登录用户信息
onMounted(() => {
  loginUserStore.fetchLoginUser()
})
</script>

<template>
  <BasicLayout />
</template>

<style scoped>
</style>

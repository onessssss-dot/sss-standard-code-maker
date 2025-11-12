// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 获取后端健康检查状态 GET /health/ */
export async function test(options?: { [key: string]: any }) {
  return request<string>('/health/', {
    method: 'GET',
    ...(options || {}),
  })
}

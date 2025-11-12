<template>
  <div class="user-manage-page">
    <!-- 搜索表单 -->
    <a-card class="search-card" :bordered="false">
      <a-form :model="queryParams" layout="inline">
        <a-form-item label="账号">
          <a-input
            v-model:value="queryParams.userAccount"
            placeholder="请输入账号"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input
            v-model:value="queryParams.userName"
            placeholder="请输入用户名"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="用户角色">
          <a-select
            v-model:value="queryParams.userRole"
            placeholder="请选择用户角色"
            allow-clear
            style="width: 200px"
          >
            <a-select-option value="">全部用户</a-select-option>
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员用户</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">查询</a-button>
          <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 数据表格 -->
    <a-card :bordered="false">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
    <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userAvatar'">
            <a-avatar :src="record.userAvatar" :size="40">
              {{ record.userName?.charAt(0)?.toUpperCase() }}
            </a-avatar>
      </template>
          <template v-else-if="column.key === 'userRole'">
            <a-tag :color="record.userRole === 'admin' ? 'red' : 'blue'">
              {{ record.userRole || 'user' }}
          </a-tag>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
      </template>
      <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
      </template>
    </template>
  </a-table>
    </a-card>

    <!-- 编辑用户模态框 -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑用户"
      @ok="handleEditSubmit"
      @cancel="handleEditCancel"
    >
      <a-form :model="editForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="用户ID">
          <a-input v-model:value="editForm.id" disabled />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input v-model:value="editForm.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="头像URL">
          <a-input v-model:value="editForm.userAvatar" placeholder="请输入头像URL" />
        </a-form-item>
        <a-form-item label="简介">
          <a-textarea
            v-model:value="editForm.userProfile"
            placeholder="请输入简介"
            :rows="3"
          />
        </a-form-item>
        <a-form-item label="用户角色">
          <a-input v-model:value="editForm.userRole" placeholder="请输入用户角色" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { listUserVoByPage, deleteUser, updateUser } from '@api/userController'
import { useLoginUserStore } from '@/stores/loginUser'

// 查询参数
const queryParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  userAccount: '',
  userName: '',
  userRole: '',
})

// 表格数据
const tableData = ref<API.UserVO[]>([])
const loading = ref(false)

// 获取当前登录用户信息
const loginUserStore = useLoginUserStore()

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

// 表格列定义
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    key: 'userAccount',
    width: 150,
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    key: 'userName',
    width: 150,
  },
  {
    title: '头像',
    key: 'userAvatar',
    width: 100,
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
    key: 'userProfile',
    ellipsis: true,
  },
  {
    title: '用户角色',
    key: 'userRole',
    width: 120,
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right' as const,
  },
]

// 编辑模态框
const editModalVisible = ref(false)
const editForm = reactive<API.UserUpdateRequest>({
  id: undefined,
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: '',
})

/**
 * 加载用户列表
 */
const loadUserList = async () => {
  loading.value = true
  try {
    // 构建查询参数，如果 userRole 为空字符串，则不包含该字段
    const params: API.UserQueryRequest = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      userAccount: queryParams.userAccount || undefined,
      userName: queryParams.userName || undefined,
    }
    // 只有当 userRole 有值（不为空字符串）时才添加到查询参数中
    // 全部用户时不传 userRole 参数
    if (queryParams.userRole) {
      params.userRole = queryParams.userRole
    }
    
    const res = await listUserVoByPage(params)
    if (res.data.code === 0 && res.data.data) {
      tableData.value = res.data.data.records || []
      pagination.total = res.data.data.totalRow || 0
      const pageNumber = res.data.data.pageNumber || queryParams.pageNum || 1
      pagination.current = pageNumber
      queryParams.pageNum = pageNumber
      pagination.pageSize = res.data.data.pageSize || queryParams.pageSize || 10
      queryParams.pageSize = pagination.pageSize
    } else {
      message.error('加载用户列表失败：' + res.data.message)
    }
  } catch (error) {
    message.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  queryParams.pageNum = 1
  pagination.current = 1
  loadUserList()
}

/**
 * 处理重置
 */
const handleReset = () => {
  queryParams.userAccount = ''
  queryParams.userName = ''
  queryParams.userRole = ''
  queryParams.pageNum = 1
  pagination.current = 1
  loadUserList()
}

/**
 * 处理表格变化（分页、排序等）
 */
const handleTableChange = (pag: any, _filters: any, _sorter: any) => {
  if (pag) {
    queryParams.pageNum = pag.current || pagination.current
    queryParams.pageSize = pag.pageSize || pagination.pageSize
    pagination.current = pag.current || pagination.current
    pagination.pageSize = pag.pageSize || pagination.pageSize
    loadUserList()
  }
}

/**
 * 处理编辑
 */
const handleEdit = (record: API.UserVO) => {
  editForm.id = record.id
  editForm.userName = record.userName || ''
  editForm.userAvatar = record.userAvatar || ''
  editForm.userProfile = record.userProfile || ''
  editForm.userRole = record.userRole || ''
  editModalVisible.value = true
}

/**
 * 处理编辑提交
 */
const handleEditSubmit = async () => {
  try {
    const res = await updateUser(editForm)
    if (res.data.code === 0) {
      message.success('更新用户成功')
      editModalVisible.value = false
      loadUserList()
    } else {
      message.error('更新用户失败：' + res.data.message)
    }
  } catch (error) {
    message.error('更新用户失败')
  }
}

/**
 * 处理编辑取消
 */
const handleEditCancel = () => {
  editModalVisible.value = false
  editForm.id = undefined
  editForm.userName = ''
  editForm.userAvatar = ''
  editForm.userProfile = ''
  editForm.userRole = ''
}

/**
 * 处理删除
 */
const handleDelete = (record: API.UserVO) => {
  // 检查是否要删除自己
  if (record.id === loginUserStore.loginUser.id) {
    message.warning('不能删除自己')
    return
  }

  Modal.confirm({
    title: '确认删除',
    content: `确定要删除用户 "${record.userName || record.userAccount}" 吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteUser({ id: record.id })
        if (res.data.code === 0) {
          message.success('删除用户成功')
          loadUserList()
        } else {
          message.error('删除用户失败：' + res.data.message)
        }
      } catch (error) {
        message.error('删除用户失败')
      }
    },
  })
}

/**
 * 格式化日期
 */
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

// 组件挂载时加载数据
onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.user-manage-page {
  padding: 0 24px 24px 24px;
}

.search-card {
  margin-bottom: 16px;
}
</style>

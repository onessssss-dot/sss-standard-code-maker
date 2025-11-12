<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  listMyAppVoByPage,
  listGoodAppVoByPage,
  updateApp,
  deleteApp,
} from '@api/appController'

const promptValue = ref('')
const quickPrompts = [
  '波普风电商页面',
  '企业网站',
  '电商运营后台',
  '暗黑话题社区',
]

const myAppFilters = reactive<{ appName: string; codeGenType?: string }>({
  appName: '',
  codeGenType: undefined,
})
const myAppPagination = reactive({ current: 1, pageSize: 6 })
const myAppList = ref<API.AppVO[]>([])
const myAppTotal = ref(0)
const myAppLoading = ref(false)

const curatedFilters = reactive<{ appName: string; codeGenType?: string }>({
  appName: '',
  codeGenType: undefined,
})
const curatedPagination = reactive({ current: 1, pageSize: 6 })
const curatedList = ref<API.AppVO[]>([])
const curatedTotal = ref(0)
const curatedLoading = ref(false)

const editModalVisible = ref(false)
const editSubmitting = ref(false)
const editForm = reactive<API.AppUpdateRequest>({
  id: undefined,
  appName: '',
})

const allCodeGenTypes = ref<string[]>([])

const codeGenTypeOptions = computed(() => allCodeGenTypes.value)

const collectCodeGenTypes = (apps: API.AppVO[]) => {
  const set = new Set(allCodeGenTypes.value)
  apps.forEach((app) => {
    if (app.codeGenType) {
      set.add(app.codeGenType)
    }
  })
  allCodeGenTypes.value = Array.from(set)
}

const buildAppQueryPayload = (
  filters: { appName: string; codeGenType?: string },
  pagination: { current: number; pageSize: number },
): API.AppQueryRequest => {
  const payload: API.AppQueryRequest = {
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
  }
  const name = filters.appName?.trim()
  if (name) {
    payload.appName = name
  }
  if (filters.codeGenType) {
    payload.codeGenType = filters.codeGenType
  }
  return payload
}

const loadMyApps = async () => {
  myAppLoading.value = true
  try {
    const res = await listMyAppVoByPage(
      buildAppQueryPayload(myAppFilters, myAppPagination),
    )
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      myAppList.value = data.records || []
      myAppTotal.value = data.totalRow || 0
      myAppPagination.current = data.pageNumber || myAppPagination.current
      myAppPagination.pageSize = data.pageSize || myAppPagination.pageSize
      collectCodeGenTypes(myAppList.value)
    } else {
      message.error(`加载我的应用失败：${res.data.message}`)
    }
  } catch (error) {
    console.error(error)
    message.error('加载我的应用失败，请稍后重试')
  } finally {
    myAppLoading.value = false
  }
}

const loadCuratedApps = async () => {
  curatedLoading.value = true
  try {
    const res = await listGoodAppVoByPage(
      buildAppQueryPayload(curatedFilters, curatedPagination),
    )
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      curatedList.value = data.records || []
      curatedTotal.value = data.totalRow || 0
      curatedPagination.current =
        data.pageNumber || curatedPagination.current
      curatedPagination.pageSize = data.pageSize || curatedPagination.pageSize
      collectCodeGenTypes(curatedList.value)
    } else {
      message.error(`加载精选应用失败：${res.data.message}`)
    }
  } catch (error) {
    console.error(error)
    message.error('加载精选应用失败，请稍后重试')
  } finally {
    curatedLoading.value = false
  }
}

const handlePromptSubmit = () => {
  if (!promptValue.value.trim()) {
    message.warning('请先输入想要创建的应用描述')
    return
  }
  message.info('提示词生成功能开发中，敬请期待～')
}

const handlePromptUpload = () => {
  message.info('文件上传功能开发中，敬请期待～')
}

const handlePromptOptimize = () => {
  message.info('智能优化功能开发中，敬请期待～')
}

const applyQuickPrompt = (prompt: string) => {
  promptValue.value = prompt
}

const handleMySearch = () => {
  myAppPagination.current = 1
  loadMyApps()
}

const handleMyReset = () => {
  myAppFilters.appName = ''
  myAppFilters.codeGenType = undefined
  handleMySearch()
}

const handleCuratedSearch = () => {
  curatedPagination.current = 1
  loadCuratedApps()
}

const handleCuratedReset = () => {
  curatedFilters.appName = ''
  curatedFilters.codeGenType = undefined
  handleCuratedSearch()
}

const handleMyPageChange = (page: number, pageSize?: number) => {
  myAppPagination.current = page
  if (pageSize) {
    myAppPagination.pageSize = pageSize
  }
  loadMyApps()
}

const handleCuratedPageChange = (page: number, pageSize?: number) => {
  curatedPagination.current = page
  if (pageSize) {
    curatedPagination.pageSize = pageSize
  }
  loadCuratedApps()
}

const formatDate = (value?: string) => {
  if (!value) {
    return ''
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

const handleOpenPreview = (app: API.AppVO) => {
  if (app.deployKey) {
    window.open(`/static/${app.deployKey}/index.html`, '_blank')
  } else {
    message.info('该应用暂未部署')
  }
}

const openEditModal = (app: API.AppVO) => {
  editForm.id = app.id
  editForm.appName = app.appName || ''
  editModalVisible.value = true
}

const handleEditCancel = () => {
  editModalVisible.value = false
  editForm.id = undefined
  editForm.appName = ''
}

const handleEditSubmit = async () => {
  if (!editForm.appName || !editForm.appName.trim()) {
    message.warning('请输入应用名称')
    return
  }
  editSubmitting.value = true
  try {
    const payload: API.AppUpdateRequest = {
      id: editForm.id,
      appName: editForm.appName.trim(),
    }
    const res = await updateApp(payload)
    if (res.data.code === 0) {
      message.success('更新成功')
      editModalVisible.value = false
      loadMyApps()
    } else {
      message.error(`更新失败：${res.data.message}`)
    }
  } catch (error) {
    console.error(error)
    message.error('更新应用失败，请稍后重试')
  } finally {
    editSubmitting.value = false
  }
}

const handleDeleteApp = (app: API.AppVO) => {
  if (!app.id) {
    message.warning('缺少应用信息，无法删除')
    return
  }
  Modal.confirm({
    title: '确认删除该应用？',
    content: `删除后将无法恢复：${app.appName || '未命名应用'}`,
    okText: '删除',
    okButtonProps: { danger: true },
    cancelText: '取消',
    async onOk() {
      try {
        const res = await deleteApp({ id: app.id })
        if (res.data.code === 0) {
          message.success('删除成功')
          loadMyApps()
        } else {
          message.error(`删除失败：${res.data.message}`)
        }
      } catch (error) {
        console.error(error)
        message.error('删除失败，请稍后重试')
      }
    },
  })
}

onMounted(() => {
  loadMyApps()
  loadCuratedApps()
})
</script>

<template>
  <main class="home-page">
    <section class="hero">
      <div class="hero-content">
        <h1 class="hero-title">一句话 · 星所想</h1>
        <p class="hero-subtitle">与 AI 对话轻松创建应用和网站</p>
        <a-card class="prompt-card" :bordered="false">
          <a-textarea
            v-model:value="promptValue"
            :auto-size="{ minRows: 4, maxRows: 6 }"
            placeholder="使用 NoCode 创建一个高效的小工具，帮我计算……"
          />
          <div class="prompt-actions">
            <a-space size="middle">
              <a-button @click="handlePromptUpload">上传</a-button>
              <a-button @click="handlePromptOptimize">优化</a-button>
              <a-button type="primary" @click="handlePromptSubmit">
                开始创建
              </a-button>
            </a-space>
          </div>
          <div class="prompt-suggestions">
            <span class="suggestion-title">精选示例</span>
            <a-space wrap>
              <a-tag
                v-for="tag in quickPrompts"
                :key="tag"
                class="suggestion-tag"
                @click="applyQuickPrompt(tag)"
              >
                {{ tag }}
              </a-tag>
            </a-space>
          </div>
        </a-card>
      </div>
    </section>

    <section class="app-section">
      <div class="section-header">
        <div>
          <h2>我的应用</h2>
          <p class="section-desc">管理你创建的项目并快速重命名</p>
        </div>
      </div>
      <a-card class="section-card" :bordered="false">
        <a-form layout="inline" class="filter-form" :model="myAppFilters">
          <a-form-item label="应用名称">
            <a-input
              v-model:value="myAppFilters.appName"
              placeholder="输入名称关键词"
              allow-clear
              style="width: 220px"
            />
          </a-form-item>
          <a-form-item v-if="codeGenTypeOptions.length" label="生成类型">
            <a-select
              v-model:value="myAppFilters.codeGenType"
              allow-clear
              placeholder="选择生成类型"
              style="width: 200px"
            >
              <a-select-option
                v-for="type in codeGenTypeOptions"
                :key="type"
                :value="type"
              >
                {{ type }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="handleMySearch">查询</a-button>
              <a-button @click="handleMyReset">重置</a-button>
            </a-space>
          </a-form-item>
        </a-form>
        <a-spin :spinning="myAppLoading">
          <a-list
            class="app-list"
            :grid="{ gutter: 16, column: 3 }"
            :data-source="myAppList"
            :locale="{ emptyText: '暂无应用，快去创建一个吧～' }"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-card
                  class="app-card"
                  :title="item.appName || '未命名应用'"
                  hoverable
                >
                  <template #cover>
                    <div class="cover-wrapper">
                      <img v-if="item.cover" :src="item.cover" alt="封面" />
                      <div v-else class="cover-placeholder">
                        <span>{{ item.appName?.charAt(0) || 'A' }}</span>
                      </div>
                    </div>
                  </template>
                  <a-typography-paragraph :ellipsis="{ rows: 2 }">
                    {{ item.initPrompt || '这个应用还没有填写描述' }}
                  </a-typography-paragraph>
                  <div class="app-meta">
                    <a-tag v-if="item.codeGenType" color="blue">
                      {{ item.codeGenType }}
                    </a-tag>
                    <span class="meta-time" v-if="item.updateTime">
                      更新于 {{ formatDate(item.updateTime) }}
                    </span>
                  </div>
                  <a-space class="card-actions">
                    <a-button size="small" @click="openEditModal(item)">
                      重命名
                    </a-button>
                    <a-button
                      size="small"
                      type="link"
                      @click="handleOpenPreview(item)"
                    >
                      预览
                    </a-button>
                    <a-button
                      size="small"
                      danger
                      type="link"
                      @click="handleDeleteApp(item)"
                    >
                      删除
                    </a-button>
                  </a-space>
                </a-card>
              </a-list-item>
            </template>
          </a-list>
          <div
            class="pagination-wrapper"
            v-if="myAppTotal > myAppPagination.pageSize"
          >
            <a-pagination
              :current="myAppPagination.current"
              :page-size="myAppPagination.pageSize"
              :total="myAppTotal"
              show-size-changer
              show-quick-jumper
              @change="handleMyPageChange"
              @showSizeChange="handleMyPageChange"
            />
          </div>
        </a-spin>
      </a-card>
    </section>

    <section class="app-section">
      <div class="section-header">
        <div>
          <h2>精选应用</h2>
          <p class="section-desc">灵感案例，看看大家都在做什么</p>
        </div>
      </div>
      <a-card class="section-card" :bordered="false">
        <a-form layout="inline" class="filter-form" :model="curatedFilters">
          <a-form-item label="应用名称">
            <a-input
              v-model:value="curatedFilters.appName"
              placeholder="输入名称关键词"
              allow-clear
              style="width: 220px"
            />
          </a-form-item>
          <a-form-item v-if="codeGenTypeOptions.length" label="生成类型">
            <a-select
              v-model:value="curatedFilters.codeGenType"
              allow-clear
              placeholder="选择生成类型"
              style="width: 200px"
            >
              <a-select-option
                v-for="type in codeGenTypeOptions"
                :key="type"
                :value="type"
              >
                {{ type }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="handleCuratedSearch">
                查询
              </a-button>
              <a-button @click="handleCuratedReset">重置</a-button>
            </a-space>
          </a-form-item>
        </a-form>
        <a-spin :spinning="curatedLoading">
          <a-list
            class="app-list"
            :grid="{ gutter: 16, column: 3 }"
            :data-source="curatedList"
            :locale="{ emptyText: '暂时没有推荐的应用' }"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-card
                  class="app-card"
                  :title="item.appName || '未命名应用'"
                  hoverable
                >
                  <template #cover>
                    <div class="cover-wrapper">
                      <img v-if="item.cover" :src="item.cover" alt="封面" />
                      <div v-else class="cover-placeholder">
                        <span>{{ item.appName?.charAt(0) || 'A' }}</span>
                      </div>
                    </div>
                  </template>
                  <a-typography-paragraph :ellipsis="{ rows: 2 }">
                    {{ item.initPrompt || '这个应用还没有填写描述' }}
                  </a-typography-paragraph>
                  <div class="app-meta">
                    <a-tag v-if="item.codeGenType" color="green">
                      {{ item.codeGenType }}
                    </a-tag>
                    <span class="meta-time" v-if="item.updateTime">
                      更新于 {{ formatDate(item.updateTime) }}
                    </span>
                  </div>
                  <a-space class="card-actions">
                    <a-button type="link" size="small" @click="handleOpenPreview(item)">
                      预览
                    </a-button>
                  </a-space>
                </a-card>
              </a-list-item>
            </template>
          </a-list>
          <div
            class="pagination-wrapper"
            v-if="curatedTotal > curatedPagination.pageSize"
          >
            <a-pagination
              :current="curatedPagination.current"
              :page-size="curatedPagination.pageSize"
              :total="curatedTotal"
              show-size-changer
              show-quick-jumper
              @change="handleCuratedPageChange"
              @showSizeChange="handleCuratedPageChange"
            />
          </div>
        </a-spin>
      </a-card>
    </section>

    <a-modal
      v-model:open="editModalVisible"
      title="修改应用名称"
      :confirm-loading="editSubmitting"
      destroy-on-close
      @ok="handleEditSubmit"
      @cancel="handleEditCancel"
    >
      <a-form :model="editForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="应用名称">
          <a-input
            v-model:value="editForm.appName"
            placeholder="请输入新的应用名称"
            allow-clear
            maxlength="50"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </main>
</template>

<style scoped>
.home-page {
  padding: 48px 0 80px;
}

.hero {
  display: flex;
  justify-content: center;
  padding: 0 24px;
  margin-bottom: 48px;
}

.hero-content {
  max-width: 1200px;
  text-align: center;
}

.hero-title {
  font-size: 40px;
  font-weight: 700;
  margin-bottom: 12px;
  color: #1a1a1a;
}

.hero-subtitle {
  font-size: 18px;
  color: #5c6b89;
  margin-bottom: 24px;
}

.prompt-card {
  width: 100%;
  padding: 24px;
  box-shadow:
    0 12px 40px rgba(46, 91, 255, 0.08),
    0 4px 8px rgba(15, 38, 67, 0.08);
  border-radius: 16px;
  background: #ffffff;
}

.prompt-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.prompt-suggestions {
  margin-top: 16px;
  text-align: left;
}

.suggestion-title {
  font-size: 14px;
  color: #5c6b89;
  margin-right: 12px;
}

.suggestion-tag {
  cursor: pointer;
}

.app-section {
  max-width: 1200px;
  margin: 0 auto 48px;
  padding: 0 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h2 {
  margin: 0;
  font-size: 24px;
}

.section-desc {
  margin: 4px 0 0;
  color: #5c6b89;
}

.section-card {
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(15, 38, 67, 0.05);
}

.filter-form {
  margin-bottom: 16px;
}

.app-list {
  min-height: 240px;
}

.app-card {
  border-radius: 16px;
  overflow: hidden;
  min-height: 320px;
  display: flex;
  flex-direction: column;
}

.cover-wrapper {
  height: 160px;
  background: linear-gradient(135deg, #e8f1ff 0%, #f5f5ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.cover-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  font-size: 32px;
  color: #5c6b89;
  font-weight: 600;
}

.app-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.meta-time {
  font-size: 12px;
  color: #8a94ab;
}

.card-actions {
  margin-top: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 1024px) {
  .hero-title {
    font-size: 32px;
  }

  .app-card {
    min-height: 300px;
  }
}
</style>

<script setup lang="ts">
import { onMounted, ref, h } from 'vue'
import {
  Row, Col, Card, Statistic, Upload, UploadDragger, Button, Table, Tag,
  List, ListItem, Textarea, Empty, message, TypographyParagraph, TypographyTitle,
} from 'ant-design-vue'
import {
  InboxOutlined,
  SendOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import { http } from '../api/http'
import type { ApiResponse, KnowledgeDocument } from '../types'
import { translateKnowledgeLine } from '../utils/display'

const docs = ref<KnowledgeDocument[]>([])
const stats = ref<Record<string, number>>({})
const question = ref('')
const streamOutput = ref<string[]>([])
const uploading = ref(false)

const load = async () => {
  const [docRes, statsRes] = await Promise.all([
    http.get<ApiResponse<KnowledgeDocument[]>>('/knowledge-bases'),
    http.get<ApiResponse<Record<string, number>>>('/knowledge-bases/stats'),
  ])
  docs.value = docRes.data.data
  stats.value = statsRes.data.data
}

const customUpload = async (options: any) => {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    await http.post('/knowledge-bases', formData)
    message.success('上传成功')
    await load()
    options.onSuccess()
  } catch {
    options.onError()
  } finally {
    uploading.value = false
  }
}

const ask = () => {
  streamOutput.value = []
  const source = new EventSource(
    `/api/knowledge-bases/chat?question=${encodeURIComponent(question.value)}`
  )
  source.onmessage = (event) => {
    streamOutput.value.push(translateKnowledgeLine(event.data))
  }
  source.onerror = () => source.close()
  message.info('问答已开始')
}

const download = (id: number) => {
  window.open(`/api/knowledge-bases/${id}/download`, '_blank')
}

const columns = [
  { title: '文件名', dataIndex: 'fileName', key: 'fileName' },
  { title: '摘要', dataIndex: 'summary', key: 'summary', ellipsis: true },
  {
    title: '分块数',
    dataIndex: 'chunks',
    key: 'chunks',
    customRender: ({ text }: { text: number }) => h(Tag, { color: 'blue' }, () => `${text} 个分块`),
  },
  { title: 'Token 估算', dataIndex: 'tokenEstimate', key: 'tokenEstimate' },
  {
    title: '关键词',
    dataIndex: 'keywords',
    key: 'keywords',
    customRender: ({ text }: { text: string[] }) => {
      return h('div', { style: 'display: flex; gap: 4px; flex-wrap: wrap' },
        text.map((k: string) => h(Tag, { key: k }, () => k))
      )
    },
  },
  {
    title: '操作',
    key: 'action',
    customRender: ({ record }: { record: KnowledgeDocument }) => {
      return h(Button, { type: 'link', size: 'small', onClick: () => download(record.id) }, () => [h(DownloadOutlined), ' 下载'])
    },
  },
]

onMounted(load)
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 24px">
    <Row :gutter="[16, 16]">
      <Col :xs="24" :sm="8">
        <Card><Statistic title="文档数" :value="stats.totalDocuments ?? 0" /></Card>
      </Col>
      <Col :xs="24" :sm="8">
        <Card><Statistic title="分块数" :value="stats.totalChunks ?? 0" /></Card>
      </Col>
      <Col :xs="24" :sm="8">
        <Card><Statistic title="估算 Token" :value="stats.totalTokens ?? 0" /></Card>
      </Col>
    </Row>

    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="12">
        <Card title="上传资料">
          <UploadDragger :custom-request="customUpload" :show-upload-list="false" :disabled="uploading">
            <p><InboxOutlined style="font-size: 48px; color: var(--ant-color-primary)" /></p>
            <p>点击或拖拽文件到此区域上传</p>
          </UploadDragger>
        </Card>
      </Col>
      <Col :xs="24" :lg="12">
        <Card title="发起提问">
          <a-textarea v-model:value="question" :rows="4" placeholder="输入一个与知识库相关的问题" style="margin-bottom: 12px" />
          <Button type="primary" @click="ask">
            <template #icon><SendOutlined /></template>
            开始流式问答
          </Button>
          <div v-if="streamOutput.length" style="margin-top: 16px; padding: 12px; background: var(--ant-color-bg-layout); border-radius: 8px; max-height: 200px; overflow: auto">
            <TypographyParagraph v-for="(line, i) in streamOutput" :key="i" style="margin: 4px 0">{{ line }}</TypographyParagraph>
          </div>
        </Card>
      </Col>
    </Row>

    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="14">
        <Card title="资料列表">
          <Table :columns="columns" :data-source="docs" row-key="id" :pagination="false" size="middle">
            <template #emptyText><Empty description="暂无资料" /></template>
          </Table>
        </Card>
      </Col>
      <Col :xs="24" :lg="10">
        <Card title="问答记录">
          <List v-if="streamOutput.length > 0" :data-source="streamOutput" size="small">
            <template #renderItem="{ item }">
              <ListItem>{{ item }}</ListItem>
            </template>
          </List>
          <Empty v-else description="暂无问答记录" />
        </Card>
      </Col>
    </Row>
  </div>
</template>

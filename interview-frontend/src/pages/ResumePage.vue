<script setup lang="ts">
import { computed, onMounted, ref, h } from 'vue'
import {
  Row, Col, Card, Statistic, Upload, UploadDragger, Button, Table, Tag, Progress,
  Descriptions, DescriptionsItem, Popconfirm, Spin, Empty, message,
  TypographyTitle,
} from 'ant-design-vue'
import {
  InboxOutlined,
  DownloadOutlined,
  RedoOutlined,
} from '@ant-design/icons-vue'
import { http } from '../api/http'
import type { ApiResponse, ResumeRecord } from '../types'
import { formatDateTime, formatResumeStatus } from '../utils/display'

const resumes = ref<ResumeRecord[]>([])
const stats = ref<Record<string, number>>({})
const loading = ref(false)
const uploading = ref(false)

const selectedResume = computed(() => resumes.value[0] ?? null)

const load = async () => {
  loading.value = true
  try {
    const [resumeRes, statsRes] = await Promise.all([
      http.get<ApiResponse<ResumeRecord[]>>('/resumes'),
      http.get<ApiResponse<Record<string, number>>>('/resumes/stats'),
    ])
    resumes.value = resumeRes.data.data
    stats.value = statsRes.data.data
  } finally {
    loading.value = false
  }
}

const customUpload = async (options: any) => {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    await http.post('/resumes', formData)
    message.success('上传成功')
    await load()
    options.onSuccess()
  } catch {
    options.onError()
  } finally {
    uploading.value = false
  }
}

const retryResume = async (id: number) => {
  await http.post(`/resumes/${id}/retry`)
  message.success('已重新提交分析')
  await load()
}

const downloadReport = (id: number) => {
  window.open(`/api/resumes/${id}/report`, '_blank')
}

const columns = [
  { title: '文件名', dataIndex: 'fileName', key: 'fileName' },
  { title: '候选人', dataIndex: 'candidateName', key: 'candidateName' },
  { title: '目标岗位', dataIndex: 'targetPosition', key: 'targetPosition' },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    customRender: ({ text }: { text: string }) => {
      const colorMap: Record<string, string> = {
        PENDING: 'default',
        ANALYZING: 'processing',
        COMPLETED: 'success',
        FAILED: 'error',
      }
      return h(Tag, { color: colorMap[text] ?? 'default' }, () => formatResumeStatus(text))
    },
  },
  {
    title: '进度',
    dataIndex: 'progress',
    key: 'progress',
    customRender: ({ text }: { text: number }) => h(Progress, { percent: text, size: 'small' }),
  },
  {
    title: '操作',
    key: 'action',
    customRender: ({ record }: { record: ResumeRecord }) => {
      return h('div', { style: 'display: flex; gap: 8px' }, [
        h(Button, { type: 'link', size: 'small', onClick: () => downloadReport(record.id) }, () => [h(DownloadOutlined), ' 下载']),
        h(Popconfirm, {
          title: '确定重新分析？',
          onConfirm: () => retryResume(record.id),
        }, () => h(Button, { type: 'link', size: 'small', disabled: record.status !== 'FAILED' }, () => [h(RedoOutlined), ' 重试'])),
      ])
    },
  },
]

onMounted(load)
</script>

<template>
  <Spin :spinning="loading">
    <div style="display: flex; flex-direction: column; gap: 24px">
      <Row :gutter="[16, 16]">
        <Col :xs="24" :sm="8">
          <Card><Statistic title="总简历数" :value="stats.total ?? 0" /></Card>
        </Col>
        <Col :xs="24" :sm="8">
          <Card><Statistic title="已完成" :value="stats.completed ?? 0" /></Card>
        </Col>
        <Col :xs="24" :sm="8">
          <Card><Statistic title="失败" :value="stats.failed ?? 0" /></Card>
        </Col>
      </Row>

      <Card title="简历上传">
        <UploadDragger :custom-request="customUpload" :show-upload-list="false" :disabled="uploading">
          <p><InboxOutlined style="font-size: 48px; color: var(--ant-color-primary)" /></p>
          <p>点击或拖拽简历文件到此区域上传</p>
        </UploadDragger>
      </Card>

      <Row :gutter="[16, 16]">
        <Col :xs="24" :lg="14">
          <Card title="分析队列">
            <Table :columns="columns" :data-source="resumes" row-key="id" :pagination="false" size="middle">
              <template #emptyText><Empty description="暂无简历数据" /></template>
            </Table>
          </Card>
        </Col>
        <Col :xs="24" :lg="10">
          <Card title="最新分析结果" v-if="selectedResume">
            <Descriptions :column="1" size="small" bordered>
              <DescriptionsItem label="候选人">{{ selectedResume.candidateName }}</DescriptionsItem>
              <DescriptionsItem label="目标岗位">{{ selectedResume.targetPosition }}</DescriptionsItem>
              <DescriptionsItem label="更新时间">{{ formatDateTime(selectedResume.updatedAt) }}</DescriptionsItem>
              <DescriptionsItem label="分析摘要">{{ selectedResume.analysisSummary }}</DescriptionsItem>
            </Descriptions>
            <div style="margin-top: 16px">
              <TypographyTitle :level="5">优势亮点</TypographyTitle>
              <div style="display: flex; flex-wrap: wrap; gap: 6px">
                <Tag v-for="item in selectedResume.strengths" :key="item" color="green">{{ item }}</Tag>
              </div>
            </div>
            <div style="margin-top: 12px">
              <TypographyTitle :level="5">风险提示</TypographyTitle>
              <div style="display: flex; flex-wrap: wrap; gap: 6px">
                <Tag v-for="item in selectedResume.risks" :key="item" color="red">{{ item }}</Tag>
              </div>
            </div>
          </Card>
          <Card v-else><Empty description="暂无分析结果" /></Card>
        </Col>
      </Row>
    </div>
  </Spin>
</template>

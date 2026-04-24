<script setup lang="ts">
import { onMounted, ref, h } from 'vue'
import {
  Row, Col, Card, Statistic, Table, Tag, Button, ButtonGroup,
  Collapse, CollapsePanel, List, ListItem, Textarea, Empty, message,
} from 'ant-design-vue'
import {
  ThunderboltOutlined,
} from '@ant-design/icons-vue'
import { http } from '../api/http'
import type { ApiResponse, InterviewSchedule } from '../types'
import { formatDateTime, formatScheduleStatus } from '../utils/display'

const schedules = ref<InterviewSchedule[]>([])
const overview = ref<Record<string, number>>({})
const calendar = ref<Record<string, InterviewSchedule[]>>({})
const invitation = ref('')

const load = async () => {
  const [scheduleRes, overviewRes, calendarRes] = await Promise.all([
    http.get<ApiResponse<InterviewSchedule[]>>('/interview-schedules'),
    http.get<ApiResponse<Record<string, number>>>('/interview-schedules/overview'),
    http.get<ApiResponse<Record<string, InterviewSchedule[]>>>('/interview-schedules/calendar'),
  ])
  schedules.value = scheduleRes.data.data
  overview.value = overviewRes.data.data
  calendar.value = calendarRes.data.data
}

const parseInvitation = async () => {
  if (!invitation.value.trim()) return
  await http.post('/interview-schedules/parse', { content: invitation.value })
  invitation.value = ''
  message.success('邀请解析成功')
  await load()
}

const updateStatus = async (id: number, status: string) => {
  await http.post(`/interview-schedules/${id}/status`, { status })
  message.success('状态已更新')
  await load()
}

const columns = [
  { title: '公司/岗位', key: 'company', customRender: ({ record }: { record: InterviewSchedule }) => `${record.company} / ${record.position}` },
  { title: '来源', dataIndex: 'source', key: 'source' },
  { title: '时间', key: 'time', customRender: ({ record }: { record: InterviewSchedule }) => formatDateTime(record.startTime) },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    customRender: ({ text }: { text: string }) => {
      const colorMap: Record<string, string> = {
        PENDING: 'processing',
        COMPLETED: 'success',
        CANCELED: 'default',
        EXPIRED: 'warning',
      }
      return h(Tag, { color: colorMap[text] ?? 'default' }, () => formatScheduleStatus(text))
    },
  },
  { title: '会议链接', dataIndex: 'meetingLink', key: 'meetingLink', ellipsis: true },
  {
    title: '操作',
    key: 'action',
    customRender: ({ record }: { record: InterviewSchedule }) => {
      return h(ButtonGroup, {}, () => [
        h(Button, { size: 'small', onClick: () => updateStatus(record.id, 'PENDING') }, () => '待面试'),
        h(Button, { size: 'small', onClick: () => updateStatus(record.id, 'COMPLETED') }, () => '完成'),
        h(Button, { size: 'small', danger: true, onClick: () => updateStatus(record.id, 'CANCELED') }, () => '取消'),
      ])
    },
  },
]

onMounted(load)
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 24px">
    <Row :gutter="[16, 16]">
      <Col :xs="12" :sm="6">
        <Card><Statistic title="总数" :value="overview.total ?? 0" /></Card>
      </Col>
      <Col :xs="12" :sm="6">
        <Card><Statistic title="待面试" :value="overview.pending ?? 0" /></Card>
      </Col>
      <Col :xs="12" :sm="6">
        <Card><Statistic title="已完成" :value="overview.completed ?? 0" /></Card>
      </Col>
      <Col :xs="12" :sm="6">
        <Card><Statistic title="已过期" :value="overview.expired ?? 0" /></Card>
      </Col>
    </Row>

    <Card title="邀请解析">
      <a-textarea v-model:value="invitation" :rows="4" placeholder="粘贴飞书、腾讯会议、Zoom 或邮件中的面试邀请内容" style="margin-bottom: 12px" />
      <Button type="primary" @click="parseInvitation">
        <template #icon><ThunderboltOutlined /></template>
        解析邀请
      </Button>
    </Card>

    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="14">
        <Card title="安排列表">
          <Table :columns="columns" :data-source="schedules" row-key="id" :pagination="false" size="middle">
            <template #emptyText><Empty description="暂无面试安排" /></template>
          </Table>
        </Card>
      </Col>
      <Col :xs="24" :lg="10">
        <Card title="按日期查看">
          <Collapse v-if="Object.keys(calendar).length" accordion>
            <CollapsePanel v-for="(items, day) in calendar" :key="day" :header="`${day} (${items.length} 场)`">
              <List :data-source="items" size="small">
                <template #renderItem="{ item }">
                  <ListItem>
                    {{ item.company }} / {{ item.position }} / {{ formatDateTime(item.startTime) }}
                  </ListItem>
                </template>
              </List>
            </CollapsePanel>
          </Collapse>
          <Empty v-else description="暂无日历数据" />
        </Card>
      </Col>
    </Row>
  </div>
</template>

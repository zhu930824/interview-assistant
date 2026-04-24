<script setup lang="ts">
import { Row, Col, Card, Statistic, List, ListItem, ListItemMeta, Tag, Timeline, TimelineItem, Button, TypographyTitle, TypographyParagraph, TypographyText } from 'ant-design-vue'
import {
  FileAddOutlined,
  SolutionOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  RocketOutlined,
  SafetyCertificateOutlined,
  ThunderboltOutlined,
  AudioOutlined,
  DatabaseOutlined,
  CloudServerOutlined,
  ApiOutlined,
  TeamOutlined,
} from '@ant-design/icons-vue'
import { RouterLink } from 'vue-router'

const overviewStats = [
  { title: '本周新增简历', value: 128, icon: FileAddOutlined, suffix: '' },
  { title: '进行中模拟面试', value: 32, icon: SolutionOutlined, suffix: '' },
  { title: '待处理面试安排', value: 14, icon: CalendarOutlined, suffix: '' },
  { title: '知识库命中率', value: 92, icon: CheckCircleOutlined, suffix: '%' },
]

const todoList = [
  '完成字节专项模拟面试的二轮追问配置',
  '确认今晚 19:30 的 Zoom 技术面链接有效',
  '补充系统设计方向知识库文档并重新向量化',
  '导出候选人王晨的最新模拟面试评估报告',
]

const moduleCards = [
  { title: '简历管理', text: '处理简历上传、内容解析、去重校验、异步分析与 PDF 报告导出。', icon: FileAddOutlined, link: '/resumes' },
  { title: '模拟面试', text: '支持 Skill 驱动出题、阶段时长联动、追问策略和统一评估。', icon: RocketOutlined, link: '/interviews' },
  { title: '语音面试', text: '提供实时语音对话、字幕回显、暂停恢复与手动提交能力。', icon: AudioOutlined, link: '/voice-interviews' },
  { title: '面试安排', text: '解析邀请内容、展示日程视图、管理状态流转和面试提醒。', icon: CalendarOutlined, link: '/schedules' },
  { title: '知识库', text: '完成文档上传、分块向量化、RAG 检索增强和流式问答。', icon: DatabaseOutlined, link: '/knowledge' },
  { title: '运营视图', text: '沉淀简历、面试、安排和知识使用情况，形成统一后台数据面板。', icon: CloudServerOutlined, link: '/' },
]

const capabilities = [
  '多格式简历解析',
  'Redis Stream 异步流',
  'Skill 驱动出题',
  '统一评估引擎',
  '实时语音问答',
  'RAG 检索问答',
]

const workflowSteps = [
  { title: '简历管理', desc: '先在简历管理完成上传与解析，再根据分析结果决定是否进入模拟面试。' },
  { title: '面试训练', desc: '对于重点候选人，优先结合知识库补充岗位材料，再进入文字或语音面试训练。' },
  { title: '安排协同', desc: '最后通过面试安排页统一管理邀请、提醒和状态流转，避免流程散落。' },
]

const pricingPlans = [
  {
    name: '免费版',
    price: '¥0',
    period: '/月',
    features: ['5 份简历分析/月', '3 次模拟面试/月', '基础知识库', '邮件支持'],
    featured: false,
  },
  {
    name: '专业版',
    price: '¥99',
    period: '/月',
    features: ['无限简历分析', '无限模拟面试', '语音面试', 'RAG 流式问答', '优先支持'],
    featured: true,
  },
  {
    name: '企业版',
    price: '¥399',
    period: '/月',
    features: ['全部专业版功能', '团队协作', 'API 接入', '自定义品牌', '专属客户经理'],
    featured: false,
  },
]
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 24px">
    <!-- Hero stats -->
    <Row :gutter="[16, 16]">
      <Col v-for="stat in overviewStats" :key="stat.title" :xs="24" :sm="12" :lg="6">
        <Card hoverable>
          <Statistic :title="stat.title" :value="stat.value" :suffix="stat.suffix">
            <template #prefix>
              <component :is="stat.icon" style="margin-right: 4px" />
            </template>
          </Statistic>
        </Card>
      </Col>
    </Row>

    <!-- Chart + Todo -->
    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="14">
        <Card title="面试趋势">
          <div style="display: flex; align-items: flex-end; gap: 12px; height: 180px; padding: 16px 0">
            <div
              v-for="(h, i) in [42, 60, 74, 56, 88, 68, 80, 52, 90, 65, 78, 85]"
              :key="i"
              style="flex: 1; border-radius: 6px 6px 2px 2px; transition: height 0.3s"
              :style="{
                height: h + '%',
                background: 'linear-gradient(180deg, var(--ant-color-primary), var(--ant-color-primary-bg))',
                opacity: 0.85,
              }"
            />
          </div>
          <Row :gutter="16" style="margin-top: 12px">
            <Col :span="8">
              <Card size="small">
                <Statistic title="本周面试" :value="68" />
              </Card>
            </Col>
            <Col :span="8">
              <Card size="small">
                <Statistic title="通过率" :value="76" suffix="%" />
              </Card>
            </Col>
            <Col :span="8">
              <Card size="small">
                <Statistic title="平均时长" :value="35" suffix="分钟" />
              </Card>
            </Col>
          </Row>
        </Card>
      </Col>
      <Col :xs="24" :lg="10">
        <Card title="今日待办">
          <template #extra><Tag>4 项</Tag></template>
          <List :data-source="todoList" size="small">
            <template #renderItem="{ item }">
              <ListItem>
                <ListItemMeta>
                  <template #title>
                    <TypographyText>{{ item }}</TypographyText>
                  </template>
                </ListItemMeta>
              </ListItem>
            </template>
          </List>
        </Card>
      </Col>
    </Row>

    <!-- Feature cards -->
    <Card title="模块入口">
      <Row :gutter="[16, 16]">
        <Col v-for="card in moduleCards" :key="card.title" :xs="24" :sm="12" :lg="8">
          <Card hoverable style="cursor: pointer; height: 100%">
            <RouterLink :to="card.link" style="color: inherit; text-decoration: none">
              <Card.Meta>
                <template #avatar>
                  <component :is="card.icon" style="font-size: 28px; color: var(--ant-color-primary)" />
                </template>
                <template #title>{{ card.title }}</template>
                <template #description>{{ card.text }}</template>
              </Card.Meta>
            </RouterLink>
          </Card>
        </Col>
      </Row>
    </Card>

    <!-- Bottom row: capabilities + workflow -->
    <Row :gutter="[16, 16]">
      <Col :xs="24" :lg="12">
        <Card title="平台能力">
          <div style="display: flex; flex-wrap: wrap; gap: 8px">
            <Tag v-for="cap in capabilities" :key="cap" color="blue">{{ cap }}</Tag>
          </div>
        </Card>
      </Col>
      <Col :xs="24" :lg="12">
        <Card title="推荐操作流程">
          <Timeline>
            <TimelineItem v-for="step in workflowSteps" :key="step.title" color="blue">
              <TypographyTitle :level="5" style="margin: 0">{{ step.title }}</TypographyTitle>
              <TypographyParagraph type="secondary" style="margin: 4px 0 0">{{ step.desc }}</TypographyParagraph>
            </TimelineItem>
          </Timeline>
        </Card>
      </Col>
    </Row>

    <!-- Pricing -->
    <Card title="版本定价">
      <Row :gutter="[16, 16]">
        <Col v-for="plan in pricingPlans" :key="plan.name" :xs="24" :sm="8">
          <Card
            :bordered="plan.featured"
            :style="plan.featured ? 'border-color: var(--ant-color-primary); box-shadow: 0 0 0 1px var(--ant-color-primary)' : ''"
          >
            <div v-if="plan.featured" style="margin-bottom: 12px">
              <Tag color="blue">推荐</Tag>
            </div>
            <TypographyTitle :level="3" style="margin: 0">{{ plan.name }}</TypographyTitle>
            <div style="margin: 12px 0">
              <TypographyTitle :level="2" style="margin: 0; display: inline">{{ plan.price }}</TypographyTitle>
              <TypographyText type="secondary">{{ plan.period }}</TypographyText>
            </div>
            <List :data-source="plan.features" size="small" :split="false">
              <template #renderItem="{ item }">
                <ListItem style="padding: 4px 0; border: none">
                  <TypographyText>{{ item }}</TypographyText>
                </ListItem>
              </template>
            </List>
            <Button type="primary" block style="margin-top: 16px" :ghost="!plan.featured">
              {{ plan.featured ? '立即开始' : '选择版本' }}
            </Button>
          </Card>
        </Col>
      </Row>
    </Card>
  </div>
</template>

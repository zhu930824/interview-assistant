<script setup lang="ts">
import { onMounted, ref } from "vue";
import PageHeader from "../components/PageHeader.vue";
import { http } from "../api/http";
import type { ApiResponse, InterviewSchedule } from "../types";
import {
  formatDateTime,
  formatScheduleStatus
} from "../utils/display";

const schedules = ref<InterviewSchedule[]>([]);
const overview = ref<Record<string, number>>({});
const calendar = ref<Record<string, InterviewSchedule[]>>({});
const invitation = ref("");

const load = async () => {
  const [scheduleResponse, overviewResponse, calendarResponse] = await Promise.all([
    http.get<ApiResponse<InterviewSchedule[]>>("/interview-schedules"),
    http.get<ApiResponse<Record<string, number>>>("/interview-schedules/overview"),
    http.get<ApiResponse<Record<string, InterviewSchedule[]>>>("/interview-schedules/calendar")
  ]);
  schedules.value = scheduleResponse.data.data;
  overview.value = overviewResponse.data.data;
  calendar.value = calendarResponse.data.data;
};

const parseInvitation = async () => {
  if (!invitation.value.trim()) return;
  await http.post("/interview-schedules/parse", { content: invitation.value });
  invitation.value = "";
  await load();
};

const updateStatus = async (id: number, status: string) => {
  await http.post(`/interview-schedules/${id}/status`, { status });
  await load();
};

onMounted(load);
</script>

<template>
  <section class="page">
    <PageHeader
      title="面试安排"
      subtitle="解析邀请文本、维护状态流转，并按日期查看轻量级日历视图。"
    />

    <div class="grid four">
      <div class="stat">
        <span class="muted">总数</span>
        <strong>{{ overview.total ?? 0 }}</strong>
      </div>
      <div class="stat">
        <span class="muted">待面试</span>
        <strong>{{ overview.pending ?? 0 }}</strong>
      </div>
      <div class="stat">
        <span class="muted">已完成</span>
        <strong>{{ overview.completed ?? 0 }}</strong>
      </div>
      <div class="stat">
        <span class="muted">已过期</span>
        <strong>{{ overview.expired ?? 0 }}</strong>
      </div>
    </div>

    <div class="card">
      <div class="section-head">
        <div>
          <p class="eyebrow">邀请解析</p>
          <h3>粘贴邀请内容并自动提取关键信息</h3>
        </div>
      </div>
      <textarea
        v-model="invitation"
        class="textarea"
        rows="6"
        placeholder="粘贴飞书、腾讯会议、Zoom 或邮件中的面试邀请内容"
      ></textarea>
      <div class="cta-row compact">
        <button class="button" @click="parseInvitation">解析邀请</button>
      </div>
    </div>

    <div class="grid two">
      <div class="card">
        <h3>安排列表</h3>
        <div class="list">
          <article v-for="schedule in schedules" :key="schedule.id" class="item">
            <div class="item-head">
              <strong>{{ schedule.company }} / {{ schedule.position }}</strong>
              <span class="tag">{{ formatScheduleStatus(schedule.status) }}</span>
            </div>
            <p class="muted">{{ schedule.source }} ｜ {{ formatDateTime(schedule.startTime) }}</p>
            <p>{{ schedule.meetingLink }}</p>
            <div class="cta-row compact">
              <button class="button secondary" @click="updateStatus(schedule.id, 'PENDING')">标记待面试</button>
              <button class="button secondary" @click="updateStatus(schedule.id, 'COMPLETED')">标记完成</button>
              <button class="button secondary" @click="updateStatus(schedule.id, 'CANCELED')">标记取消</button>
            </div>
          </article>
        </div>
      </div>

      <div class="card">
        <h3>按日期查看</h3>
        <div class="list">
          <article v-for="(items, day) in calendar" :key="day" class="item">
            <strong>{{ day }}</strong>
            <p class="muted">{{ items.length }} 场面试</p>
            <div class="list dense">
              <article v-for="item in items" :key="item.id" class="item">
                {{ item.company }} / {{ item.position }} / {{ formatDateTime(item.startTime) }}
              </article>
            </div>
          </article>
        </div>
      </div>
    </div>
  </section>
</template>

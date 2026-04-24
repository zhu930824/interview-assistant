<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import PageHeader from "../components/PageHeader.vue";
import { http } from "../api/http";
import type {
  ApiResponse,
  InterviewDirectionOption,
  InterviewSession
} from "../types";
import {
  formatDirection,
  formatInterviewStatus,
  formatStage
} from "../utils/display";

const sessions = ref<InterviewSession[]>([]);
const directions = ref<InterviewDirectionOption[]>([]);
const summary = ref<Record<string, unknown>>({});
const direction = ref("JAVA_BACKEND");
const totalMinutes = ref(45);
const followUpRounds = ref(1);
const answer = ref("");
const selectedId = ref<number | null>(null);

const selectedSession = computed(
  () => sessions.value.find((session) => session.id === selectedId.value) ?? sessions.value[0] ?? null
);

const currentQuestion = computed(() =>
  selectedSession.value?.askedQuestions.find(
    (question) => question.id === selectedSession.value?.currentQuestionId
  ) ?? null
);

const load = async () => {
  const [sessionResponse, directionResponse, centerResponse] = await Promise.all([
    http.get<ApiResponse<InterviewSession[]>>("/interviews"),
    http.get<ApiResponse<InterviewDirectionOption[]>>("/interviews/directions"),
    http.get<ApiResponse<{ summary: Record<string, unknown> }>>("/interviews/center")
  ]);
  sessions.value = sessionResponse.data.data;
  directions.value = directionResponse.data.data;
  summary.value = centerResponse.data.data.summary;
  if (!selectedId.value && sessions.value.length > 0) {
    selectedId.value = sessions.value[0].id;
  }
};

const createSession = async () => {
  const response = await http.post<ApiResponse<InterviewSession>>("/interviews", {
    direction: direction.value,
    totalMinutes: totalMinutes.value,
    followUpRounds: followUpRounds.value
  });
  selectedId.value = response.data.data.id;
  await load();
};

const submitAnswer = async () => {
  if (!selectedSession.value || !answer.value.trim()) return;
  const response = await http.post<ApiResponse<InterviewSession>>(
    `/interviews/${selectedSession.value.id}/answer`,
    { answer: answer.value }
  );
  answer.value = "";
  const updated = response.data.data;
  sessions.value = sessions.value.map((item) => (item.id === updated.id ? updated : item));
  selectedId.value = updated.id;
};

const evaluate = async () => {
  if (!selectedSession.value) return;
  const response = await http.post<ApiResponse<InterviewSession>>(
    `/interviews/${selectedSession.value.id}/evaluate`,
    { transcript: selectedSession.value.transcript }
  );
  const updated = response.data.data;
  sessions.value = sessions.value.map((item) => (item.id === updated.id ? updated : item));
};

const downloadReport = () => {
  if (!selectedSession.value) return;
  window.open(`/api/interviews/${selectedSession.value.id}/report`, "_blank");
};

onMounted(load);
</script>

<template>
  <section class="page">
    <PageHeader
      title="模拟面试"
      subtitle="按方向创建面试会话，完成分阶段问答，并统一生成结构化评估结果。"
    />

    <div class="grid three">
      <div class="stat">
        <span class="muted">总会话数</span>
        <strong>{{ summary.totalSessions ?? 0 }}</strong>
      </div>
      <div class="stat">
        <span class="muted">进行中</span>
        <strong>{{ summary.activeSessions ?? 0 }}</strong>
      </div>
      <div class="stat">
        <span class="muted">已完成</span>
        <strong>{{ summary.completedSessions ?? 0 }}</strong>
      </div>
    </div>

    <div class="card">
      <div class="section-head">
        <div>
          <p class="eyebrow">面试配置</p>
          <h3>创建新的模拟面试</h3>
        </div>
      </div>
      <div class="form-grid three">
        <select v-model="direction" class="select">
          <option v-for="item in directions" :key="item.code" :value="item.code">
            {{ formatDirection(item.code) }}
          </option>
        </select>
        <input v-model="totalMinutes" class="input" type="number" min="10" max="180" />
        <input v-model="followUpRounds" class="input" type="number" min="0" max="5" />
      </div>
      <div class="cta-row compact">
        <button class="button" @click="createSession">创建面试</button>
      </div>
    </div>

    <div class="grid two">
      <div class="card">
        <h3>会话列表</h3>
        <div class="list">
          <article
            v-for="session in sessions"
            :key="session.id"
            class="item clickable"
            :class="{ active: selectedId === session.id }"
            @click="selectedId = session.id"
          >
            <div class="item-head">
              <strong>{{ formatDirection(session.direction) }}</strong>
              <span class="tag">{{ formatInterviewStatus(session.status) }}</span>
            </div>
            <p class="muted">
              总时长 {{ session.totalMinutes }} 分钟 ｜ 追问 {{ session.followUpRounds }} 轮
            </p>
            <p>{{ session.evaluation ? "评估结果已生成" : "等待继续作答" }}</p>
          </article>
        </div>
      </div>

      <div class="card" v-if="selectedSession">
        <h3>面试工作区</h3>
        <p class="muted">
          当前题目：{{ currentQuestion?.content || "所有题目已完成，可以直接生成评估。" }}
        </p>

        <div class="stage-grid">
          <div
            v-for="(minutes, stage) in selectedSession.stageDurations"
            :key="stage"
            class="mini-stat"
          >
            <span>{{ formatStage(String(stage)) }}</span>
            <strong>{{ minutes }} 分钟</strong>
          </div>
        </div>

        <textarea
          v-model="answer"
          class="textarea"
          rows="6"
          placeholder="输入你的回答内容"
        ></textarea>

        <div class="cta-row compact">
          <button class="button" :disabled="!currentQuestion" @click="submitAnswer">提交回答</button>
          <button class="button secondary" @click="evaluate">生成评估</button>
          <button class="button secondary" @click="downloadReport">下载报告</button>
        </div>

        <h4>会话记录</h4>
        <pre class="console">{{ selectedSession.transcript || "暂无作答记录。" }}</pre>

        <h4>评估结果</h4>
        <pre class="console">{{ selectedSession.evaluation || "还没有生成评估结果。" }}</pre>
      </div>
    </div>
  </section>
</template>

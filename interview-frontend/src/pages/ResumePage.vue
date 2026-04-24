<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import PageHeader from "../components/PageHeader.vue";
import { http } from "../api/http";
import type { ApiResponse, ResumeRecord } from "../types";
import {
  formatDateTime,
  formatResumeStatus
} from "../utils/display";

const resumes = ref<ResumeRecord[]>([]);
const stats = ref<Record<string, number>>({});
const file = ref<File | null>(null);
const loading = ref(false);

const selectedResume = computed(() => resumes.value[0] ?? null);

const load = async () => {
  const [resumeResponse, statsResponse] = await Promise.all([
    http.get<ApiResponse<ResumeRecord[]>>("/resumes"),
    http.get<ApiResponse<Record<string, number>>>("/resumes/stats")
  ]);
  resumes.value = resumeResponse.data.data;
  stats.value = statsResponse.data.data;
};

const submit = async () => {
  if (!file.value) return;
  loading.value = true;
  try {
    const formData = new FormData();
    formData.append("file", file.value);
    await http.post("/resumes", formData);
    file.value = null;
    await load();
  } finally {
    loading.value = false;
  }
};

const retryResume = async (id: number) => {
  await http.post(`/resumes/${id}/retry`);
  await load();
};

const downloadReport = (id: number) => {
  window.open(`/api/resumes/${id}/report`, "_blank");
};

onMounted(load);
</script>

<template>
  <section class="page">
    <PageHeader
      title="简历管理"
      subtitle="上传候选人简历，实时查看异步分析进度，并输出结构化 PDF 分析报告。"
    />

    <div class="grid three">
      <div class="stat">
        <span class="muted">总简历数</span>
        <strong>{{ stats.total ?? 0 }}</strong>
      </div>
      <div class="stat">
        <span class="muted">已完成</span>
        <strong>{{ stats.completed ?? 0 }}</strong>
      </div>
      <div class="stat">
        <span class="muted">失败</span>
        <strong>{{ stats.failed ?? 0 }}</strong>
      </div>
    </div>

    <div class="card">
      <div class="section-head">
        <div>
          <p class="eyebrow">简历上传</p>
          <h3>简历上传与分析队列</h3>
        </div>
      </div>
      <div class="toolbar inline">
        <input
          class="input"
          type="file"
          @change="file = ($event.target as HTMLInputElement).files?.[0] ?? null"
        />
        <button class="button" :disabled="loading" @click="submit">
          {{ loading ? "上传中..." : "上传并开始分析" }}
        </button>
      </div>
    </div>

    <div class="grid two">
      <div class="card">
        <h3>分析队列</h3>
        <div class="list">
          <article v-for="resume in resumes" :key="resume.id" class="item">
            <div class="item-head">
              <strong>{{ resume.fileName }}</strong>
              <span class="tag">{{ formatResumeStatus(resume.status) }}</span>
            </div>
            <p class="muted">
              {{ resume.candidateName }} ｜ {{ resume.targetPosition }} ｜ 进度 {{ resume.progress }}%
            </p>
            <p>{{ resume.analysisSummary }}</p>
            <div class="pill-row">
              <span v-for="keyword in resume.keywords" :key="keyword" class="pill">{{ keyword }}</span>
            </div>
            <div class="cta-row compact">
              <button class="button secondary" @click="downloadReport(resume.id)">下载报告</button>
              <button
                class="button secondary"
                :disabled="resume.status !== 'FAILED'"
                @click="retryResume(resume.id)"
              >
                重新分析
              </button>
            </div>
          </article>
        </div>
      </div>

      <div class="card" v-if="selectedResume">
        <h3>最新分析结果</h3>
        <p class="muted">{{ formatDateTime(selectedResume.updatedAt) }}</p>
        <p>{{ selectedResume.rawTextPreview || "还没有可展示的解析预览。" }}</p>
        <h4>优势亮点</h4>
        <div class="list dense">
          <article v-for="item in selectedResume.strengths" :key="item" class="item">{{ item }}</article>
        </div>
        <h4>风险提示</h4>
        <div class="list dense">
          <article v-for="item in selectedResume.risks" :key="item" class="item">{{ item }}</article>
        </div>
      </div>
    </div>
  </section>
</template>

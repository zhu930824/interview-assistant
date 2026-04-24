<script setup lang="ts">
import { onMounted, ref } from "vue";
import PageHeader from "../components/PageHeader.vue";
import { http } from "../api/http";
import type { ApiResponse, KnowledgeDocument } from "../types";
import { translateKnowledgeLine } from "../utils/display";

const docs = ref<KnowledgeDocument[]>([]);
const stats = ref<Record<string, number>>({});
const question = ref("");
const streamOutput = ref<string[]>([]);
const file = ref<File | null>(null);

const load = async () => {
  const [docResponse, statsResponse] = await Promise.all([
    http.get<ApiResponse<KnowledgeDocument[]>>("/knowledge-bases"),
    http.get<ApiResponse<Record<string, number>>>("/knowledge-bases/stats")
  ]);
  docs.value = docResponse.data.data;
  stats.value = statsResponse.data.data;
};

const upload = async () => {
  if (!file.value) return;
  const formData = new FormData();
  formData.append("file", file.value);
  await http.post("/knowledge-bases", formData);
  file.value = null;
  await load();
};

const ask = () => {
  streamOutput.value = [];
  const source = new EventSource(
    `/api/knowledge-bases/chat?question=${encodeURIComponent(question.value)}`
  );
  source.onmessage = (event) => {
    streamOutput.value.push(translateKnowledgeLine(event.data));
  };
  source.onerror = () => source.close();
};

const download = (id: number) => {
  window.open(`/api/knowledge-bases/${id}/download`, "_blank");
};

onMounted(load);
</script>

<template>
  <section class="page">
    <PageHeader
      title="知识库"
      subtitle="上传资料、查看向量化统计，并通过流式问答快速检索面试知识点。"
    />

    <div class="grid three">
      <div class="stat">
        <span class="muted">文档数</span>
        <strong>{{ stats.totalDocuments ?? 0 }}</strong>
      </div>
      <div class="stat">
        <span class="muted">分块数</span>
        <strong>{{ stats.totalChunks ?? 0 }}</strong>
      </div>
      <div class="stat">
        <span class="muted">估算 Token</span>
        <strong>{{ stats.totalTokens ?? 0 }}</strong>
      </div>
    </div>

    <div class="grid two">
      <div class="card">
        <h3>上传资料</h3>
        <input
          class="input"
          type="file"
          @change="file = ($event.target as HTMLInputElement).files?.[0] ?? null"
        />
        <div class="cta-row compact">
          <button class="button" @click="upload">上传</button>
        </div>
      </div>

      <div class="card">
        <h3>发起提问</h3>
        <textarea
          v-model="question"
          class="textarea"
          rows="4"
          placeholder="输入一个与知识库相关的问题"
        ></textarea>
        <div class="cta-row compact">
          <button class="button" @click="ask">开始流式问答</button>
        </div>
      </div>
    </div>

    <div class="grid two">
      <div class="card">
        <h3>资料列表</h3>
        <div class="list">
          <article v-for="doc in docs" :key="doc.id" class="item">
            <div class="item-head">
              <strong>{{ doc.fileName }}</strong>
              <span class="tag">{{ doc.chunks }} 个分块</span>
            </div>
            <p>{{ doc.summary }}</p>
            <p class="muted">估算 Token：{{ doc.tokenEstimate }}</p>
            <div class="pill-row">
              <span v-for="keyword in doc.keywords" :key="keyword" class="pill">{{ keyword }}</span>
            </div>
            <div class="cta-row compact">
              <button class="button secondary" @click="download(doc.id)">下载原文档</button>
            </div>
          </article>
        </div>
      </div>

      <div class="card">
        <h3>流式输出</h3>
        <div class="list">
          <article v-for="(line, index) in streamOutput" :key="index" class="item">
            {{ line }}
          </article>
        </div>
      </div>
    </div>
  </section>
</template>

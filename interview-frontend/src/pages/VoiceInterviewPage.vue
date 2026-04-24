<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import PageHeader from "../components/PageHeader.vue";
import { http } from "../api/http";
import type { ApiResponse, VoiceInterviewSession } from "../types";
import { translateVoiceEvent } from "../utils/display";

const sessions = ref<VoiceInterviewSession[]>([]);
const sessionId = ref("");
const transcript = ref("");
const events = ref<string[]>([]);
let socket: WebSocket | null = null;

const currentSession = computed(
  () => sessions.value.find((item) => item.sessionId === sessionId.value) ?? null
);

const load = async () => {
  const response = await http.get<ApiResponse<VoiceInterviewSession[]>>("/voice-interviews");
  sessions.value = response.data.data;
  if (!sessionId.value && sessions.value.length > 0) {
    sessionId.value = sessions.value[0].sessionId;
  }
};

const refreshDetail = async () => {
  if (!sessionId.value) return;
  const response = await http.get<ApiResponse<VoiceInterviewSession>>(
    `/voice-interviews/${sessionId.value}`
  );
  const updated = response.data.data;
  sessions.value = [
    updated,
    ...sessions.value.filter((item) => item.sessionId !== updated.sessionId)
  ];
};

const createSession = async () => {
  const response = await http.post<ApiResponse<VoiceInterviewSession>>("/voice-interviews");
  sessionId.value = response.data.data.sessionId;
  events.value.unshift(`会话已创建：${sessionId.value}`);
  await load();
};

const connect = () => {
  if (!sessionId.value) return;
  socket?.close();
  const protocol = location.protocol === "https:" ? "wss" : "ws";
  socket = new WebSocket(
    `${protocol}://${location.host}/ws/voice-interview?sessionId=${sessionId.value}`
  );
  socket.onmessage = (event) => {
    events.value.unshift(translateVoiceEvent(event.data));
    void refreshDetail();
  };
};

const sendTranscript = async () => {
  if (!sessionId.value || !transcript.value.trim()) return;
  await http.post(`/voice-interviews/${sessionId.value}/submit`, { transcript: transcript.value });
  socket?.send(transcript.value);
  transcript.value = "";
  await refreshDetail();
};

const pause = async () => {
  if (!sessionId.value) return;
  await http.post(`/voice-interviews/${sessionId.value}/pause`);
  events.value.unshift("会话已暂停");
  await refreshDetail();
};

const resume = async () => {
  if (!sessionId.value) return;
  await http.post(`/voice-interviews/${sessionId.value}/resume`);
  events.value.unshift("会话已恢复");
  await refreshDetail();
};

const downloadReport = () => {
  if (!sessionId.value) return;
  window.open(`/api/voice-interviews/${sessionId.value}/report`, "_blank");
};

onMounted(load);
</script>

<template>
  <section class="page">
    <PageHeader
      title="语音面试"
      subtitle="创建语音会话，连接 WebSocket 实时字幕，并模拟 AI 追问与多轮对话。"
    />

    <div class="card">
      <div class="cta-row">
        <button class="button" @click="createSession">创建会话</button>
        <button class="button secondary" @click="connect">连接 WebSocket</button>
        <button class="button secondary" @click="pause">暂停</button>
        <button class="button secondary" @click="resume">恢复</button>
        <button class="button secondary" @click="downloadReport">下载报告</button>
      </div>
      <p class="muted">当前会话：{{ sessionId || "尚未选择" }}</p>
    </div>

    <div class="grid two">
      <div class="card">
        <h3>手动提交转写</h3>
        <textarea
          v-model="transcript"
          class="textarea"
          rows="5"
          placeholder="输入识别后的语音文本或模拟字幕"
        ></textarea>
        <div class="cta-row compact">
          <button class="button" @click="sendTranscript">提交文本</button>
        </div>

        <div v-if="currentSession" class="detail-stack">
          <p><strong>暂停状态：</strong> {{ currentSession.paused ? "已暂停" : "进行中" }}</p>
          <p><strong>当前题目：</strong> {{ currentSession.currentQuestion }}</p>
          <p><strong>已提交轮次：</strong> {{ currentSession.submittedTurns }}</p>
        </div>
      </div>

      <div class="card">
        <h3>实时事件流</h3>
        <div class="list">
          <article v-for="(item, index) in events" :key="index" class="item">
            {{ item }}
          </article>
        </div>
      </div>
    </div>

    <div class="grid two" v-if="currentSession">
      <div class="card">
        <h3>字幕记录</h3>
        <div class="list">
          <article
            v-for="(item, index) in currentSession.subtitles"
            :key="`${item}-${index}`"
            class="item"
          >
            {{ item }}
          </article>
        </div>
      </div>

      <div class="card">
        <h3>AI 追问</h3>
        <div class="list">
          <article
            v-for="(item, index) in currentSession.aiReplies"
            :key="`${item}-${index}`"
            class="item"
          >
            {{ item }}
          </article>
        </div>
      </div>
    </div>
  </section>
</template>

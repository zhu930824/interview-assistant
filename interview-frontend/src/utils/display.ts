const directionMap: Record<string, string> = {
  JAVA_BACKEND: "Java 后端",
  FRONTEND: "前端工程",
  PYTHON: "Python 开发",
  ALGORITHM: "算法工程",
  SYSTEM_DESIGN: "系统设计",
  TESTING: "测试开发",
  AI_AGENT: "AI 智能体",
  ALI_SPECIAL: "阿里专项",
  BYTE_SPECIAL: "字节专项",
  TENCENT_SPECIAL: "腾讯专项"
};

const resumeStatusMap: Record<string, string> = {
  UPLOADED: "已上传",
  PARSING: "解析中",
  PARSED: "待分析",
  ANALYZING: "分析中",
  COMPLETED: "已完成",
  PARSE_FAILED: "解析失败",
  ANALYSIS_FAILED: "分析失败",
  PENDING: "待分析",
  FAILED: "失败"
};

const sessionStatusMap: Record<string, string> = {
  IN_PROGRESS: "进行中",
  READY_FOR_EVALUATION: "待评估",
  COMPLETED: "已完成"
};

const scheduleStatusMap: Record<string, string> = {
  PENDING: "待面试",
  COMPLETED: "已完成",
  CANCELED: "已取消",
  EXPIRED: "已过期"
};

const stageMap: Record<string, string> = {
  INTRODUCTION: "自我介绍",
  TECHNICAL: "技术考察",
  PROJECT: "项目深挖",
  QA: "反问环节"
};

export function formatDirection(value: string): string {
  return directionMap[value] ?? value.replace(/_/g, " ");
}

export function formatResumeStatus(value: string): string {
  return resumeStatusMap[value] ?? value;
}

export function formatInterviewStatus(value: string): string {
  return sessionStatusMap[value] ?? value;
}

export function formatScheduleStatus(value: string): string {
  return scheduleStatusMap[value] ?? value;
}

export function formatStage(value: string): string {
  return stageMap[value] ?? value;
}

export function formatDateTime(value: string): string {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value.replace("T", " ");
  }
  return new Intl.DateTimeFormat("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  }).format(date);
}

export function translateKnowledgeLine(line: string): string {
  if (line.startsWith("Searching knowledge base for:")) {
    return `正在检索知识库：${line.replace("Searching knowledge base for:", "").trim()}`;
  }
  if (line.startsWith("Matched documents:")) {
    return `命中文档：${line.replace("Matched documents:", "").trim()}`;
  }
  if (line.startsWith("Using ")) {
    return `引用片段：${line.replace("Using ", "").trim()}`;
  }
  if (line.startsWith("Answer:")) {
    return `回答：${line.replace("Answer:", "").trim()}`;
  }
  if (line.startsWith("No direct hit found.")) {
    return "暂未命中直接相关内容，建议补充文档或换一种提问方式。";
  }
  return line;
}

export function translateVoiceEvent(raw: string): string {
  try {
    const parsed = JSON.parse(raw) as Record<string, string | boolean>;
    if (parsed.type === "connected") {
      return "语音面试 WebSocket 已连接";
    }
    if (parsed.type === "subtitle") {
      const content = typeof parsed.content === "string" ? parsed.content : "";
      const reply = typeof parsed.reply === "string" ? parsed.reply : "";
      return `字幕：${content}${reply ? ` ｜ AI 追问：${reply}` : ""}`;
    }
  } catch {
    if (raw === "Session paused") return "会话已暂停";
    if (raw === "Session resumed") return "会话已恢复";
    if (raw.startsWith("Session created:")) {
      return `会话已创建：${raw.replace("Session created:", "").trim()}`;
    }
  }
  return raw;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export * from './auth';

export interface ResumeRecord {
  id: number;
  fileName: string;
  filePath: string;
  contentHash: string;
  status: string;
  retryCount: number;
  progress: number;
  duplicated: boolean;
  candidateName: string;
  targetPosition: string;
  analysisSummary: string;
  keywords: string[];
  strengths: string[];
  risks: string[];
  rawTextPreview: string;
  createdAt: string;
  updatedAt: string;
}

export interface InterviewQuestion {
  id: string;
  stage: string;
  content: string;
}

export interface InterviewSession {
  id: number;
  direction: string;
  totalMinutes: number;
  stageDurations: Record<string, number>;
  followUpRounds: number;
  askedQuestions: InterviewQuestion[];
  currentQuestionId: string | null;
  transcript: string;
  evaluation: string | null;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface InterviewDirectionOption {
  code: string;
  label: string;
  preview: string;
}

export interface InterviewSchedule {
  id: number;
  company: string;
  position: string;
  startTime: string;
  meetingLink: string;
  status: string;
  source: string;
}

export interface KnowledgeDocument {
  id: number;
  fileName: string;
  summary: string;
  chunks: number;
  tokenEstimate: number;
  keywords: string[];
  createdAt: string;
}

export interface VoiceInterviewSession {
  sessionId: string;
  paused: boolean;
  latestTranscript: string;
  currentQuestion: string;
  subtitles: string[];
  aiReplies: string[];
  submittedTurns: number;
  createdAt: string;
  updatedAt: string;
}

// ========== 简历多维度评分系统 ==========

/** 多维度评分详情 */
export interface ScoreDetail {
  /** 项目经验评分 (0-40) */
  projectScore: number;
  /** 技能匹配度评分 (0-20) */
  skillMatchScore: number;
  /** 内容完整性评分 (0-15) */
  contentScore: number;
  /** 结构清晰度评分 (0-15) */
  structureScore: number;
  /** 表达专业性评分 (0-10) */
  expressionScore: number;
}

/** 建议类别 */
export type SuggestionCategory = '项目' | '技能' | '内容' | '格式' | '结构' | '表达';

/** 建议优先级 */
export type SuggestionPriority = '高' | '中' | '低';

/** 改进建议 */
export interface ResumeSuggestion {
  /** 建议类别 */
  category: SuggestionCategory;
  /** 优先级 */
  priority: SuggestionPriority;
  /** 问题描述 */
  issue: string;
  /** 具体建议 */
  recommendation: string;
}

/** 简历分析详情 */
export interface ResumeAnalysis {
  /** 分析记录ID */
  id: number;
  /** 总分 (0-100) */
  overallScore: number;
  /** 各维度评分 */
  scoreDetail: ScoreDetail;
  /** 一句话总结 */
  summary: string;
  /** 优势亮点 */
  strengths: string[];
  /** 改进建议 */
  suggestions: ResumeSuggestion[];
  /** 分析时间 */
  analyzedAt: string;
}

/** 简历分析状态 */
export type AnalyzeStatus =
  | 'UPLOADED'
  | 'PARSING'
  | 'PARSED'
  | 'ANALYZING'
  | 'COMPLETED'
  | 'PARSE_FAILED'
  | 'ANALYSIS_FAILED'
  | 'PENDING'
  | 'FAILED'

/** 简历详情（扩展版） */
export interface ResumeDetail extends ResumeRecord {
  /** 分析状态 */
  analyzeStatus?: AnalyzeStatus;
  /** 分析错误信息 */
  analyzeError?: string;
  /** 分析结果列表（可能有多次分析记录） */
  analyses?: ResumeAnalysis[];
  /** 关联的面试记录ID列表 */
  interviewIds?: number[];
}

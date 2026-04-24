export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

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

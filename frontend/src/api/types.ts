/**
 * 接口层类型出口：全部来自成员 A 交付的契约 `src/api/contracts.d.ts`，
 * 本文件不定义任何字段，避免契约出现两份而漂移（评审建议项 3）。
 */
export type {
  AnswerView,
  Author,
  CommentView,
  LeaderboardEntry,
  Me,
  Notification,
  Page,
  ProblemDetail,
  QuestionDetail,
  QuestionSummary,
  ReputationProfile,
  Role,
  SearchResult,
} from '@/api/contracts';

/** 业务代码沿用的命名，等价于契约里的 CreateQuestionRequest */
export type { CreateQuestionRequest as CreateQuestionDTO } from '@/api/contracts';

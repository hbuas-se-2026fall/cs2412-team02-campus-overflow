/**
 * 接口契约统一出口（成员 C）
 *
 * 契约的唯一定义来源是 A 交付的 `src/types.ts`，本文件只做转发，不重复定义字段，
 * 避免前后端契约出现两份而互相漂移。
 *
 * 若 A 后续改为交付 `src/api/contracts.d.ts`，删除本文件并把各处的
 * `from './contracts'` 改成 `from './contracts'`（同名即可，无需改调用方）。
 */
export type {
  Answer,
  Author,
  Comment,
  LeaderboardEntry,
  Me,
  Notification,
  Page,
  QuestionDetail,
  QuestionSummary,
  ReputationProfile,
  Role,
  SearchResult,
} from '../types';

/** 发布提问的请求体，与后端 POST /api/v1/questions 契约一致 */
export interface CreateQuestionDTO {
  courseId?: number;
  title: string;
  body: string;
  tags: string[];
}

/** 后端统一以 RFC 9457 Problem Details 返回错误 */
export interface ProblemDetail {
  code?: string;
  detail?: string;
  title?: string;
  status?: number;
}

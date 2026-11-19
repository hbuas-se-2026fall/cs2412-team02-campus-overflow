/**
 * 接口层类型：全部从 contracts 转发，本文件不再定义任何字段。
 * 页面与 hooks 统一 `from './types'` 或 `from './contracts'` 拿到的是同一套类型。
 */
export type { CreateQuestionDTO, ProblemDetail } from './contracts';
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
} from './contracts';

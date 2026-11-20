/**
 * CampusOverflow 前端 API 契约（Lab5 · 成员A 产出）
 * ------------------------------------------------------------------
 * 这是**类型契约**，不是实现：成员 C 的 axios 封装、成员 D 的页面、成员 E 的验收
 * 都以本文件为准，避免各自猜字段名。
 *
 * 约定：
 * 1. 所有接口前缀 `/api`，由 Vite 的 server.proxy 转发到后端 8080（同源，Cookie 与
 *    CSRF 双提交才能正常工作）。
 * 2. 错误统一是 RFC 9457 Problem Details，额外带业务码 `code`（如 QA-1001）。
 * 3. 时间字段（createdAt / updatedAt / awardedAt）都是 ISO-8601 字符串（UTC），
 *    展示时由界面层转本地时区。
 * 4. 分页响应形如 `{ items, total, page, size }`。
 */

/* ============================ 基础类型 ============================ */

export type Role = 'STUDENT' | 'TA' | 'TEACHER' | 'ADMIN';

export type TargetType = 'QUESTION' | 'ANSWER';

/** 统一分页响应。 */
export interface Page<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
}

/** RFC 9457 Problem Details + 业务错误码。 */
export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
  /** 业务错误码，如 QA-1001、AUTH-403-CSRF */
  code?: string;
}

/* ============================ 认证与会话 ============================ */

export interface CsrfResponse {
  token: string;
  headerName: 'X-XSRF-TOKEN';
}

export interface RegisterRequest {
  /** 4–20 个字符，全局唯一 */
  username: string;
  /** 2–20 个字符 */
  displayName: string;
  email: string;
  /** 8–64 个字符 */
  password: string;
  college?: string;
  className?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface CourseMembership {
  id: number;
  code: string;
  name: string;
  term: string;
  teacherName: string;
}

/** 当前登录用户（GET /v1/auth/me、POST /v1/auth/login 的响应体）。 */
export interface Me {
  id: number;
  username: string;
  displayName: string;
  email: string;
  role: Role;
  verified: boolean;
  college?: string;
  className?: string;
  courses: CourseMembership[];
}

/* ============================ 通用视图 ============================ */

/** 内容作者的精简信息，所有列表 / 详情里的 author、endorsedBy 都是这个形状。 */
export interface Author {
  id: number;
  displayName: string;
  role: Role | string;
  verified: boolean;
}

/* ============================ 问答 ============================ */

/** 列表项（GET /v1/questions、GET /v1/search/questions）。 */
export interface QuestionSummary {
  id: number;
  title: string;
  /** 纯文本摘要，已截断（不是 Markdown） */
  excerpt: string;
  tags: string[];
  courseId?: number;
  author: Author;
  score: number;
  answerCount: number;
  viewCount: number;
  accepted: boolean;
  createdAt: string;
}

export interface CourseRef {
  id: number;
  code: string;
  name: string;
}

export interface PermissionsView {
  canEdit: boolean;
  canDelete: boolean;
  canAccept: boolean;
  canManage: boolean;
}

export interface CommentView {
  id: number;
  author: Author;
  parentId?: number;
  body: string;
  createdAt: string;
}

export interface AnswerView {
  id: number;
  /** Markdown 原文 */
  body: string;
  /** 服务端渲染并净化后的 HTML */
  bodyHtml: string;
  author: Author;
  score: number;
  accepted: boolean;
  endorsedBy?: Author;
  /** 当前用户在这条回答上的投票：-1 / 0 / 1 */
  myVote: number;
  canEdit: boolean;
  createdAt: string;
  updatedAt: string;
  comments: CommentView[];
}

/** 详情（GET /v1/questions/{id}）。 */
export interface QuestionDetail {
  id: number;
  title: string;
  body: string;
  bodyHtml: string;
  tags: string[];
  course?: CourseRef;
  author: Author;
  score: number;
  answerCount: number;
  viewCount: number;
  acceptedAnswerId?: number;
  myVote: number;
  createdAt: string;
  updatedAt: string;
  permissions: PermissionsView;
  comments: CommentView[];
  answers: AnswerView[];
}

/** 发布问题（POST /v1/questions）的请求体。 */
export interface CreateQuestionRequest {
  courseId?: number;
  /** 10–150 个字符 */
  title: string;
  /** 10–30000 个字符的 Markdown */
  body: string;
  /** 1–5 个标签 */
  tags: string[];
}

/** 编辑问题（PUT /v1/questions/{id}）与发布问题同构。 */
export type UpdateQuestionRequest = CreateQuestionRequest;

export interface CreateAnswerRequest {
  body: string;
}

/** 创建类接口统一只返回新建资源的 id。 */
export interface CreatedResponse {
  id: number;
}

export interface VoteRequest {
  targetType: TargetType;
  targetId: number;
  /** -1 反对 / 0 撤销 / 1 赞成 */
  value: -1 | 0 | 1;
}

export interface VoteResult {
  myVote: number;
  /** 本次投票对目标分数的增量（改投时为「新值 − 旧值」） */
  scoreDelta: number;
}

export interface CreateCommentRequest {
  targetType: TargetType;
  targetId: number;
  body: string;
  /** 二级回复时传父评论 id，最多一层 */
  parentId?: number;
}

/** 相关推荐（GET /v1/questions/{id}/related）。 */
export interface RelatedQuestion {
  id: number;
  title: string;
  answerCount: number;
  accepted: boolean;
}

/* ============================ 检索与标签 ============================ */

export interface SearchResult extends QuestionSummary {
  /** 该问题是否还有未结算的悬赏 */
  bountyOpen: boolean;
}

export interface SearchParams {
  q: string;
  tag?: string;
  sort?: 'newest' | 'votes';
  unanswered?: boolean;
  page?: number;
}

export interface TagCount {
  name: string;
  questionCount: number;
}

/* ============================ 声誉与激励 ============================ */

export interface LeaderboardEntry {
  rank: number;
  userId: number;
  displayName: string;
  verified: boolean;
  score: number;
}

export interface Badge {
  code: string;
  name: string;
  description: string;
  awardedAt: string;
}

export interface ReputationProfile {
  userId: number;
  displayName: string;
  reputation: number;
  availablePoints?: number;
  frozenPoints?: number;
  badges: Badge[];
}

export interface CreateBountyRequest {
  points: number;
  days: number;
}

/* ============================ 通知 ============================ */

export interface Notification {
  id: number;
  type: string;
  title: string;
  link: string;
  read: boolean;
  createdAt: string;
}

export interface UnreadCount {
  count: number;
}

/* ====================== 端点索引（成员C/E 对照用） ====================== */

/**
 * 每个端点的「方法 + 路径 + 入参 + 出参」，成员 C 按这张表实现请求函数，
 * 成员 E 按这张表写验收用例。
 */
export interface ApiContract {
  'GET /v1/auth/csrf': { request: void; response: CsrfResponse };
  'POST /v1/auth/register': { request: RegisterRequest; response: CreatedResponse };
  'POST /v1/auth/login': { request: LoginRequest; response: Me };
  'POST /v1/auth/logout': { request: void; response: void };
  'GET /v1/auth/me': { request: void; response: Me };

  'GET /v1/questions': {
    request: { page?: number; size?: number; courseId?: number };
    response: Page<QuestionSummary>;
  };
  'POST /v1/questions': { request: CreateQuestionRequest; response: CreatedResponse };
  'GET /v1/questions/{id}': { request: void; response: QuestionDetail };
  'PUT /v1/questions/{id}': { request: UpdateQuestionRequest; response: void };
  'DELETE /v1/questions/{id}': { request: void; response: void };
  'GET /v1/questions/{id}/related': { request: void; response: RelatedQuestion[] };

  'POST /v1/questions/{id}/answers': { request: CreateAnswerRequest; response: CreatedResponse };
  'PUT /v1/questions/{id}/accepted-answer': { request: { answerId: number }; response: void };
  'POST /v1/answers/{id}/endorsement': { request: void; response: void };

  'PUT /v1/votes': { request: VoteRequest; response: VoteResult };
  'POST /v1/comments': { request: CreateCommentRequest; response: CreatedResponse };

  'GET /v1/search/questions': { request: SearchParams; response: Page<SearchResult> };
  'GET /v1/tags/popular': { request: { limit?: number }; response: TagCount[] };

  'GET /v1/leaderboard': { request: { period?: string }; response: LeaderboardEntry[] };
  'GET /v1/users/{id}/reputation': { request: void; response: ReputationProfile };
  'POST /v1/questions/{id}/bounty': { request: CreateBountyRequest; response: CreatedResponse };

  'GET /v1/notifications': { request: { page?: number }; response: Page<Notification> };
  'GET /v1/notifications/unread-count': { request: void; response: UnreadCount };
  'POST /v1/notifications/read-all': { request: void; response: { updated: number } };
}

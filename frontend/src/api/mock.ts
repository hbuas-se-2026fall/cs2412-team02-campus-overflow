/**
 * mock 模拟接口：返回结构严格对齐后端 Lab4 契约（PascalCase 除外，字段名一致）。
 * D 页面开发时优先走这里，不需要等后端联通。
 *
 * 切换方式见 questionApi.ts 里的 USE_MOCK 开关：
 *   USE_MOCK = true  → 用本文件 mock 数据
 *   USE_MOCK = false → 真实请求后端 /api/v1
 */
import type { CreateQuestionDTO, Page, QuestionDetail, QuestionSummary } from './types';

const mockAuthor = { id: 1, displayName: '测试用户', role: 'STUDENT', verified: false };

/* ---------------- mock 数据 ---------------- */

export const mockQuestions: QuestionSummary[] = [
  {
    id: 1,
    title: 'SpringBoot 领域分层怎么写',
    excerpt: 'domain 和 infrastructure 的职责应该怎么划分？',
    tags: ['java', 'springboot'],
    courseId: 1,
    author: mockAuthor,
    score: 0,
    answerCount: 2,
    viewCount: 12,
    accepted: false,
    createdAt: '2026-11-19T09:30:00Z',
  },
  {
    id: 2,
    title: 'React Query 的缓存怎么失效',
    excerpt: '发完帖子列表不刷新，是要调用 invalidateQueries 吗？',
    tags: ['react', 'frontend'],
    author: mockAuthor,
    score: 3,
    answerCount: 1,
    viewCount: 8,
    accepted: true,
    createdAt: '2026-11-19T10:10:00Z',
  },
];

/* ---------------- mock 接口实现（签名与真实接口一致） ---------------- */

/** GET /api/v1/questions → Page<QuestionSummary> */
export function mockFetchPage(page: number, size: number): Page<QuestionSummary> {
  const start = (page - 1) * size;
  return {
    items: mockQuestions.slice(start, start + size),
    total: mockQuestions.length,
    page,
    size,
  };
}

/** GET /api/v1/questions/{id} → QuestionDetail */
export function mockFetchDetail(id: number): QuestionDetail {
  const item = mockQuestions.find((q) => q.id === id);
  if (!item) throw new Error('问题不存在');
  return {
    id: item.id,
    title: item.title,
    body: item.excerpt,
    bodyHtml: `<p>${item.excerpt}</p>`,
    tags: item.tags,
    course: item.courseId ? { id: item.courseId, code: 'CS2412', name: '软件工程' } : undefined,
    author: item.author,
    score: item.score,
    answerCount: item.answerCount,
    viewCount: item.viewCount,
    acceptedAnswerId: undefined,
    myVote: 0,
    createdAt: item.createdAt,
    permissions: { canEdit: true, canDelete: true, canAccept: true, canManage: true },
    comments: [],
    answers: [],
  };
}

/** POST /api/v1/questions → { id } */
export function mockCreate(dto: CreateQuestionDTO): { id: number } {
  const newItem: QuestionSummary = {
    id: Date.now(),
    title: dto.title,
    excerpt: dto.body.slice(0, 80),
    tags: dto.tags,
    courseId: dto.courseId,
    author: mockAuthor,
    score: 0,
    answerCount: 0,
    viewCount: 0,
    accepted: false,
    createdAt: new Date().toISOString(),
  };
  mockQuestions.unshift(newItem);
  return { id: newItem.id };
}

/** DELETE /api/v1/questions/{id} → 204 */
export function mockDelete(id: number): void {
  const idx = mockQuestions.findIndex((q) => q.id === id);
  if (idx >= 0) mockQuestions.splice(idx, 1);
}

import type {
  CreateQuestionRequest,
  CreatedResponse,
  Page,
  QuestionDetail,
  QuestionSummary,
  TagCount,
} from '@/api/contracts';
import { api } from '@/api/axios';
import { createSeedQuestions, findCourse } from './mockQuestions';

/** 默认走 mock；在 .env.local 写 VITE_USE_MOCK=false 即切真实后端 */
const USE_MOCK = (import.meta.env.VITE_USE_MOCK ?? 'true') !== 'false';

const STORAGE_KEY = 'campusoverflow.lab5.questions';
const CURRENT_AUTHOR = { id: 1, displayName: '我（演示账号）', role: 'STUDENT', verified: false };

export interface QuestionListQuery {
  page: number;
  pageSize: number;
  keyword?: string;
  tag?: string;
}

interface SearchHitResponse {
  questionId: number;
  title: string;
  snippet: string;
  tags: string[];
  courseId: number | null;
  author: QuestionSummary['author'];
  score: number;
  answerCount: number;
  accepted: boolean;
  bountyOpen: boolean;
  createdAt: string;
}

let cache: QuestionDetail[] | null = null;

function save(items: QuestionDetail[]): void {
  cache = items;
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
}

function load(): QuestionDetail[] {
  if (cache !== null) return cache;
  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (raw !== null) {
    try {
      const parsed = JSON.parse(raw) as QuestionDetail[];
      if (Array.isArray(parsed) && parsed.length > 0) {
        cache = parsed;
        return cache;
      }
    } catch {
      window.localStorage.removeItem(STORAGE_KEY);
    }
  }
  const seeds = createSeedQuestions();
  save(seeds);
  return seeds;
}

function delay(ms: number): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}

function summaryOf(question: QuestionDetail): QuestionSummary {
  return {
    id: question.id,
    title: question.title,
    excerpt: question.body.replace(/\s+/g, ' ').slice(0, 120),
    tags: question.tags,
    courseId: question.course?.id,
    author: question.author,
    score: question.score,
    answerCount: question.answers.length,
    viewCount: question.viewCount,
    accepted: question.answers.some((answer) => answer.accepted),
    createdAt: question.createdAt,
  };
}

function matches(question: QuestionDetail, keyword: string, tag: string | undefined): boolean {
  if (tag !== undefined && !question.tags.includes(tag)) return false;
  if (keyword === '') return true;
  const haystack = `${question.title} ${question.body} ${question.tags.join(' ')}`.toLowerCase();
  return haystack.includes(keyword);
}

function toSummary(hit: SearchHitResponse): QuestionSummary {
  return {
    id: hit.questionId,
    title: hit.title,
    excerpt: hit.snippet,
    tags: hit.tags,
    courseId: hit.courseId ?? undefined,
    author: hit.author,
    score: hit.score,
    answerCount: hit.answerCount,
    viewCount: 0,
    accepted: hit.accepted,
    createdAt: hit.createdAt,
  };
}

export async function fetchQuestionList(query: QuestionListQuery): Promise<Page<QuestionSummary>> {
  if (USE_MOCK) {
    await delay(220);
    const keyword = (query.keyword ?? '').trim().toLowerCase();
    const filtered = load().filter((question) => matches(question, keyword, query.tag));
    const start = (query.page - 1) * query.pageSize;
    return {
      items: filtered.slice(start, start + query.pageSize).map(summaryOf),
      total: filtered.length,
      page: query.page,
      size: query.pageSize,
    };
  }
  const params = { page: query.page, size: query.pageSize };
  if ((query.keyword ?? '') !== '' || query.tag !== undefined) {
    const res = await api.get<Page<SearchHitResponse>>('/v1/search/questions', {
      params: { ...params, q: query.keyword, tag: query.tag },
    });
    return { ...res.data, items: res.data.items.map(toSummary) };
  }
  const res = await api.get<Page<QuestionSummary>>('/v1/questions', { params });
  return res.data;
}

export async function fetchQuestionDetail(id: number): Promise<QuestionDetail> {
  if (USE_MOCK) {
    await delay(180);
    const found = load().find((question) => question.id === id);
    if (found === undefined) throw new Error(`问题 #${id} 不存在或已被删除`);
    return found;
  }
  const res = await api.get<QuestionDetail>(`/v1/questions/${id}`);
  return res.data;
}

export async function fetchPopularTags(): Promise<TagCount[]> {
  if (USE_MOCK) {
    await delay(140);
    const counter = new Map<string, number>();
    for (const question of load()) {
      for (const tag of question.tags) {
        counter.set(tag, (counter.get(tag) ?? 0) + 1);
      }
    }
    return [...counter.entries()]
        .map(([name, questionCount]) => ({ name, questionCount }))
        .sort((left, right) => right.questionCount - left.questionCount)
        .slice(0, 10);
  }
  const res = await api.get<TagCount[]>('/v1/tags/popular', { params: { limit: 10 } });
  return res.data;
}

export async function createQuestion(input: CreateQuestionRequest): Promise<CreatedResponse> {
  if (USE_MOCK) {
    await delay(320);
    const items = load();
    const nextId = items.reduce((max, question) => Math.max(max, question.id), 0) + 1;
    const now = new Date().toISOString();
    const created: QuestionDetail = {
      id: nextId,
      title: input.title,
      body: input.body,
      bodyHtml: '',
      tags: input.tags,
      course: findCourse(input.courseId),
      author: CURRENT_AUTHOR,
      score: 0,
      answerCount: 0,
      viewCount: 0,
      acceptedAnswerId: undefined,
      myVote: 0,
      createdAt: now,
      updatedAt: now,
      permissions: { canEdit: true, canDelete: true, canAccept: true, canManage: false },
      comments: [],
      answers: [],
    };
    save([created, ...items]);
    return { id: created.id };
  }
  const res = await api.post<CreatedResponse>('/v1/questions', input);
  return res.data;
}

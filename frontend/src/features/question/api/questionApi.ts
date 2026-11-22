import type {
  CreateQuestionRequest,
  CreatedResponse,
  Page,
  QuestionDetail,
  QuestionSummary,
  TagCount,
} from '@/api/contracts';
import { createSeedQuestions, findCourse } from './mockQuestions';

const STORAGE_KEY = 'campusoverflow.lab5.questions';
const CURRENT_AUTHOR = { id: 1, displayName: '我（演示账号）', role: 'STUDENT', verified: false };

export interface QuestionListQuery {
  page: number;
  pageSize: number;
  keyword?: string;
  tag?: string;
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

export async function fetchQuestionList(query: QuestionListQuery): Promise<Page<QuestionSummary>> {
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

export async function fetchQuestionDetail(id: number): Promise<QuestionDetail> {
  await delay(180);
  const found = load().find((question) => question.id === id);
  if (found === undefined) throw new Error(`问题 #${id} 不存在或已被删除`);
  return found;
}

export async function fetchPopularTags(): Promise<TagCount[]> {
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

export async function createQuestion(input: CreateQuestionRequest): Promise<CreatedResponse> {
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

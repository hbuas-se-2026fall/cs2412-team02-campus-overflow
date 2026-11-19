/**
 * 业务请求 hooks（成员4 / 实验五 C 接口层）
 *
 * ## 数据源切换（重要）
 * 改下面的 USE_MOCK 即可，hooks 的调用方式完全不变：
 *   USE_MOCK = true   → 走 ./mock.ts 的模拟数据，不依赖后端是否启动
 *   USE_MOCK = false  → 走 axios 真实请求后端 /api/v1/...
 * 也可以用环境变量：VITE_USE_MOCK=true npm run dev
 */
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import api from './axios';
import type { CreateQuestionDTO, Page, QuestionDetail, QuestionSummary } from './types';
import { mockCreate, mockDelete, mockFetchDetail, mockFetchPage } from './mock';

/** 数据源开关：true = mock（默认），改成 false = 真实请求后端 */
export const USE_MOCK = true;

/** 统一 queryKey，避免各页面各写一套拼错 */
export const questionKeys = {
  list: (page: number) => ['questions', 'list', page] as const,
  detail: (id: number) => ['questions', 'detail', id] as const,
};

/** 查：问答列表 GET /api/v1/questions */
export function useQuestionList(page = 1, size = 20) {
  return useQuery({
    queryKey: questionKeys.list(page),
    queryFn: async (): Promise<Page<QuestionSummary>> => {
      if (USE_MOCK) return mockFetchPage(page, size);
      const res = await api.get<Page<QuestionSummary>>('/v1/questions', { params: { page, size } });
      return res.data;
    },
  });
}

/** 查：单条问答详情 GET /api/v1/questions/{id} */
export function useQuestionDetail(id: number) {
  return useQuery({
    queryKey: questionKeys.detail(id),
    queryFn: async (): Promise<QuestionDetail> => {
      if (USE_MOCK) return mockFetchDetail(id);
      const res = await api.get<QuestionDetail>(`/v1/questions/${id}`);
      return res.data;
    },
    enabled: id > 0,
  });
}

/** 增：发布问答 POST /api/v1/questions */
export function useCreateQuestion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (dto: CreateQuestionDTO): Promise<{ id: number }> => {
      if (USE_MOCK) return mockCreate(dto);
      const res = await api.post<{ id: number }>('/v1/questions', dto);
      return res.data;
    },
    onSuccess: () => {
      // 发布成功后让列表缓存失效，D 的页面会自动重新拉取
      qc.invalidateQueries({ queryKey: ['questions'] });
    },
  });
}

/** 删：删除问答 DELETE /api/v1/questions/{id} */
export function useDeleteQuestion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (id: number): Promise<void> => {
      if (USE_MOCK) return mockDelete(id);
      await api.delete(`/v1/questions/${id}`);
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['questions'] });
    },
  });
}

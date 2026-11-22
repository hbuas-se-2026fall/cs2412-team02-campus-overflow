import type { QuestionListQuery } from '../api/questionApi';

export const questionKeys = {
  all: ['questions'] as const,
  lists: () => [...questionKeys.all, 'list'] as const,
  list: (query: QuestionListQuery) => [...questionKeys.lists(), query] as const,
  details: () => [...questionKeys.all, 'detail'] as const,
  detail: (id: number) => [...questionKeys.details(), id] as const,
  tags: () => [...questionKeys.all, 'tags'] as const,
};

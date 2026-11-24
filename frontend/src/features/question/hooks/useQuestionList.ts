import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { fetchQuestionList, type QuestionListQuery } from '../api/questionApi';
import { questionKeys } from './queryKeys';

export function useQuestionList(query: QuestionListQuery) {
  return useQuery({
    queryKey: questionKeys.list(query),
    queryFn: () => fetchQuestionList(query),
    placeholderData: keepPreviousData,
  });
}

import { useQuery } from '@tanstack/react-query';
import { fetchQuestionDetail } from '../api/questionApi';
import { questionKeys } from './queryKeys';

export function useQuestionDetail(id: number) {
  return useQuery({
    queryKey: questionKeys.detail(id),
    queryFn: () => fetchQuestionDetail(id),
    enabled: Number.isInteger(id) && id > 0,
  });
}

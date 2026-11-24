import { useQuery } from '@tanstack/react-query';
import { fetchPopularTags } from '../api/questionApi';
import { questionKeys } from './queryKeys';

export function usePopularTags() {
  return useQuery({
    queryKey: questionKeys.tags(),
    queryFn: fetchPopularTags,
    staleTime: 60_000,
  });
}

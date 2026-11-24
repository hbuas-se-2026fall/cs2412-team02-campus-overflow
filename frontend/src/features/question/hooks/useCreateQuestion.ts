import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { CreateQuestionRequest } from '@/api/contracts';
import { createQuestion } from '../api/questionApi';
import { questionKeys } from './queryKeys';

export function useCreateQuestion() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateQuestionRequest) => createQuestion(input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: questionKeys.lists() });
      void queryClient.invalidateQueries({ queryKey: questionKeys.tags() });
    },
  });
}

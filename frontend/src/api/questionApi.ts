import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from './axios';
import { Question } from './types';

// 查询全部
export function useQuestionList() {
    return useQuery({
        queryKey: ['questions'],
        queryFn: async () => {
            const res = await api.get<Question[]>('/questions');
            return res.data;
        }
    });
}

// 查单个
export function useQuestion(id: number) {
    return useQuery({
        queryKey: ['questions', id],
        queryFn: async () => {
            const res = await api.get<Question>(`/questions/${id}`);
            return res.data;
        },
        enabled: !!id
    });
}

// 新增
export function useCreateQuestion() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async (data: Omit<Question, 'id'>) => {
            const res = await api.post<Question>('/questions', data);
            return res.data;
        },
        onSuccess: () => qc.invalidateQueries({queryKey: ['questions']})
    });
}

// 修改
export function useUpdateQuestion() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async ({id, data}: {id:number, data:Partial<Question>}) => {
            const res = await api.put<Question>(`/questions/${id}`, data);
            return res.data;
        },
        onSuccess: () => qc.invalidateQueries({queryKey: ['questions']})
    });
}

// 删除
export function useDeleteQuestion() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: async (id: number) => {
            await api.delete(`/questions/${id}`);
        },
        onSuccess: () => qc.invalidateQueries({queryKey: ['questions']})
    });
}

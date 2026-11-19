import { QueryClient } from '@tanstack/react-query';

/** 全局唯一的 QueryClient，供 main.tsx 里的 QueryClientProvider 使用 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: 0,
    },
  },
});

export default queryClient;

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Navigate, Route, Routes } from 'react-router-dom';
import { NewQuestionPage } from '@/pages/NewQuestionPage';
import { QuestionDetailPage } from '@/pages/QuestionDetailPage';
import { QuestionListPage } from '@/pages/QuestionListPage';
import { AppShell } from '@/shared/ui/AppShell';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Routes>
        <Route element={<AppShell />}>
          <Route index element={<Navigate to="/questions" replace />} />
          <Route path="/questions" element={<QuestionListPage />} />
          <Route path="/questions/new" element={<NewQuestionPage />} />
          <Route path="/questions/:id" element={<QuestionDetailPage />} />
          <Route path="*" element={<Navigate to="/questions" replace />} />
        </Route>
      </Routes>
    </QueryClientProvider>
  );
}

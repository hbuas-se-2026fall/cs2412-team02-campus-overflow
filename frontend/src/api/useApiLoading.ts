/**
 * 全局 loading：基于 react-query 的 useIsFetching，页面直接订阅即可。
 *
 * 用法（D 的页面里）：
 *   const loading = useApiLoading();
 *   {loading && <全局遮罩 />}
 */
import { useIsFetching, useIsMutating } from '@tanstack/react-query';

/** 有任意 query 或 mutation 在跑就返回 true */
export function useApiLoading(): boolean {
  const fetching = useIsFetching();
  const mutating = useIsMutating();
  return fetching > 0 || mutating > 0;
}

/** 只要 query 在跑就返回 true（用于列表页骨架屏） */
export function useQueryLoading(): boolean {
  return useIsFetching() > 0;
}

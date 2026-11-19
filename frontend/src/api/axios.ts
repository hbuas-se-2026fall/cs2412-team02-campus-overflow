import axios, { AxiosError } from 'axios';
import type { ProblemDetail } from './types';

/** 后端以 RFC 9457 Problem Details 返回错误，这里统一转成带业务码的异常 */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly code: string,
    message: string,
  ) {
    super(message);
  }
}

function cookie(name: string): string | undefined {
  return document.cookie
    .split('; ')
    .find((c) => c.startsWith(`${name}=`))
    ?.split('=')[1];
}

/** 全局 loading 计数，UI 可订阅（不引第三方状态库） */
let pending = 0;
const listeners = new Set<(loading: boolean) => void>();

function notify() {
  const loading = pending > 0;
  listeners.forEach((fn) => fn(loading));
}

export function subscribeLoading(fn: (loading: boolean) => void): () => void {
  listeners.add(fn);
  return () => listeners.delete(fn);
}

export const api = axios.create({
  baseURL: '/api', // 由 vite server.proxy 转发到 http://localhost:8080
  timeout: 10_000,
  withCredentials: true, // 会话 Cookie 同源携带
});

// 请求拦截：CSRF 双提交 + loading 计数
api.interceptors.request.use(async (config) => {
  const method = (config.method ?? 'get').toUpperCase();
  if (method !== 'GET') {
    let token = cookie('XSRF-TOKEN');
    if (!token) {
      // 首次写请求前先取一次 CSRF 令牌
      await api.get('/v1/auth/csrf');
      token = cookie('XSRF-TOKEN');
    }
    if (token) config.headers['X-XSRF-TOKEN'] = decodeURIComponent(token);
  }
  if (config.data !== undefined && !config.headers['Content-Type']) {
    config.headers['Content-Type'] = 'application/json';
  }
  pending += 1;
  notify();
  return config;
});

// 响应拦截：统一错误捕获 + loading 释放
api.interceptors.response.use(
  (res) => {
    pending = Math.max(0, pending - 1);
    notify();
    return res;
  },
  (err: AxiosError<ProblemDetail>) => {
    pending = Math.max(0, pending - 1);
    notify();
    if (err.response) {
      const data = err.response.data;
      throw new ApiError(
        err.response.status,
        data?.code ?? 'SYS-000',
        data?.detail ?? data?.title ?? '请求失败',
      );
    }
    throw new ApiError(0, 'NET-000', err.message || '网络异常');
  },
);

export default api;

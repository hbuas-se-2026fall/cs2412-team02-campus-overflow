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

/** 读取 Cookie 值：按 name= 截取，避免 token 内含 '=' 时被截断 */
function cookie(name: string): string | undefined {
  const raw = document.cookie.split('; ').find((c) => c.startsWith(`${name}=`));
  return raw ? raw.slice(name.length + 1) : undefined;
}

export const api = axios.create({
  baseURL: '/api', // 由 vite server.proxy 转发到 http://localhost:8080
  timeout: 10_000,
  withCredentials: true, // 会话 Cookie 同源携带
});

// 请求拦截：CSRF 双提交（GET /v1/auth/csrf → Cookie XSRF-TOKEN → 请求头 X-XSRF-TOKEN）
api.interceptors.request.use(async (config) => {
  const method = (config.method ?? 'get').toUpperCase();
  if (method !== 'GET') {
    let token = cookie('XSRF-TOKEN');
    if (!token) {
      // 首次写请求前先取一次令牌
      await api.get('/v1/auth/csrf');
      token = cookie('XSRF-TOKEN');
    }
    if (token) {
      try {
        config.headers['X-XSRF-TOKEN'] = decodeURIComponent(token);
      } catch {
        config.headers['X-XSRF-TOKEN'] = token;
      }
    }
  }
  return config;
});

// 响应拦截：把 RFC 9457 的 code/detail 统一转成 ApiError
api.interceptors.response.use(
  (res) => res,
  (err: AxiosError<ProblemDetail>) => {
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

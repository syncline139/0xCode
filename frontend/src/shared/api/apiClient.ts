import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { clearAccessToken, getAccessToken, setAccessToken } from './tokenManager';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

type RetriableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean;
};

let refreshPromise: Promise<string> | null = null;
let unauthorizedHandler: (() => void) | null = null;

export function setUnauthorizedHandler(handler: (() => void) | null) {
  unauthorizedHandler = handler;
}

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

apiClient.interceptors.request.use((config) => {
  const token = getAccessToken();

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

export function refreshAccessToken() {
  refreshPromise ??= axios
    .post<string>(`${API_BASE_URL}/api/auth/refresh`, undefined, {
      withCredentials: true,
    })
    .then((response) => {
      setAccessToken(response.data);
      return response.data;
    })
    .finally(() => {
      refreshPromise = null;
    });

  return refreshPromise;
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetriableRequestConfig | undefined;
    const status = error.response?.status;
    const url = originalRequest?.url ?? '';
    const isRefreshRequest = url.includes('/api/auth/refresh');

    if (status !== 401 || !originalRequest || originalRequest._retry || isRefreshRequest) {
      if (isRefreshRequest && (status === 401 || status === 403)) {
        clearAccessToken();
        unauthorizedHandler?.();
      }

      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      const newAccessToken = await refreshAccessToken();
      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
      return apiClient(originalRequest);
    } catch (refreshError) {
      const refreshStatus = axios.isAxiosError(refreshError) ? refreshError.response?.status : undefined;

      if (refreshStatus === 401 || refreshStatus === 403) {
        clearAccessToken();
        unauthorizedHandler?.();
      }

      return Promise.reject(refreshError);
    }
  },
);

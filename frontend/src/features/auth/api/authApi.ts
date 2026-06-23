import { apiClient } from '../../../shared/api/apiClient';
import type {
  CurrentUser,
  RefreshVerifyCodeRequest,
  SignInRequest,
  SignUpRequest,
  VerifyRequest,
} from '../model/types';

export const authApi = {
  register: async (payload: SignUpRequest) => {
    const response = await apiClient.post<string>('/api/auth/sign-up', payload);
    return response.data;
  },

  verify: async (payload: VerifyRequest) => {
    const response = await apiClient.post<string>('/api/auth/verify', payload);
    return response.data;
  },

  refreshVerifyCode: async (payload: RefreshVerifyCodeRequest) => {
    await apiClient.post<void>('/api/auth/refreshVerifyCode', payload);
  },

  login: async (payload: SignInRequest) => {
    const response = await apiClient.post<string>('/api/auth/sign-in', payload);
    return response.data;
  },

  logout: async () => {
    await apiClient.post<void>('/api/auth/logout');
  },

  getCurrentUser: async () => {
    const response = await apiClient.get<CurrentUser>('/api/users/me');
    return response.data;
  },
};

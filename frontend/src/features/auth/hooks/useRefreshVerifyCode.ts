import { useMutation } from '@tanstack/react-query';
import { authApi } from '../api/authApi';

export function useRefreshVerifyCode() {
  return useMutation({
    mutationFn: authApi.refreshVerifyCode,
  });
}

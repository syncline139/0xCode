import { useMutation } from '@tanstack/react-query';
import { authApi } from '../api/authApi';

export function useVerify() {
  return useMutation({
    mutationFn: authApi.verify,
  });
}

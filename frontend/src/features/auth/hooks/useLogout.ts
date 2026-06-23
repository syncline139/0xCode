import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { useAuth } from '../model/authStore';

export function useLogout() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const auth = useAuth();

  return useMutation({
    mutationFn: authApi.logout,
    onSettled: async () => {
      auth.logout();
      queryClient.clear();
      navigate('/login', { replace: true });
    },
  });
}

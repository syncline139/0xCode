import { useQuery } from '@tanstack/react-query';
import { authApi } from '../api/authApi';
import { useAuth } from '../model/authStore';

export function useCurrentUser() {
  const { isAuthenticated } = useAuth();

  return useQuery({
    queryKey: ['currentUser'],
    queryFn: authApi.getCurrentUser,
    enabled: isAuthenticated,
  });
}

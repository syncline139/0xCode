import { createContext, useCallback, useContext, useEffect, useMemo, useState, type PropsWithChildren } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import {
  clearAccessToken,
  getAccessToken,
  setAccessToken as saveAccessToken,
  subscribeAccessToken,
} from '../../../shared/api/tokenManager';
import { refreshAccessToken, setUnauthorizedHandler } from '../../../shared/api/apiClient';

type AuthContextValue = {
  accessToken: string | null;
  isAuthenticated: boolean;
  isAuthLoading: boolean;
  login: (token: string) => void;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [accessToken, setAccessTokenState] = useState<string | null>(() => getAccessToken());
  const [isAuthLoading, setIsAuthLoading] = useState(true);
  const queryClient = useQueryClient();

  const login = useCallback((token: string) => {
    saveAccessToken(token);
  }, []);

  const logout = useCallback(() => {
    clearAccessToken();
  }, []);

  useEffect(() => subscribeAccessToken(setAccessTokenState), []);

  useEffect(() => {
    let isMounted = true;

    refreshAccessToken()
      .catch(() => {
        clearAccessToken();
      })
      .finally(() => {
        if (isMounted) {
          setIsAuthLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(() => {
      logout();
      queryClient.clear();
      window.location.assign('/login');
    });

    return () => setUnauthorizedHandler(null);
  }, [logout, queryClient]);

  const value = useMemo<AuthContextValue>(
    () => ({
      accessToken,
      isAuthenticated: Boolean(accessToken),
      isAuthLoading,
      login,
      logout,
    }),
    [accessToken, isAuthLoading, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }

  return context;
}

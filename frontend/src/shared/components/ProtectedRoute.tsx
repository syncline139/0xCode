import { Navigate, useLocation } from 'react-router-dom';
import type { PropsWithChildren } from 'react';
import { useAuth } from '../../features/auth/model/authStore';

export function ProtectedRoute({ children }: PropsWithChildren) {
  const { isAuthenticated, isAuthLoading } = useAuth();
  const location = useLocation();

  if (isAuthLoading) {
    return (
      <div className="rounded-lg border border-slate-200 bg-white p-6 text-sm text-slate-600 shadow-sm">
        Проверка авторизации...
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return children;
}

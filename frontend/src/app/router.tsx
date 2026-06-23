import { createBrowserRouter } from 'react-router-dom';
import { Layout } from '../shared/components/Layout';
import { ProtectedRoute } from '../shared/components/ProtectedRoute';
import { HomePage } from '../pages/HomePage';
import { LoginPage } from '../pages/LoginPage';
import { RegisterPage } from '../pages/RegisterPage';
import { VerifyPage } from '../pages/VerifyPage';
import { ProfilePage } from '../pages/ProfilePage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'register', element: <RegisterPage /> },
      { path: 'verify', element: <VerifyPage /> },
      { path: 'login', element: <LoginPage /> },
      {
        path: 'profile',
        element: (
          <ProtectedRoute>
            <ProfilePage />
          </ProtectedRoute>
        ),
      },
    ],
  },
]);

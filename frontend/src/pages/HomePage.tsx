import { Link } from 'react-router-dom';
import { useAuth } from '../features/auth/model/authStore';
import { useCurrentUser } from '../features/auth/hooks/useCurrentUser';

export function HomePage() {
  const { isAuthenticated } = useAuth();
  const currentUserQuery = useCurrentUser();

  return (
    <section className="space-y-6">
      <div className="space-y-3">
        <h1 className="text-3xl font-semibold tracking-tight text-slate-950">Spring Boot JWT Frontend</h1>
        <p className="max-w-2xl text-slate-600">
          Минимальный React-клиент для регистрации, подтверждения email, входа, refresh access token и профиля.
        </p>
      </div>

      {isAuthenticated && currentUserQuery.data && (
        <div className="rounded-md border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
          Вы вошли как {currentUserQuery.data.email}
        </div>
      )}

      <div className="flex flex-wrap gap-3">
        <Link className="rounded-md bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800" to="/login">
          Login
        </Link>
        <Link
          className="rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-100"
          to="/register"
        >
          Register
        </Link>
        <Link
          className="rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-100"
          to="/profile"
        >
          Profile
        </Link>
      </div>
    </section>
  );
}

import { Link } from 'react-router-dom';
import { LoginForm } from '../features/auth/components/LoginForm';

export function LoginPage() {
  return (
    <section className="mx-auto max-w-md space-y-6">
      <div className="space-y-2">
        <h1 className="text-2xl font-semibold tracking-tight text-slate-950">Вход</h1>
        <p className="text-sm text-slate-600">Access token будет храниться только в памяти приложения.</p>
      </div>
      <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <LoginForm />
      </div>
      <p className="text-center text-sm text-slate-600">
        Нет аккаунта?{' '}
        <Link className="font-medium text-slate-950 underline underline-offset-4" to="/register">
          Зарегистрироваться
        </Link>
      </p>
    </section>
  );
}

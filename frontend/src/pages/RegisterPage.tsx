import { Link } from 'react-router-dom';
import { RegisterForm } from '../features/auth/components/RegisterForm';

export function RegisterPage() {
  return (
    <section className="mx-auto max-w-md space-y-6">
      <div className="space-y-2">
        <h1 className="text-2xl font-semibold tracking-tight text-slate-950">Регистрация</h1>
        <p className="text-sm text-slate-600">Создайте аккаунт и подтвердите email кодом из письма.</p>
      </div>
      <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <RegisterForm />
      </div>
      <p className="text-center text-sm text-slate-600">
        Уже есть аккаунт?{' '}
        <Link className="font-medium text-slate-950 underline underline-offset-4" to="/login">
          Войти
        </Link>
      </p>
    </section>
  );
}

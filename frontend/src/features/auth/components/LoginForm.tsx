import { zodResolver } from '@hookform/resolvers/zod';
import { useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { useLogin } from '../hooks/useLogin';
import { useAuth } from '../model/authStore';
import { FieldError, FormMessage, getErrorMessage } from './formUtils';

const loginSchema = z.object({
  email: z.string().email('Введите корректный email'),
  password: z.string().min(1, 'Введите пароль'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export function LoginForm() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const auth = useAuth();
  const loginMutation = useLogin();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  });

  const onSubmit = (values: LoginFormValues) => {
    loginMutation.mutate(values, {
      onSuccess: (token) => {
        auth.login(token);
        queryClient.invalidateQueries({ queryKey: ['currentUser'] });
        navigate('/profile', { replace: true });
      },
    });
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <FormMessage type="error">{getErrorMessage(loginMutation.error, '')}</FormMessage>

      <label className="block">
        <span className="mb-1 block text-sm font-medium text-slate-700">Email</span>
        <input type="email" autoComplete="email" {...register('email')} />
        <FieldError message={errors.email?.message} />
      </label>

      <label className="block">
        <span className="mb-1 block text-sm font-medium text-slate-700">Password</span>
        <input type="password" autoComplete="current-password" {...register('password')} />
        <FieldError message={errors.password?.message} />
      </label>

      <button
        type="submit"
        disabled={loginMutation.isPending}
        className="w-full rounded-md bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {loginMutation.isPending ? 'Вход...' : 'Войти'}
      </button>
    </form>
  );
}

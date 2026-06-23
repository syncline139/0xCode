import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { useRegister } from '../hooks/useRegister';
import { FieldError, FormMessage, getErrorMessage } from './formUtils';

const registerSchema = z.object({
  email: z.string().email('Введите корректный email'),
  password: z.string().min(6, 'Минимум 6 символов'),
});

type RegisterFormValues = z.infer<typeof registerSchema>;

export function RegisterForm() {
  const navigate = useNavigate();
  const registerMutation = useRegister();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { email: '', password: '' },
  });

  const onSubmit = (values: RegisterFormValues) => {
    registerMutation.mutate(values, {
      onSuccess: (message) => {
        navigate('/verify', {
          state: {
            email: values.email,
            password: values.password,
            message,
          },
        });
      },
    });
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <FormMessage type="error">{getErrorMessage(registerMutation.error, '')}</FormMessage>

      <label className="block">
        <span className="mb-1 block text-sm font-medium text-slate-700">Email</span>
        <input type="email" autoComplete="email" {...register('email')} />
        <FieldError message={errors.email?.message} />
      </label>

      <label className="block">
        <span className="mb-1 block text-sm font-medium text-slate-700">Password</span>
        <input type="password" autoComplete="new-password" {...register('password')} />
        <FieldError message={errors.password?.message} />
      </label>

      <button
        type="submit"
        disabled={registerMutation.isPending}
        className="w-full rounded-md bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {registerMutation.isPending ? 'Регистрация...' : 'Зарегистрироваться'}
      </button>
    </form>
  );
}

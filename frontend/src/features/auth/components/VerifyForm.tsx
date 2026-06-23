import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useLocation, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { useRefreshVerifyCode } from '../hooks/useRefreshVerifyCode';
import { useVerify } from '../hooks/useVerify';
import { FieldError, FormMessage, getErrorMessage } from './formUtils';

type VerifyLocationState = {
  email?: string;
  password?: string;
  message?: string;
};

const verifySchema = z.object({
  email: z.string().email('Введите корректный email'),
  code: z.string().min(1, 'Введите код подтверждения'),
  password: z.string().optional(),
});

type VerifyFormValues = z.infer<typeof verifySchema>;

export function VerifyForm() {
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state as VerifyLocationState | null;
  const [successMessage, setSuccessMessage] = useState(state?.message ?? '');
  const [resendValidationError, setResendValidationError] = useState('');
  const verifyMutation = useVerify();
  const refreshMutation = useRefreshVerifyCode();
  const {
    register,
    handleSubmit,
    getValues,
    watch,
    formState: { errors },
  } = useForm<VerifyFormValues>({
    resolver: zodResolver(verifySchema),
    defaultValues: {
      email: state?.email ?? '',
      code: '',
      password: state?.password ?? '',
    },
  });

  const onSubmit = (values: VerifyFormValues) => {
    verifyMutation.mutate(
      { email: values.email, code: values.code },
      {
        onSuccess: () => {
          navigate('/login', { replace: true });
        },
      },
    );
  };

  const onResendCode = () => {
    const { email, password } = getValues();

    if (!email || !password) {
      setSuccessMessage('');
      setResendValidationError('Для повторной отправки кода нужны email и password.');
      refreshMutation.reset();
      return;
    }

    setResendValidationError('');
    refreshMutation.mutate(
      { email, password },
      {
        onSuccess: () => setSuccessMessage('Код подтверждения отправлен повторно'),
      },
    );
  };

  const resendNeedsPassword = !watch('password');

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <FormMessage type="success">{successMessage}</FormMessage>
      <FormMessage type="error">
        {resendValidationError || getErrorMessage(verifyMutation.error ?? refreshMutation.error, '')}
      </FormMessage>

      <label className="block">
        <span className="mb-1 block text-sm font-medium text-slate-700">Email</span>
        <input type="email" autoComplete="email" {...register('email')} />
        <FieldError message={errors.email?.message} />
      </label>

      <label className="block">
        <span className="mb-1 block text-sm font-medium text-slate-700">Код подтверждения</span>
        <input type="text" inputMode="numeric" autoComplete="one-time-code" {...register('code')} />
        <FieldError message={errors.code?.message} />
      </label>

      <label className="block">
        <span className="mb-1 block text-sm font-medium text-slate-700">
          Password для повторной отправки кода
        </span>
        <input type="password" autoComplete="current-password" {...register('password')} />
        {resendNeedsPassword && (
          <p className="mt-1 text-sm text-slate-500">Нужен только для кнопки повторной отправки.</p>
        )}
      </label>

      <button
        type="submit"
        disabled={verifyMutation.isPending}
        className="w-full rounded-md bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {verifyMutation.isPending ? 'Подтверждение...' : 'Подтвердить аккаунт'}
      </button>

      <button
        type="button"
        disabled={refreshMutation.isPending}
        onClick={onResendCode}
        className="w-full rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {refreshMutation.isPending ? 'Отправка...' : 'Отправить код повторно'}
      </button>
    </form>
  );
}

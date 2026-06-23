import { VerifyForm } from '../features/auth/components/VerifyForm';

export function VerifyPage() {
  return (
    <section className="mx-auto max-w-md space-y-6">
      <div className="space-y-2">
        <h1 className="text-2xl font-semibold tracking-tight text-slate-950">Подтверждение email</h1>
        <p className="text-sm text-slate-600">Введите код подтверждения. Email можно изменить вручную.</p>
      </div>
      <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <VerifyForm />
      </div>
    </section>
  );
}

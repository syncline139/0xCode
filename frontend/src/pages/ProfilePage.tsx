import { useCurrentUser } from '../features/auth/hooks/useCurrentUser';
import { useLogout } from '../features/auth/hooks/useLogout';
import { getErrorMessage } from '../features/auth/components/formUtils';

export function ProfilePage() {
  const currentUserQuery = useCurrentUser();
  const logoutMutation = useLogout();

  return (
    <section className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight text-slate-950">Profile</h1>
          <p className="text-sm text-slate-600">Защищённая страница текущего пользователя.</p>
        </div>
        <button
          type="button"
          onClick={() => logoutMutation.mutate()}
          disabled={logoutMutation.isPending}
          className="rounded-md bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {logoutMutation.isPending ? 'Выход...' : 'Logout'}
        </button>
      </div>

      {currentUserQuery.isLoading && (
        <div className="rounded-lg border border-slate-200 bg-white p-6 text-sm text-slate-600 shadow-sm">
          Загрузка профиля...
        </div>
      )}

      {currentUserQuery.isError && (
        <div className="rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {getErrorMessage(currentUserQuery.error, 'Не удалось загрузить профиль')}
        </div>
      )}

      {currentUserQuery.data && (
        <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <dl className="grid gap-4 sm:grid-cols-2">
            <div>
              <dt className="text-sm font-medium text-slate-500">Email</dt>
              <dd className="mt-1 text-base text-slate-950">{currentUserQuery.data.email}</dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-slate-500">Role</dt>
              <dd className="mt-1 text-base text-slate-950">{currentUserQuery.data.role ?? 'Не указана'}</dd>
            </div>
            {currentUserQuery.data.id !== undefined && (
              <div>
                <dt className="text-sm font-medium text-slate-500">ID</dt>
                <dd className="mt-1 text-base text-slate-950">{currentUserQuery.data.id}</dd>
              </div>
            )}
          </dl>
          <pre className="mt-6 overflow-auto rounded-md bg-slate-950 p-4 text-xs text-slate-100">
            {JSON.stringify(currentUserQuery.data, null, 2)}
          </pre>
        </div>
      )}
    </section>
  );
}

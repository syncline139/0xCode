import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../../features/auth/model/authStore';

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-2 text-sm font-medium ${
    isActive ? 'bg-slate-900 text-white' : 'text-slate-700 hover:bg-slate-100 hover:text-slate-950'
  }`;

export function Layout() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="min-h-screen">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4">
          <NavLink to="/" className="text-base font-semibold text-slate-950">
            JWT Auth
          </NavLink>
          <nav className="flex items-center gap-1">
            <NavLink to="/login" className={navLinkClass}>
              Login
            </NavLink>
            <NavLink to="/register" className={navLinkClass}>
              Register
            </NavLink>
            {isAuthenticated && (
              <NavLink to="/profile" className={navLinkClass}>
                Profile
              </NavLink>
            )}
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-10">
        <Outlet />
      </main>
    </div>
  );
}

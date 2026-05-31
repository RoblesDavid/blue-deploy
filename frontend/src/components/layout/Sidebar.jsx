import { Link, useLocation } from 'react-router-dom'
import { LayoutDashboard, Server, Users, LogOut, User, Shield } from 'lucide-react'
import { useAuth } from '../../auth/AuthContext'

const ADMIN_NAV = [
  { path: '/admin', icon: LayoutDashboard, label: 'Dashboard' },
  { path: '/admin/deployments', icon: Server, label: 'Despliegues' },
  { path: '/admin/technicians', icon: Users, label: 'Técnicos' },
]

const TECH_NAV = [
  { path: '/technician', icon: LayoutDashboard, label: 'Mi Dashboard' },
  { path: '/technician/deployments', icon: Server, label: 'Mis Despliegues' },
  { path: '/technician/profile', icon: User, label: 'Mi Perfil' },
]

export default function Sidebar() {
  const { user, logout } = useAuth()
  const location = useLocation()
  const nav = user?.role === 'ADMIN' ? ADMIN_NAV : TECH_NAV

  return (
    <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col shrink-0">
      {/* Brand */}
      <div className="p-5 border-b border-slate-800">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-cyan-400 flex items-center justify-center shrink-0">
            <Shield className="w-5 h-5 text-white" />
          </div>
          <div>
            <p className="text-white font-bold text-base leading-none">Blue Deploy</p>
            <p className="text-slate-500 text-xs mt-1">Blue Team Platform</p>
          </div>
        </div>
      </div>

      {/* Role badge */}
      <div className="px-5 pt-4 pb-2">
        <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${
          user?.role === 'ADMIN'
            ? 'bg-purple-500/20 text-purple-400'
            : 'bg-blue-500/20 text-blue-400'
        }`}>
          {user?.role === 'ADMIN' ? 'Administrador' : 'Técnico'}
        </span>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 py-2">
        {nav.map(({ path, icon: Icon, label }) => {
          const active = location.pathname === path
          return (
            <Link
              key={path}
              to={path}
              className={`flex items-center gap-3 px-4 py-2.5 rounded-xl mb-1 transition-all text-sm font-medium ${
                active
                  ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/40'
                  : 'text-slate-400 hover:bg-slate-800 hover:text-white'
              }`}
            >
              <Icon className="w-4 h-4 shrink-0" />
              {label}
            </Link>
          )
        })}
      </nav>

      {/* User info + logout */}
      <div className="p-4 border-t border-slate-800">
        <div className="flex items-center gap-3 mb-3 px-1">
          <div className="w-8 h-8 rounded-full bg-blue-500/20 flex items-center justify-center text-blue-400 text-xs font-bold shrink-0">
            {user?.initials}
          </div>
          <div className="min-w-0">
            <p className="text-white text-sm font-medium truncate leading-none">{user?.name}</p>
            <p className="text-slate-500 text-xs truncate mt-1">{user?.department}</p>
          </div>
        </div>
        <button
          onClick={logout}
          className="w-full flex items-center gap-2 px-3 py-2 text-slate-400 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-all text-sm"
        >
          <LogOut className="w-4 h-4" />
          Cerrar sesión
        </button>
      </div>
    </aside>
  )
}

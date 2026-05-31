import { useLocation } from 'react-router-dom'
import { Bell } from 'lucide-react'
import { useAuth } from '../../auth/AuthContext'

const PAGE_TITLES = {
  '/admin': 'Dashboard General',
  '/admin/deployments': 'Todos los Despliegues',
  '/admin/technicians': 'Gestión de Técnicos',
  '/technician': 'Mi Dashboard',
  '/technician/deployments': 'Mis Despliegues',
  '/technician/profile': 'Mi Perfil',
}

export default function Header() {
  const { user } = useAuth()
  const location = useLocation()
  const title = PAGE_TITLES[location.pathname] ?? 'BlueDeploy'

  return (
    <header className="h-14 bg-slate-900 border-b border-slate-800 flex items-center justify-between px-6 shrink-0">
      <h1 className="text-white font-semibold text-base">{title}</h1>

      <div className="flex items-center gap-3">
        <button className="relative p-2 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-all">
          <Bell className="w-4 h-4" />
          <span className="absolute top-1.5 right-1.5 w-1.5 h-1.5 bg-blue-500 rounded-full" />
        </button>

        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-full bg-blue-500/20 flex items-center justify-center text-blue-400 text-xs font-bold">
            {user?.initials}
          </div>
          <span className="text-slate-300 text-sm hidden sm:block">{user?.name}</span>
        </div>
      </div>
    </header>
  )
}

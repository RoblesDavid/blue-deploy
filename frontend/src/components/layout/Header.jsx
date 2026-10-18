import { useState, useEffect, useRef } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { Bell, AlertTriangle, ShieldAlert, CheckCircle2, Clock, X, CheckCheck, Info } from 'lucide-react'
import { useAuth } from '../../auth/AuthContext'
import deploymentsApi from '../../api/deploymentsApi'
import DeploymentDetail from '../deployments/DeploymentDetail'

const PAGE_TITLES = {
  '/admin': 'Dashboard General',
  '/admin/deployments': 'Todos los Despliegues',
  '/admin/technicians': 'Gestión de Técnicos',
  '/admin/audit': 'Registro de Auditoría',
  '/technician': 'Mi Dashboard',
  '/technician/deployments': 'Mis Despliegues',
  '/technician/profile': 'Mi Perfil',
}

export default function Header() {
  const { user } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const title = PAGE_TITLES[location.pathname] ?? 'BlueDeploy'

  const [isOpen, setIsOpen] = useState(false)
  const [selectedDeployment, setSelectedDeployment] = useState(null)
  const [notifications, setNotifications] = useState([])
  const [readIds, setReadIds] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('bluedeploy_read_notifications') || '[]')
    } catch {
      return []
    }
  })
  const [loading, setLoading] = useState(false)
  const dropdownRef = useRef(null)

  // Close dropdown on outside click
  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false)
      }
    }
    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside)
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
    }
  }, [isOpen])

  async function loadNotifications() {
    if (!user) return
    setLoading(true)
    try {
      const list = await deploymentsApi.getDeployments().catch(() => [])
      const notifs = []

      list.forEach((dep) => {
        if (dep.complianceStatus === 'NON_COMPLIANT' || dep.deploymentStatus === 'FAILED') {
          notifs.push({
            id: `fail-${dep.deploymentId}`,
            type: 'error',
            title: `Incidencia en ${dep.hostname}`,
            message: `El despliegue ${dep.deploymentId} presenta fallos de cumplimiento o instalación.`,
            timestamp: dep.lastSeenAt || dep.startedAt,
            deploymentId: dep.deploymentId,
            link: user.role === 'ADMIN' ? '/admin/deployments' : '/technician/deployments',
          })
        } else if (dep.deploymentStatus === 'COMPLETED_WITH_WARNINGS') {
          notifs.push({
            id: `warn-${dep.deploymentId}`,
            type: 'warning',
            title: `Advertencias en ${dep.hostname}`,
            message: `Despliegue finalizado con comprobaciones no críticas pendientes.`,
            timestamp: dep.lastSeenAt || dep.startedAt,
            deploymentId: dep.deploymentId,
            link: user.role === 'ADMIN' ? '/admin/deployments' : '/technician/deployments',
          })
        } else if (user.role === 'ADMIN' && !dep.technicianId && dep.deploymentStatus === 'IN_PROGRESS') {
          notifs.push({
            id: `unassigned-${dep.deploymentId}`,
            type: 'info',
            title: `Despliegue sin asignar: ${dep.hostname}`,
            message: `Nuevo equipo registrado por script sin técnico asignado.`,
            timestamp: dep.startedAt,
            deploymentId: dep.deploymentId,
            link: '/admin/deployments',
          })
        }
      })

      setNotifications(notifs)
    } catch (err) {
      console.error('Error fetching notifications:', err)
    } finally {
      setLoading(false)
    }
  }

  // Fetch deployments to derive notifications
  useEffect(() => {
    loadNotifications()
    const interval = setInterval(loadNotifications, 30000) // Poll every 30s
    return () => clearInterval(interval)
  }, [user])

  const unreadCount = notifications.filter((n) => !readIds.includes(n.id)).length

  function markAllAsRead() {
    const allIds = notifications.map((n) => n.id)
    setReadIds(allIds)
    localStorage.setItem('bluedeploy_read_notifications', JSON.stringify(allIds))
  }

  async function handleNotificationClick(notif) {
    if (!readIds.includes(notif.id)) {
      const updated = [...readIds, notif.id]
      setReadIds(updated)
      localStorage.setItem('bluedeploy_read_notifications', JSON.stringify(updated))
    }
    setIsOpen(false)
    if (notif.deploymentId) {
      try {
        const dep = await deploymentsApi.getDeploymentById(notif.deploymentId)
        if (dep) {
          setSelectedDeployment(dep)
          return
        }
      } catch (err) {
        console.error('Error fetching notification deployment:', err)
      }
    }
    if (notif.link) {
      navigate(notif.link)
    }
  }

  return (
    <header className="h-14 bg-slate-900 border-b border-slate-800 flex items-center justify-between px-6 shrink-0 relative z-30">
      <h1 className="text-white font-semibold text-base">{title}</h1>

      <div className="flex items-center gap-3">
        {/* Notification Bell */}
        <div className="relative" ref={dropdownRef}>
          <button
            onClick={() => setIsOpen((prev) => !prev)}
            aria-label="Notificaciones"
            className="relative p-2 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-all focus:outline-none"
          >
            <Bell className="w-4 h-4" />
            {unreadCount > 0 && (
              <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full animate-pulse" />
            )}
          </button>

          {/* Dropdown Panel */}
          {isOpen && (
            <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-slate-900 border border-slate-700/80 rounded-2xl shadow-2xl shadow-black/80 overflow-hidden z-50">
              <div className="p-3.5 bg-slate-800/80 border-b border-slate-700/60 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Bell className="w-4 h-4 text-blue-400" />
                  <span className="text-white text-xs font-semibold">Notificaciones</span>
                  {unreadCount > 0 && (
                    <span className="bg-red-500/20 text-red-400 border border-red-500/30 text-[10px] font-bold px-1.5 py-0.2 rounded-full">
                      {unreadCount} nuevas
                    </span>
                  )}
                </div>
                {unreadCount > 0 && (
                  <button
                    onClick={markAllAsRead}
                    className="text-[11px] text-slate-400 hover:text-blue-400 flex items-center gap-1 transition-colors"
                  >
                    <CheckCheck className="w-3 h-3" />
                    Marcar leídas
                  </button>
                )}
              </div>

              <div className="max-h-80 overflow-y-auto divide-y divide-slate-800/60">
                {loading && notifications.length === 0 ? (
                  <div className="p-6 text-center text-slate-500 text-xs">Cargando notificaciones...</div>
                ) : notifications.length === 0 ? (
                  <div className="p-6 text-center text-slate-500 text-xs flex flex-col items-center gap-2">
                    <CheckCircle2 className="w-6 h-6 text-green-500/50" />
                    <span>No hay alertas ni incidencias pendientes.</span>
                  </div>
                ) : (
                  notifications.map((notif) => {
                    const isUnread = !readIds.includes(notif.id)
                    return (
                      <div
                        key={notif.id}
                        onClick={() => handleNotificationClick(notif)}
                        className={`p-3.5 flex items-start gap-3 cursor-pointer transition-colors ${
                          isUnread ? 'bg-slate-800/40 hover:bg-slate-800/70' : 'hover:bg-slate-800/30 opacity-75'
                        }`}
                      >
                        <div className="mt-0.5 shrink-0">
                          {notif.type === 'error' && <ShieldAlert className="w-4 h-4 text-red-400" />}
                          {notif.type === 'warning' && <AlertTriangle className="w-4 h-4 text-yellow-400" />}
                          {notif.type === 'info' && <Info className="w-4 h-4 text-blue-400" />}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center justify-between gap-1 mb-0.5">
                            <p className="text-white text-xs font-medium truncate">{notif.title}</p>
                            {isUnread && (
                              <span className="w-1.5 h-1.5 rounded-full bg-blue-400 shrink-0" />
                            )}
                          </div>
                          <p className="text-slate-400 text-[11px] leading-relaxed line-clamp-2">
                            {notif.message}
                          </p>
                        </div>
                      </div>
                    )
                  })
                )}
              </div>

              <div className="p-2.5 bg-slate-950/60 border-t border-slate-800 text-center">
                <button
                  onClick={() => {
                    setIsOpen(false)
                    navigate(user?.role === 'ADMIN' ? '/admin/deployments' : '/technician/deployments')
                  }}
                  className="text-xs text-blue-400 hover:text-blue-300 font-medium transition-colors"
                >
                  Ver todos los despliegues →
                </button>
              </div>
            </div>
          )}
        </div>

        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-full bg-blue-500/20 flex items-center justify-center text-blue-400 text-xs font-bold">
            {user?.initials}
          </div>
          <span className="text-slate-300 text-sm hidden sm:block">{user?.name}</span>
        </div>
      </div>

      {selectedDeployment && (
        <DeploymentDetail
          deployment={selectedDeployment}
          onClose={() => setSelectedDeployment(null)}
          onUpdated={loadNotifications}
        />
      )}
    </header>
  )
}

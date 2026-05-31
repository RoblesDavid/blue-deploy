import { useMemo } from 'react'
import { Server, CheckCircle2, XCircle, Clock } from 'lucide-react'
import StatCard from '../../components/common/StatCard'
import DeploymentTable from '../../components/deployments/DeploymentTable'
import Badge from '../../components/common/Badge'
import { useAuth } from '../../auth/AuthContext'
import { MOCK_DEPLOYMENTS } from '../../data/mockDeployments'

export default function TechnicianDashboard() {
  const { user } = useAuth()

  const myDeps = useMemo(
    () => MOCK_DEPLOYMENTS.filter((d) => d.technicianId === user?.username),
    [user]
  )

  const stats = useMemo(() => ({
    total: myDeps.length,
    active: myDeps.filter((d) => ['IN_PROGRESS', 'DOMAIN_JOINED', 'POST_DOMAIN_PENDING'].includes(d.deploymentStatus)).length,
    completed: myDeps.filter((d) => ['COMPLETED', 'COMPLETED_WITH_WARNINGS'].includes(d.deploymentStatus)).length,
    failed: myDeps.filter((d) => ['FAILED', 'NON_COMPLIANT'].includes(d.deploymentStatus)).length,
  }), [myDeps])

  const recent = myDeps.slice().sort((a, b) => new Date(b.lastSeenAt) - new Date(a.lastSeenAt)).slice(0, 3)

  return (
    <div className="space-y-6">
      {/* Welcome banner */}
      <div className="bg-gradient-to-r from-blue-600/20 to-cyan-600/10 border border-blue-500/20 rounded-2xl p-5">
        <p className="text-slate-400 text-sm">Bienvenido/a,</p>
        <h2 className="text-white font-bold text-xl mt-0.5">{user?.name}</h2>
        <p className="text-slate-500 text-xs mt-1">{user?.department}</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <StatCard title="Mis despliegues" value={stats.total} icon={Server} color="blue" />
        <StatCard title="En progreso" value={stats.active} icon={Clock} color="cyan" />
        <StatCard title="Completados" value={stats.completed} icon={CheckCircle2} color="green" />
        <StatCard title="Con errores" value={stats.failed} icon={XCircle} color="red" />
      </div>

      {/* Status overview */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {myDeps.map((dep) => (
          <div key={dep.deploymentId} className="bg-slate-800/60 border border-slate-700/50 rounded-xl p-4 flex items-center gap-4">
            <div className="w-9 h-9 rounded-xl bg-slate-700 flex items-center justify-center shrink-0">
              <Server className="w-4 h-4 text-slate-400" />
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-white text-sm font-medium truncate">{dep.hostname}</p>
              <p className="text-slate-500 text-xs truncate">{dep.deviceModel}</p>
            </div>
            <Badge status={dep.deploymentStatus} />
          </div>
        ))}
      </div>

      {/* Recent activity */}
      <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl">
        <div className="p-5 border-b border-slate-700/50">
          <h2 className="text-white font-semibold text-sm">Actividad reciente</h2>
          <p className="text-slate-500 text-xs mt-0.5">Últimos despliegues actualizados</p>
        </div>
        <DeploymentTable deployments={recent} />
      </div>
    </div>
  )
}

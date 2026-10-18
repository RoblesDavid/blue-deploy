import { useState, useEffect, useMemo } from 'react'
import { Server, CheckCircle2, XCircle, Clock } from 'lucide-react'
import StatCard from '../../components/common/StatCard'
import DeploymentTable from '../../components/deployments/DeploymentTable'
import DeploymentDetail from '../../components/deployments/DeploymentDetail'
import Badge from '../../components/common/Badge'
import { useAuth } from '../../auth/AuthContext'
import dashboardApi from '../../api/dashboardApi'
import deploymentsApi from '../../api/deploymentsApi'

export default function TechnicianDashboard() {
  const { user } = useAuth()
  const [summary, setSummary] = useState(null)
  const [myDeps, setMyDeps] = useState([])
  const [selectedDep, setSelectedDep] = useState(null)
  const [loading, setLoading] = useState(true)

  async function loadData() {
    try {
      setLoading(true)
      const [sumRes, depsRes] = await Promise.all([
        dashboardApi.getSummary().catch(() => null),
        deploymentsApi.getMyDeployments().catch(() => []),
      ])
      setSummary(sumRes)
      setMyDeps(depsRes || [])
    } catch (err) {
      console.error('Error loading technician dashboard:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const stats = useMemo(() => {
    if (summary) {
      return {
        total: summary.totalDeployments ?? myDeps.length,
        active: (summary.inProgress ?? 0) + (summary.domainJoined ?? 0) + (summary.postDomainPending ?? 0),
        completed: (summary.completed ?? 0) + (summary.completedWithWarnings ?? 0),
        failed: (summary.failed ?? 0) + (summary.nonCompliant ?? 0),
      }
    }
    return {
      total: myDeps.length,
      active: myDeps.filter((d) => ['IN_PROGRESS', 'DOMAIN_JOINED', 'POST_DOMAIN_PENDING'].includes(d.deploymentStatus)).length,
      completed: myDeps.filter((d) => ['COMPLETED', 'COMPLETED_WITH_WARNINGS'].includes(d.deploymentStatus)).length,
      failed: myDeps.filter((d) => ['FAILED', 'NON_COMPLIANT'].includes(d.deploymentStatus)).length,
    }
  }, [summary, myDeps])

  const recent = useMemo(() => {
    return [...myDeps]
      .sort((a, b) => new Date(b.lastSeenAt || b.startedAt || 0) - new Date(a.lastSeenAt || a.startedAt || 0))
      .slice(0, 5)
  }, [myDeps])

  if (loading) {
    return (
      <div className="flex items-center justify-center p-12">
        <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Welcome banner */}
      <div className="bg-gradient-to-r from-blue-600/20 to-cyan-600/10 border border-blue-500/20 rounded-2xl p-5">
        <p className="text-slate-400 text-sm">Bienvenido/a,</p>
        <h2 className="text-white font-bold text-xl mt-0.5">{user?.name}</h2>
        <p className="text-slate-500 text-xs mt-1">{user?.department || 'Departamento de Operaciones'}</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <StatCard title="Mis despliegues" value={stats.total} icon={Server} color="blue" />
        <StatCard title="En progreso" value={stats.active} icon={Clock} color="cyan" />
        <StatCard title="Completados" value={stats.completed} icon={CheckCircle2} color="green" />
        <StatCard title="Con errores" value={stats.failed} icon={XCircle} color="red" />
      </div>

      {/* Status overview cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {myDeps.length === 0 ? (
          <div className="sm:col-span-2 p-6 bg-slate-800/40 rounded-xl text-center text-slate-500 text-sm">
            Actualmente no tienes despliegues asignados.
          </div>
        ) : (
          myDeps.slice(0, 4).map((dep) => (
            <div
              key={dep.deploymentId}
              onClick={() => setSelectedDep(dep)}
              className="bg-slate-800/60 hover:bg-slate-800/90 border border-slate-700/50 hover:border-blue-500/40 rounded-xl p-4 flex items-center gap-4 cursor-pointer transition-all"
            >
              <div className="w-9 h-9 rounded-xl bg-slate-700 flex items-center justify-center shrink-0">
                <Server className="w-4 h-4 text-slate-400" />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-white text-sm font-medium truncate">{dep.hostname}</p>
                <p className="text-slate-500 text-xs truncate">{dep.deviceModel || dep.deploymentId}</p>
              </div>
              <Badge status={dep.deploymentStatus} />
            </div>
          ))
        )}
      </div>

      {/* Recent activity */}
      <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl">
        <div className="p-5 border-b border-slate-700/50">
          <h2 className="text-white font-semibold text-sm">Actividad reciente</h2>
          <p className="text-slate-500 text-xs mt-0.5">Tus últimos despliegues actualizados</p>
        </div>
        <DeploymentTable deployments={recent} onRefresh={loadData} />
      </div>

      {selectedDep && (
        <DeploymentDetail
          deployment={selectedDep}
          onClose={() => setSelectedDep(null)}
          onUpdated={loadData}
        />
      )}
    </div>
  )
}

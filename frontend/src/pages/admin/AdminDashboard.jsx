import { useState, useEffect, useMemo } from 'react'
import { Server, CheckCircle2, XCircle, AlertTriangle, Clock, Users, ShieldAlert, Cpu, PieChart } from 'lucide-react'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  ArcElement,
  Tooltip,
  Legend,
} from 'chart.js'
import { Bar, Doughnut } from 'react-chartjs-2'
import StatCard from '../../components/common/StatCard'
import DeploymentTable from '../../components/deployments/DeploymentTable'
import dashboardApi from '../../api/dashboardApi'
import deploymentsApi from '../../api/deploymentsApi'
import usersApi from '../../api/usersApi'

ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, Tooltip, Legend)

const CHART_OPTIONS = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'top',
      labels: { color: '#94a3b8', font: { size: 11 }, padding: 14 },
    },
    tooltip: { mode: 'index', intersect: false },
  },
  scales: {
    x: {
      stacked: true,
      ticks: { color: '#94a3b8', font: { size: 11 } },
      grid: { color: 'rgba(100,116,139,0.12)' },
    },
    y: {
      stacked: true,
      beginAtZero: true,
      ticks: { color: '#94a3b8', stepSize: 1, precision: 0 },
      grid: { color: 'rgba(100,116,139,0.12)' },
    },
  },
}

const DOUGHNUT_OPTIONS = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom',
      labels: { color: '#94a3b8', font: { size: 11 }, padding: 12 },
    },
    tooltip: { mode: 'index', intersect: false },
  },
  cutout: '68%',
}

export default function AdminDashboard() {
  const [summary, setSummary] = useState(null)
  const [deployments, setDeployments] = useState([])
  const [technicians, setTechnicians] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  async function loadData() {
    try {
      setLoading(true)
      const [sumRes, depsRes, techsRes] = await Promise.all([
        dashboardApi.getSummary().catch(() => null),
        deploymentsApi.getDeployments().catch(() => []),
        usersApi.getTechnicians().catch(() => []),
      ])

      setSummary(sumRes)
      setDeployments(depsRes || [])
      setTechnicians(techsRes || [])
    } catch (err) {
      setError(err.message)
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
        total: summary.totalDeployments ?? 0,
        active: (summary.inProgress ?? 0) + (summary.domainJoined ?? 0) + (summary.postDomainPending ?? 0),
        completed: summary.completed ?? 0,
        warnings: summary.completedWithWarnings ?? 0,
        failed: summary.failed ?? 0,
        nonCompliant: summary.nonCompliant ?? 0,
        assignedTechs: summary.assignedTechnicians ?? technicians.length,
        activeTechs: summary.activeTechnicians ?? 0,
        avgTime: summary.averageDeploymentTimeMinutes ?? 0,
        lastFailed: summary.lastFailedDeployment,
        lastWarning: summary.lastSecurityWarning,
      }
    }
    return {
      total: deployments.length,
      active: deployments.filter((x) => ['IN_PROGRESS', 'DOMAIN_JOINED', 'POST_DOMAIN_PENDING'].includes(x.deploymentStatus)).length,
      completed: deployments.filter((x) => x.deploymentStatus === 'COMPLETED').length,
      warnings: deployments.filter((x) => x.deploymentStatus === 'COMPLETED_WITH_WARNINGS').length,
      failed: deployments.filter((x) => x.deploymentStatus === 'FAILED').length,
      nonCompliant: deployments.filter((x) => x.complianceStatus === 'NON_COMPLIANT').length,
      assignedTechs: technicians.length,
      activeTechs: 0,
      avgTime: 0,
      lastFailed: null,
      lastWarning: null,
    }
  }, [summary, deployments, technicians])

  const chartData = useMemo(() => {
    const unassignedCount = deployments.filter((d) => !d.technicianId && !d.technicianName).length
    const techGroups = technicians.map((t) => ({
      id: t.id,
      name: t.name || t.username,
      username: t.username,
      matcher: (d) =>
        (d.technicianId != null && d.technicianId === t.id) ||
        (d.technicianName && (d.technicianName === t.name || d.technicianName === t.username)) ||
        (d.technicianUsername && (d.technicianUsername === t.username || d.technicianUsername === t.name)),
    }))

    if (unassignedCount > 0 || technicians.length === 0) {
      techGroups.push({
        id: 'unassigned',
        name: 'Sin asignar',
        username: 'unassigned',
        matcher: (d) => !d.technicianId && !d.technicianName && !d.technicianUsername,
      })
    }

    const labels = techGroups.map((t) => t.name)
    const byTech = (tech, statusList) => {
      const statuses = Array.isArray(statusList) ? statusList : [statusList]
      return deployments.filter((d) => tech.matcher(d) && statuses.includes(d.deploymentStatus)).length
    }

    return {
      labels,
      datasets: [
        {
          label: 'Completado',
          data: techGroups.map((t) => byTech(t, 'COMPLETED')),
          backgroundColor: 'rgba(34,197,94,0.75)',
          borderRadius: 4,
        },
        {
          label: 'Con advertencias',
          data: techGroups.map((t) => byTech(t, 'COMPLETED_WITH_WARNINGS')),
          backgroundColor: 'rgba(234,179,8,0.75)',
          borderRadius: 4,
        },
        {
          label: 'En progreso',
          data: techGroups.map((t) => byTech(t, ['IN_PROGRESS', 'DOMAIN_JOINED', 'POST_DOMAIN_PENDING'])),
          backgroundColor: 'rgba(59,130,246,0.75)',
          borderRadius: 4,
        },
        {
          label: 'Fallido',
          data: techGroups.map((t) => byTech(t, 'FAILED')),
          backgroundColor: 'rgba(239,68,68,0.75)',
          borderRadius: 4,
        },
      ],
    }
  }, [technicians, deployments])

  const doughnutData = useMemo(() => {
    const compliant = deployments.filter((d) => d.complianceStatus === 'COMPLIANT').length
    const nonCompliant = deployments.filter((d) => d.complianceStatus === 'NON_COMPLIANT').length
    const pending = deployments.filter((d) => !d.complianceStatus || d.complianceStatus === 'PENDING').length

    return {
      labels: ['Conforme', 'No conforme', 'Pendiente'],
      datasets: [
        {
          data: [compliant, nonCompliant, pending],
          backgroundColor: ['rgba(34,197,94,0.8)', 'rgba(249,115,22,0.8)', 'rgba(59,130,246,0.8)'],
          borderColor: '#1e293b',
          borderWidth: 2,
        },
      ],
    }
  }, [deployments])

  const recent = useMemo(() => {
    return [...deployments]
      .sort((a, b) => new Date(b.lastSeenAt || b.startedAt || 0) - new Date(a.lastSeenAt || a.startedAt || 0))
      .slice(0, 5)
  }, [deployments])

  if (loading) {
    return (
      <div className="flex items-center justify-center p-12">
        <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Metric Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        <StatCard title="Total" value={stats.total} icon={Server} color="blue" />
        <StatCard title="Activos" value={stats.active} icon={Clock} color="blue" />
        <StatCard title="Completados" value={stats.completed} icon={CheckCircle2} color="green" />
        <StatCard title="Advertencias" value={stats.warnings} icon={AlertTriangle} color="yellow" />
        <StatCard title="Fallidos" value={stats.failed} icon={XCircle} color="red" />
        <StatCard title="No conformes" value={stats.nonCompliant} icon={ShieldAlert} color="orange" />
      </div>

      {/* Advanced metrics bar */}
      {stats.avgTime > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 bg-slate-900/40 border border-slate-800 p-4 rounded-xl">
          <div className="flex items-center gap-3">
            <Cpu className="w-5 h-5 text-cyan-400" />
            <div>
              <p className="text-slate-400 text-xs">Tiempo medio despliegue</p>
              <p className="text-white text-sm font-semibold">{stats.avgTime} min</p>
            </div>
          </div>
          {stats.lastFailed && (
            <div className="flex items-center gap-3">
              <XCircle className="w-5 h-5 text-red-400" />
              <div>
                <p className="text-slate-400 text-xs">Último fallo registrado</p>
                <p className="text-white text-sm font-semibold">{stats.lastFailed}</p>
              </div>
            </div>
          )}
          {stats.lastWarning && (
            <div className="flex items-center gap-3">
              <AlertTriangle className="w-5 h-5 text-amber-400" />
              <div>
                <p className="text-slate-400 text-xs">Alerta de seguridad</p>
                <p className="text-white text-sm font-semibold truncate max-w-xs">{stats.lastWarning}</p>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Chart + Technician summary */}
      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        {/* Bar chart */}
        <div className="xl:col-span-2 bg-slate-800/60 border border-slate-700/50 rounded-2xl p-5">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-white font-semibold text-sm">Despliegues por técnico</h2>
              <p className="text-slate-400 text-xs mt-0.5">Distribución de estados y cargas de trabajo</p>
            </div>
            <span className="text-xs text-slate-400 bg-slate-900/80 px-2.5 py-1 rounded-lg border border-slate-700/60">
              {deployments.length} equipos totales
            </span>
          </div>
          <div className="h-60">
            <Bar data={chartData} options={CHART_OPTIONS} />
          </div>
        </div>

        {/* Compliance Doughnut chart */}
        <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl p-5 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <PieChart className="w-4 h-4 text-emerald-400" />
                <h2 className="text-white font-semibold text-sm">Conformidad de Seguridad</h2>
              </div>
              <span className="text-xs text-emerald-400 font-medium">
                {deployments.length > 0
                  ? Math.round((deployments.filter((d) => d.complianceStatus === 'COMPLIANT').length / deployments.length) * 100)
                  : 0}% OK
              </span>
            </div>
            <p className="text-slate-400 text-xs mb-3">Nivel de cumplimiento global de la flota</p>
          </div>
          <div className="h-48 relative flex items-center justify-center">
            <Doughnut data={doughnutData} options={DOUGHNUT_OPTIONS} />
          </div>
        </div>
      </div>

      {/* Technicians & Recent deployments */}
      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        {/* Technician cards */}
        <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl p-5">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Users className="w-4 h-4 text-blue-400" />
              <h2 className="text-white font-semibold text-sm">Equipo Técnico</h2>
            </div>
            <span className="text-xs text-slate-400 bg-slate-900 px-2 py-0.5 rounded-md border border-slate-700">
              {technicians.length} activos
            </span>
          </div>
          <div className="space-y-3 max-h-80 overflow-y-auto">
            {technicians.length === 0 ? (
              <p className="text-slate-500 text-xs">No hay técnicos dados de alta.</p>
            ) : (
              technicians.map((tech) => {
                const myDeps = deployments.filter((d) =>
                  (d.technicianId != null && d.technicianId === tech.id) ||
                  (d.technicianName && (d.technicianName === tech.name || d.technicianName === tech.username)) ||
                  (d.technicianUsername && (d.technicianUsername === tech.username || d.technicianUsername === tech.name))
                )
                const myCompleted = myDeps.filter((d) => ['COMPLETED', 'COMPLETED_WITH_WARNINGS'].includes(d.deploymentStatus)).length
                const myFailed = myDeps.filter((d) => d.deploymentStatus === 'FAILED' || d.complianceStatus === 'NON_COMPLIANT').length
                return (
                  <div key={tech.id} className="flex items-center gap-3 p-3 bg-slate-900/60 rounded-xl border border-slate-800">
                    <div className="w-9 h-9 rounded-full bg-blue-500/20 flex items-center justify-center text-blue-400 text-xs font-bold shrink-0">
                      {tech.initials || (tech.name ? tech.name.substring(0, 2).toUpperCase() : tech.username.substring(0, 2).toUpperCase())}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-white text-sm font-medium truncate">{tech.name}</p>
                      <p className="text-slate-400 text-xs">{myDeps.length} asignados · <span className="text-emerald-400">{myCompleted} OK</span> · <span className="text-amber-400">{myFailed} incidencias</span></p>
                    </div>
                  </div>
                )
              })
            )}
          </div>
        </div>

        {/* Recent deployments */}
        <div className="xl:col-span-2 bg-slate-800/60 border border-slate-700/50 rounded-2xl">
          <div className="p-5 border-b border-slate-700/50 flex items-center justify-between">
            <div>
              <h2 className="text-white font-semibold text-sm">Actividad reciente</h2>
              <p className="text-slate-500 text-xs mt-0.5">Últimos despliegues en plataforma</p>
            </div>
          </div>
          <DeploymentTable deployments={recent} showTechnician onRefresh={loadData} />
        </div>
      </div>
    </div>
  )
}

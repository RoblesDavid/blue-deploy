import { useMemo } from 'react'
import { Server, CheckCircle2, XCircle, AlertTriangle, Clock, Users } from 'lucide-react'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend,
} from 'chart.js'
import { Bar } from 'react-chartjs-2'
import StatCard from '../../components/common/StatCard'
import DeploymentTable from '../../components/deployments/DeploymentTable'
import { MOCK_DEPLOYMENTS } from '../../data/mockDeployments'
import { MOCK_USERS } from '../../data/mockUsers'

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend)

const CHART_OPTIONS = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'top',
      labels: { color: '#94a3b8', font: { size: 11 }, padding: 16 },
    },
    tooltip: { mode: 'index', intersect: false },
  },
  scales: {
    x: {
      stacked: true,
      ticks: { color: '#64748b' },
      grid: { color: 'rgba(100,116,139,0.12)' },
    },
    y: {
      stacked: true,
      beginAtZero: true,
      ticks: { color: '#64748b', stepSize: 1 },
      grid: { color: 'rgba(100,116,139,0.12)' },
    },
  },
}

export default function AdminDashboard() {
  const stats = useMemo(() => {
    const d = MOCK_DEPLOYMENTS
    return {
      total: d.length,
      active: d.filter((x) => ['IN_PROGRESS', 'DOMAIN_JOINED', 'POST_DOMAIN_PENDING'].includes(x.deploymentStatus)).length,
      completed: d.filter((x) => x.deploymentStatus === 'COMPLETED').length,
      warnings: d.filter((x) => x.deploymentStatus === 'COMPLETED_WITH_WARNINGS').length,
      failed: d.filter((x) => x.deploymentStatus === 'FAILED').length,
      nonCompliant: d.filter((x) => x.deploymentStatus === 'NON_COMPLIANT').length,
    }
  }, [])

  const technicians = MOCK_USERS.filter((u) => u.role === 'TECHNICIAN')

  const chartData = useMemo(() => {
    const labels = technicians.map((t) => t.name)
    const byTech = (techId, status) =>
      MOCK_DEPLOYMENTS.filter((d) => d.technicianId === techId && d.deploymentStatus === status).length

    return {
      labels,
      datasets: [
        {
          label: 'Completado',
          data: technicians.map((t) => byTech(t.username, 'COMPLETED')),
          backgroundColor: 'rgba(34,197,94,0.7)',
          borderRadius: 4,
        },
        {
          label: 'Con advertencias',
          data: technicians.map((t) => byTech(t.username, 'COMPLETED_WITH_WARNINGS')),
          backgroundColor: 'rgba(234,179,8,0.7)',
          borderRadius: 4,
        },
        {
          label: 'En progreso',
          data: technicians.map((t) => byTech(t.username, 'IN_PROGRESS') + byTech(t.username, 'POST_DOMAIN_PENDING') + byTech(t.username, 'DOMAIN_JOINED')),
          backgroundColor: 'rgba(59,130,246,0.7)',
          borderRadius: 4,
        },
        {
          label: 'Fallido',
          data: technicians.map((t) => byTech(t.username, 'FAILED')),
          backgroundColor: 'rgba(239,68,68,0.7)',
          borderRadius: 4,
        },
        {
          label: 'No conforme',
          data: technicians.map((t) => byTech(t.username, 'NON_COMPLIANT')),
          backgroundColor: 'rgba(249,115,22,0.7)',
          borderRadius: 4,
        },
      ],
    }
  }, [technicians])

  const recent = MOCK_DEPLOYMENTS.slice().sort((a, b) => new Date(b.lastSeenAt) - new Date(a.lastSeenAt)).slice(0, 5)

  return (
    <div className="space-y-6">
      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        <StatCard title="Total" value={stats.total} icon={Server} color="blue" />
        <StatCard title="Activos" value={stats.active} icon={Clock} color="blue" />
        <StatCard title="Completados" value={stats.completed} icon={CheckCircle2} color="green" />
        <StatCard title="Advertencias" value={stats.warnings} icon={AlertTriangle} color="yellow" />
        <StatCard title="Fallidos" value={stats.failed} icon={XCircle} color="red" />
        <StatCard title="No conformes" value={stats.nonCompliant} icon={XCircle} color="orange" />
      </div>

      {/* Chart + Technician summary */}
      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        {/* Bar chart */}
        <div className="xl:col-span-2 bg-slate-800/60 border border-slate-700/50 rounded-2xl p-5">
          <h2 className="text-white font-semibold text-sm mb-4">Despliegues por técnico</h2>
          <div className="h-52">
            <Bar data={chartData} options={CHART_OPTIONS} />
          </div>
        </div>

        {/* Technician cards */}
        <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl p-5">
          <div className="flex items-center gap-2 mb-4">
            <Users className="w-4 h-4 text-blue-400" />
            <h2 className="text-white font-semibold text-sm">Técnicos activos</h2>
          </div>
          <div className="space-y-3">
            {technicians.map((tech) => {
              const myDeps = MOCK_DEPLOYMENTS.filter((d) => d.technicianId === tech.username)
              const myCompleted = myDeps.filter((d) => ['COMPLETED', 'COMPLETED_WITH_WARNINGS'].includes(d.deploymentStatus)).length
              const myFailed = myDeps.filter((d) => ['FAILED', 'NON_COMPLIANT'].includes(d.deploymentStatus)).length
              return (
                <div key={tech.id} className="flex items-center gap-3 p-3 bg-slate-900/60 rounded-xl">
                  <div className="w-8 h-8 rounded-full bg-blue-500/20 flex items-center justify-center text-blue-400 text-xs font-bold shrink-0">
                    {tech.initials}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-white text-sm font-medium truncate">{tech.name}</p>
                    <p className="text-slate-500 text-xs">{myDeps.length} despliegues · {myCompleted} OK · {myFailed} errores</p>
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      </div>

      {/* Recent deployments */}
      <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl">
        <div className="p-5 border-b border-slate-700/50">
          <h2 className="text-white font-semibold text-sm">Actividad reciente</h2>
          <p className="text-slate-500 text-xs mt-0.5">Últimos 5 despliegues actualizados</p>
        </div>
        <DeploymentTable deployments={recent} showTechnician />
      </div>
    </div>
  )
}

import { useState, useEffect, useMemo } from 'react'
import { Search, RefreshCw, UserCheck, Inbox } from 'lucide-react'
import DeploymentTable from '../../components/deployments/DeploymentTable'
import deploymentsApi from '../../api/deploymentsApi'

const STATUS_OPTIONS = [
  { value: '', label: 'Todos los estados' },
  { value: 'IN_PROGRESS', label: 'En progreso' },
  { value: 'DOMAIN_JOINED', label: 'Dominio unido' },
  { value: 'POST_DOMAIN_PENDING', label: 'Post-dominio' },
  { value: 'COMPLETED', label: 'Completado' },
  { value: 'COMPLETED_WITH_WARNINGS', label: 'Con advertencias' },
  { value: 'FAILED', label: 'Fallido' },
  { value: 'NON_COMPLIANT', label: 'No conforme' },
]

export default function MyDeployments() {
  const [activeTab, setActiveTab] = useState('my') // 'my' | 'unassigned'
  const [myDeployments, setMyDeployments] = useState([])
  const [unassignedDeployments, setUnassignedDeployments] = useState([])
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [loading, setLoading] = useState(true)

  async function loadData() {
    try {
      setLoading(true)
      const [myData, unassignedData] = await Promise.all([
        deploymentsApi.getMyDeployments().catch(() => []),
        deploymentsApi.getUnassignedDeployments().catch(() => []),
      ])
      setMyDeployments(myData || [])
      setUnassignedDeployments(unassignedData || [])
    } catch (err) {
      console.error('Error loading deployments:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const currentList = activeTab === 'my' ? myDeployments : unassignedDeployments

  const filtered = useMemo(() => {
    return currentList.filter((d) => {
      const matchSearch =
        !search ||
        (d.hostname && d.hostname.toLowerCase().includes(search.toLowerCase())) ||
        (d.deploymentId && d.deploymentId.toLowerCase().includes(search.toLowerCase())) ||
        (d.deviceModel && d.deviceModel.toLowerCase().includes(search.toLowerCase()))
      const matchStatus = !statusFilter || d.deploymentStatus === statusFilter
      return matchSearch && matchStatus
    }).sort((a, b) => new Date(b.lastSeenAt || b.startedAt || 0) - new Date(a.lastSeenAt || a.startedAt || 0))
  }, [currentList, search, statusFilter])

  return (
    <div className="space-y-5">
      {/* Tabs */}
      <div className="flex border-b border-slate-700/80 gap-2">
        <button
          onClick={() => setActiveTab('my')}
          className={`flex items-center gap-2 pb-3 px-4 text-sm font-medium border-b-2 transition-all ${
            activeTab === 'my'
              ? 'border-blue-500 text-blue-400 font-semibold'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <Inbox className="w-4 h-4" />
          <span>Mis despliegues</span>
          <span className="text-xs px-2 py-0.5 rounded-full bg-slate-800 text-slate-300">
            {myDeployments.length}
          </span>
        </button>

        <button
          onClick={() => setActiveTab('unassigned')}
          className={`flex items-center gap-2 pb-3 px-4 text-sm font-medium border-b-2 transition-all ${
            activeTab === 'unassigned'
              ? 'border-blue-500 text-blue-400 font-semibold'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <UserCheck className="w-4 h-4" />
          <span>Despliegues sin asignar</span>
          {unassignedDeployments.length > 0 && (
            <span className="text-xs px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/30 font-semibold">
              {unassignedDeployments.length} disponibles
            </span>
          )}
        </button>
      </div>

      {/* Unassigned banner */}
      {activeTab === 'unassigned' && (
        <div className="p-4 bg-amber-500/10 border border-amber-500/20 rounded-xl text-amber-200 text-xs flex items-center justify-between gap-4">
          <div>
            <p className="font-semibold text-sm text-amber-100">Equipos huérfanos creados por OSDCloud</p>
            <p className="text-slate-300 text-xs mt-0.5">
              Haz clic en el icono del ojo para ver el detalle y pulsa <strong>"Asignarme este despliegue"</strong> para transferirlo a tu bandeja personal.
            </p>
          </div>
        </div>
      )}

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por host, ID o dispositivo..."
            className="w-full bg-slate-800 border border-slate-700 rounded-xl pl-10 pr-4 py-2.5 text-white text-sm placeholder-slate-600 focus:outline-none focus:border-blue-500 transition-all"
          />
        </div>

        <div className="flex gap-2">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-slate-800 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-slate-300 focus:outline-none focus:border-blue-500 transition-all appearance-none cursor-pointer shrink-0"
          >
            {STATUS_OPTIONS.map((o) => (
              <option key={o.value} value={o.value}>{o.label}</option>
            ))}
          </select>

          <button
            onClick={loadData}
            title="Recargar"
            className="p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-slate-300 hover:text-white hover:bg-slate-700 transition-all"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Table */}
      <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl">
        <div className="px-5 py-4 border-b border-slate-700/50 flex items-center justify-between">
          <h2 className="text-white font-semibold text-sm">
            {activeTab === 'my' ? 'Mis despliegues asignados' : 'Despliegues sin técnico asignado'}
          </h2>
          <span className="text-slate-500 text-xs bg-slate-700/50 px-2.5 py-1 rounded-full">
            {filtered.length} resultados
          </span>
        </div>
        {loading && currentList.length === 0 ? (
          <div className="flex items-center justify-center p-12">
            <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
          </div>
        ) : (
          <DeploymentTable
            deployments={filtered}
            showTechnician={activeTab === 'unassigned'}
            onRefresh={loadData}
          />
        )}
      </div>
    </div>
  )
}

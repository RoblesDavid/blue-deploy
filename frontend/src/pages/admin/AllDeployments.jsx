import { useState, useEffect, useMemo } from 'react'
import { Search, Filter, RefreshCw } from 'lucide-react'
import DeploymentTable from '../../components/deployments/DeploymentTable'
import deploymentsApi from '../../api/deploymentsApi'
import usersApi from '../../api/usersApi'

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

export default function AllDeployments() {
  const [deployments, setDeployments] = useState([])
  const [technicians, setTechnicians] = useState([])
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [techFilter, setTechFilter] = useState('')
  const [loading, setLoading] = useState(true)

  async function loadData() {
    try {
      setLoading(true)
      const [depsRes, techsRes] = await Promise.all([
        deploymentsApi.getDeployments().catch(() => []),
        usersApi.getTechnicians().catch(() => []),
      ])
      setDeployments(depsRes || [])
      setTechnicians(techsRes || [])
    } catch (err) {
      console.error('Error loading deployments:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const filtered = useMemo(() => {
    return deployments.filter((d) => {
      const matchSearch =
        !search ||
        (d.hostname && d.hostname.toLowerCase().includes(search.toLowerCase())) ||
        (d.deploymentId && d.deploymentId.toLowerCase().includes(search.toLowerCase())) ||
        (d.deviceModel && d.deviceModel.toLowerCase().includes(search.toLowerCase()))
      const matchStatus = !statusFilter || d.deploymentStatus === statusFilter
      const matchTech = !techFilter || String(d.technicianId) === String(techFilter) || d.technicianUsername === techFilter
      return matchSearch && matchStatus && matchTech
    }).sort((a, b) => new Date(b.lastSeenAt || b.startedAt || 0) - new Date(a.lastSeenAt || a.startedAt || 0))
  }, [deployments, search, statusFilter, techFilter])

  return (
    <div className="space-y-5">
      {/* Filters and Actions */}
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

        <div className="flex gap-3 shrink-0">
          <div className="relative">
            <Filter className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slate-500" />
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="bg-slate-800 border border-slate-700 rounded-xl pl-8 pr-4 py-2.5 text-sm text-slate-300 focus:outline-none focus:border-blue-500 transition-all appearance-none cursor-pointer"
            >
              {STATUS_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
          </div>

          <select
            value={techFilter}
            onChange={(e) => setTechFilter(e.target.value)}
            className="bg-slate-800 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-slate-300 focus:outline-none focus:border-blue-500 transition-all appearance-none cursor-pointer"
          >
            <option value="">Todos los técnicos</option>
            {technicians.map((t) => (
              <option key={t.id || t.username} value={t.id || t.username}>{t.name}</option>
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

      {/* Results Table */}
      <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl">
        <div className="px-5 py-4 border-b border-slate-700/50 flex items-center justify-between">
          <h2 className="text-white font-semibold text-sm">Inventario de Despliegues</h2>
          <span className="text-slate-500 text-xs bg-slate-700/50 px-2.5 py-1 rounded-full">
            {filtered.length} resultados
          </span>
        </div>
        {loading && deployments.length === 0 ? (
          <div className="flex items-center justify-center p-12">
            <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
          </div>
        ) : (
          <DeploymentTable deployments={filtered} showTechnician onRefresh={loadData} />
        )}
      </div>
    </div>
  )
}

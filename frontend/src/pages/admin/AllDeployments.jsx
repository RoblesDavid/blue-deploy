import { useState } from 'react'
import { Search, Filter } from 'lucide-react'
import DeploymentTable from '../../components/deployments/DeploymentTable'
import { MOCK_DEPLOYMENTS } from '../../data/mockDeployments'
import { MOCK_USERS } from '../../data/mockUsers'

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
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [techFilter, setTechFilter] = useState('')

  const technicians = MOCK_USERS.filter((u) => u.role === 'TECHNICIAN')

  const filtered = MOCK_DEPLOYMENTS.filter((d) => {
    const matchSearch =
      !search ||
      d.hostname.toLowerCase().includes(search.toLowerCase()) ||
      d.deploymentId.toLowerCase().includes(search.toLowerCase()) ||
      d.deviceModel.toLowerCase().includes(search.toLowerCase())
    const matchStatus = !statusFilter || d.deploymentStatus === statusFilter
    const matchTech = !techFilter || d.technicianId === techFilter
    return matchSearch && matchStatus && matchTech
  }).sort((a, b) => new Date(b.lastSeenAt) - new Date(a.lastSeenAt))

  return (
    <div className="space-y-5">
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
              <option key={t.username} value={t.username}>{t.name}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Results */}
      <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl">
        <div className="px-5 py-4 border-b border-slate-700/50 flex items-center justify-between">
          <h2 className="text-white font-semibold text-sm">Despliegues</h2>
          <span className="text-slate-500 text-xs bg-slate-700/50 px-2.5 py-1 rounded-full">
            {filtered.length} resultados
          </span>
        </div>
        <DeploymentTable deployments={filtered} showTechnician />
      </div>
    </div>
  )
}

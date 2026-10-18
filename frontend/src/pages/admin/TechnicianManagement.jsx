import { useState, useEffect } from 'react'
import { UserPlus, Server, CheckCircle2, XCircle, Mail, Building2, RefreshCw } from 'lucide-react'
import CreateTechnicianModal from '../../components/modals/CreateTechnicianModal'
import usersApi from '../../api/usersApi'
import deploymentsApi from '../../api/deploymentsApi'

function TechnicianCard({ tech, deployments = [] }) {
  const deps = deployments.filter((d) => d.technicianUsername === tech.username || d.technicianId === tech.id)
  const completed = deps.filter((d) => ['COMPLETED', 'COMPLETED_WITH_WARNINGS'].includes(d.deploymentStatus)).length
  const active = deps.filter((d) => ['IN_PROGRESS', 'DOMAIN_JOINED', 'POST_DOMAIN_PENDING'].includes(d.deploymentStatus)).length
  const failed = deps.filter((d) => ['FAILED', 'NON_COMPLIANT'].includes(d.deploymentStatus)).length

  return (
    <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl p-5 hover:border-slate-600 transition-colors">
      <div className="flex items-start gap-4">
        <div className="w-12 h-12 rounded-2xl bg-blue-500/20 flex items-center justify-center text-blue-400 font-bold text-lg shrink-0">
          {tech.initials || tech.username.substring(0, 2).toUpperCase()}
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2">
            <h3 className="text-white font-semibold text-base">{tech.name}</h3>
            {!tech.active && (
              <span className="px-2 py-0.5 text-xs bg-red-500/20 text-red-400 border border-red-500/30 rounded-full">
                Inactivo
              </span>
            )}
          </div>
          <p className="text-slate-500 text-xs font-mono mt-0.5">@{tech.username}</p>

          <div className="flex flex-wrap gap-3 mt-3 text-xs text-slate-400">
            <span className="flex items-center gap-1.5">
              <Mail className="w-3 h-3 text-slate-600" />
              {tech.email}
            </span>
            <span className="flex items-center gap-1.5">
              <Building2 className="w-3 h-3 text-slate-600" />
              {tech.department || 'Operaciones'}
            </span>
          </div>
        </div>
      </div>

      {/* Stats row */}
      <div className="grid grid-cols-4 gap-2 mt-5 pt-4 border-t border-slate-700/50">
        {[
          { label: 'Total', value: deps.length, icon: Server, cls: 'text-slate-400' },
          { label: 'Activos', value: active, icon: Server, cls: 'text-blue-400' },
          { label: 'Completados', value: completed, icon: CheckCircle2, cls: 'text-green-400' },
          { label: 'Errores', value: failed, icon: XCircle, cls: 'text-red-400' },
        ].map(({ label, value, icon: Icon, cls }) => (
          <div key={label} className="text-center">
            <Icon className={`w-4 h-4 mx-auto mb-1 ${cls}`} />
            <p className="text-white text-lg font-bold leading-none">{value}</p>
            <p className="text-slate-600 text-xs mt-0.5">{label}</p>
          </div>
        ))}
      </div>
    </div>
  )
}

export default function TechnicianManagement() {
  const [showModal, setShowModal] = useState(false)
  const [technicians, setTechnicians] = useState([])
  const [deployments, setDeployments] = useState([])
  const [loading, setLoading] = useState(true)

  async function loadData() {
    try {
      setLoading(true)
      const [techsRes, depsRes] = await Promise.all([
        usersApi.getTechnicians().catch(() => []),
        deploymentsApi.getDeployments().catch(() => []),
      ])
      setTechnicians(techsRes || [])
      setDeployments(depsRes || [])
    } catch (err) {
      console.error('Error loading technicians:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  return (
    <>
      <div className="space-y-5">
        {/* Header action */}
        <div className="flex items-center justify-between">
          <div>
            <p className="text-slate-400 text-sm">{technicians.length} técnico(s) registrado(s)</p>
          </div>
          <div className="flex gap-2">
            <button
              onClick={loadData}
              title="Recargar"
              className="p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-slate-300 hover:text-white hover:bg-slate-700 transition-all"
            >
              <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            </button>
            <button
              onClick={() => setShowModal(true)}
              className="flex items-center gap-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-500 text-white text-sm font-medium rounded-xl transition-all shadow-lg shadow-blue-900/30"
            >
              <UserPlus className="w-4 h-4" />
              Nuevo técnico
            </button>
          </div>
        </div>

        {/* Technician grid */}
        {loading && technicians.length === 0 ? (
          <div className="flex items-center justify-center p-12">
            <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            {technicians.length === 0 ? (
              <div className="lg:col-span-2 p-8 bg-slate-800/40 rounded-2xl text-center text-slate-500 text-sm">
                No hay técnicos registrados. Haz clic en "Nuevo técnico" para dar de alta uno.
              </div>
            ) : (
              technicians.map((tech) => (
                <TechnicianCard key={tech.id} tech={tech} deployments={deployments} />
              ))
            )}
          </div>
        )}
      </div>

      {showModal && (
        <CreateTechnicianModal
          onClose={() => setShowModal(false)}
          onSuccess={loadData}
        />
      )}
    </>
  )
}

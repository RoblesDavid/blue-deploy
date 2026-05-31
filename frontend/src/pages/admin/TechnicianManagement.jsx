import { useState } from 'react'
import { UserPlus, Server, CheckCircle2, XCircle, Mail, Building2 } from 'lucide-react'
import CreateTechnicianModal from '../../components/modals/CreateTechnicianModal'
import { MOCK_USERS } from '../../data/mockUsers'
import { MOCK_DEPLOYMENTS } from '../../data/mockDeployments'

function TechnicianCard({ tech }) {
  const deps = MOCK_DEPLOYMENTS.filter((d) => d.technicianId === tech.username)
  const completed = deps.filter((d) => ['COMPLETED', 'COMPLETED_WITH_WARNINGS'].includes(d.deploymentStatus)).length
  const active = deps.filter((d) => ['IN_PROGRESS', 'DOMAIN_JOINED', 'POST_DOMAIN_PENDING'].includes(d.deploymentStatus)).length
  const failed = deps.filter((d) => ['FAILED', 'NON_COMPLIANT'].includes(d.deploymentStatus)).length

  return (
    <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl p-5 hover:border-slate-600 transition-colors">
      <div className="flex items-start gap-4">
        <div className="w-12 h-12 rounded-2xl bg-blue-500/20 flex items-center justify-center text-blue-400 font-bold text-lg shrink-0">
          {tech.initials}
        </div>
        <div className="flex-1 min-w-0">
          <h3 className="text-white font-semibold text-base">{tech.name}</h3>
          <p className="text-slate-500 text-xs font-mono mt-0.5">@{tech.username}</p>

          <div className="flex flex-wrap gap-3 mt-3 text-xs text-slate-400">
            <span className="flex items-center gap-1.5">
              <Mail className="w-3 h-3 text-slate-600" />
              {tech.email}
            </span>
            <span className="flex items-center gap-1.5">
              <Building2 className="w-3 h-3 text-slate-600" />
              {tech.department}
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
  const technicians = MOCK_USERS.filter((u) => u.role === 'TECHNICIAN')

  return (
    <>
      <div className="space-y-5">
        {/* Header action */}
        <div className="flex items-center justify-between">
          <div>
            <p className="text-slate-400 text-sm">{technicians.length} técnico(s) registrado(s)</p>
          </div>
          <button
            onClick={() => setShowModal(true)}
            className="flex items-center gap-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-500 text-white text-sm font-medium rounded-xl transition-all shadow-lg shadow-blue-900/30"
          >
            <UserPlus className="w-4 h-4" />
            Nuevo técnico
          </button>
        </div>

        {/* Technician grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {technicians.map((tech) => (
            <TechnicianCard key={tech.id} tech={tech} />
          ))}
        </div>
      </div>

      {showModal && <CreateTechnicianModal onClose={() => setShowModal(false)} />}
    </>
  )
}

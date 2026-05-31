import { useState } from 'react'
import { Eye } from 'lucide-react'
import Badge from '../common/Badge'
import DeploymentDetail from './DeploymentDetail'

function fmt(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('es-ES', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

export default function DeploymentTable({ deployments, showTechnician = false }) {
  const [selected, setSelected] = useState(null)

  if (!deployments.length) {
    return (
      <div className="py-16 text-center text-slate-500 text-sm">
        No hay despliegues que mostrar.
      </div>
    )
  }

  return (
    <>
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-700">
              {['Host', 'Dispositivo', ...(showTechnician ? ['Técnico'] : []), 'Estado', 'Compliance', 'Última actividad', ''].map((h) => (
                <th key={h} className="text-left py-3 px-4 text-slate-500 text-xs font-semibold uppercase tracking-wider whitespace-nowrap">
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {deployments.map((dep) => (
              <tr
                key={dep.deploymentId}
                className="border-b border-slate-800 hover:bg-slate-800/40 transition-colors"
              >
                <td className="py-3 px-4">
                  <p className="text-white font-medium">{dep.hostname}</p>
                  <p className="text-slate-600 text-xs font-mono mt-0.5">{dep.deploymentId}</p>
                </td>
                <td className="py-3 px-4 text-slate-300 whitespace-nowrap">{dep.deviceModel}</td>
                {showTechnician && (
                  <td className="py-3 px-4 text-slate-300 whitespace-nowrap">{dep.technicianName}</td>
                )}
                <td className="py-3 px-4">
                  <Badge status={dep.deploymentStatus} />
                </td>
                <td className="py-3 px-4">
                  <Badge status={dep.complianceStatus} />
                </td>
                <td className="py-3 px-4 text-slate-400 whitespace-nowrap">{fmt(dep.lastSeenAt)}</td>
                <td className="py-3 px-4">
                  <button
                    onClick={() => setSelected(dep)}
                    className="p-1.5 text-slate-500 hover:text-blue-400 rounded-lg hover:bg-blue-500/10 transition-all"
                    title="Ver detalle"
                  >
                    <Eye className="w-4 h-4" />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selected && (
        <DeploymentDetail deployment={selected} onClose={() => setSelected(null)} />
      )}
    </>
  )
}

import { X, CheckCircle2, XCircle, AlertTriangle, Clock, Monitor, Calendar } from 'lucide-react'
import Badge from '../common/Badge'

function StatusIcon({ status }) {
  if (['SUCCESS', 'COMPLETED', 'PASSED'].includes(status))
    return <CheckCircle2 className="w-4 h-4 text-green-400 shrink-0 mt-0.5" />
  if (['FAILED', 'NON_COMPLIANT'].includes(status))
    return <XCircle className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
  if (['WARNING', 'COMPLETED_WITH_WARNINGS'].includes(status))
    return <AlertTriangle className="w-4 h-4 text-yellow-400 shrink-0 mt-0.5" />
  return <Clock className="w-4 h-4 text-blue-400 shrink-0 mt-0.5" />
}

function fmt(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('es-ES', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

export default function DeploymentDetail({ deployment, onClose }) {
  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      {/* Backdrop */}
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />

      {/* Panel */}
      <div className="relative w-full max-w-lg bg-slate-900 border-l border-slate-700 overflow-y-auto">
        {/* Header */}
        <div className="sticky top-0 bg-slate-900/95 backdrop-blur border-b border-slate-800 p-5 flex items-start justify-between z-10">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <Monitor className="w-4 h-4 text-blue-400" />
              <h2 className="text-white font-semibold text-lg leading-none">{deployment.hostname}</h2>
            </div>
            <p className="text-slate-500 text-xs font-mono">{deployment.deploymentId}</p>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-all"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-5 space-y-6">
          {/* Metadata grid */}
          <div className="grid grid-cols-2 gap-3">
            {[
              { label: 'Dispositivo', value: deployment.deviceModel },
              { label: 'Dominio', value: deployment.domainName },
              { label: 'Técnico', value: deployment.technicianName },
              { label: 'Fase actual', value: deployment.currentStep },
            ].map(({ label, value }) => (
              <div key={label} className="bg-slate-800 rounded-xl p-3">
                <p className="text-slate-500 text-xs mb-1">{label}</p>
                <p className="text-white text-sm font-medium break-all">{value}</p>
              </div>
            ))}
          </div>

          {/* Status badges */}
          <div className="flex items-center gap-3 flex-wrap">
            <div className="flex items-center gap-2">
              <span className="text-slate-500 text-xs">Estado:</span>
              <Badge status={deployment.deploymentStatus} />
            </div>
            <div className="flex items-center gap-2">
              <span className="text-slate-500 text-xs">Compliance:</span>
              <Badge status={deployment.complianceStatus} />
            </div>
          </div>

          {/* Steps */}
          <section>
            <h3 className="text-slate-300 text-sm font-semibold mb-3 flex items-center gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-blue-400 shrink-0" />
              Fases del despliegue
              <span className="text-slate-600 font-normal">({deployment.steps.length})</span>
            </h3>
            <div className="space-y-2">
              {deployment.steps.map((step, i) => (
                <div key={i} className="flex items-start gap-3 p-3 bg-slate-800/80 rounded-xl">
                  <StatusIcon status={step.stepStatus} />
                  <div className="flex-1 min-w-0">
                    <p className="text-white text-sm font-medium leading-none">{step.stepName}</p>
                    {step.message && <p className="text-slate-400 text-xs mt-1">{step.message}</p>}
                    {step.errorCode && (
                      <p className="text-red-400 text-xs mt-1 font-mono">Error: {step.errorCode}</p>
                    )}
                  </div>
                  <Badge status={step.stepStatus} />
                </div>
              ))}
            </div>
          </section>

          {/* Security checks */}
          {deployment.checks.length > 0 && (
            <section>
              <h3 className="text-slate-300 text-sm font-semibold mb-3 flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-purple-400 shrink-0" />
                Verificaciones de seguridad
                <span className="text-slate-600 font-normal">({deployment.checks.length})</span>
              </h3>
              <div className="space-y-2">
                {deployment.checks.map((check, i) => (
                  <div key={i} className="flex items-start gap-3 p-3 bg-slate-800/80 rounded-xl">
                    <StatusIcon status={check.checkStatus} />
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <p className="text-white text-sm font-medium leading-none">{check.checkName}</p>
                        <Badge status={check.severity} />
                      </div>
                      <p className="text-slate-400 text-xs mt-1">{check.message}</p>
                      <p className="text-slate-600 text-xs mt-0.5">
                        Esperado: <span className="text-slate-400">{check.expectedValue}</span>
                        {' · '}
                        Obtenido: <span className="text-slate-400">{check.actualValue}</span>
                      </p>
                    </div>
                    <Badge status={check.checkStatus} />
                  </div>
                ))}
              </div>
            </section>
          )}

          {/* Timestamps */}
          <section className="flex flex-col gap-1.5 pt-2 border-t border-slate-800">
            {[
              { label: 'Iniciado', value: fmt(deployment.startedAt) },
              { label: 'Finalizado', value: fmt(deployment.finishedAt) },
              { label: 'Última actividad', value: fmt(deployment.lastSeenAt) },
            ].map(({ label, value }) => (
              <div key={label} className="flex items-center gap-2">
                <Calendar className="w-3 h-3 text-slate-600 shrink-0" />
                <span className="text-slate-500 text-xs">{label}:</span>
                <span className="text-slate-400 text-xs">{value}</span>
              </div>
            ))}
          </section>
        </div>
      </div>
    </div>
  )
}

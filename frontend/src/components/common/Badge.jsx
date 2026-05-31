const CONFIG = {
  // Deployment status
  COMPLETED:               { label: 'Completado',       cls: 'bg-green-500/15 text-green-400 border-green-500/30' },
  COMPLETED_WITH_WARNINGS: { label: 'Con advertencias', cls: 'bg-yellow-500/15 text-yellow-400 border-yellow-500/30' },
  IN_PROGRESS:             { label: 'En progreso',      cls: 'bg-blue-500/15 text-blue-400 border-blue-500/30' },
  DOMAIN_JOINED:           { label: 'Dominio unido',    cls: 'bg-cyan-500/15 text-cyan-400 border-cyan-500/30' },
  POST_DOMAIN_PENDING:     { label: 'Post-dominio',     cls: 'bg-indigo-500/15 text-indigo-400 border-indigo-500/30' },
  FAILED:                  { label: 'Fallido',          cls: 'bg-red-500/15 text-red-400 border-red-500/30' },
  NON_COMPLIANT:           { label: 'No conforme',      cls: 'bg-orange-500/15 text-orange-400 border-orange-500/30' },
  // Step / check status
  SUCCESS:                 { label: 'Éxito',            cls: 'bg-green-500/15 text-green-400 border-green-500/30' },
  WARNING:                 { label: 'Advertencia',      cls: 'bg-yellow-500/15 text-yellow-400 border-yellow-500/30' },
  RUNNING:                 { label: 'Ejecutando',       cls: 'bg-blue-500/15 text-blue-400 border-blue-500/30' },
  PENDING:                 { label: 'Pendiente',        cls: 'bg-slate-500/15 text-slate-400 border-slate-500/30' },
  SKIPPED:                 { label: 'Omitido',          cls: 'bg-slate-500/15 text-slate-400 border-slate-500/30' },
  PASSED:                  { label: 'Correcto',         cls: 'bg-green-500/15 text-green-400 border-green-500/30' },
  UNKNOWN:                 { label: 'Desconocido',      cls: 'bg-slate-500/15 text-slate-400 border-slate-500/30' },
  // Compliance
  COMPLIANT:               { label: 'Conforme',         cls: 'bg-green-500/15 text-green-400 border-green-500/30' },
  // Severity
  LOW:                     { label: 'Baja',             cls: 'bg-green-500/15 text-green-400 border-green-500/30' },
  MEDIUM:                  { label: 'Media',            cls: 'bg-yellow-500/15 text-yellow-400 border-yellow-500/30' },
  HIGH:                    { label: 'Alta',             cls: 'bg-orange-500/15 text-orange-400 border-orange-500/30' },
  CRITICAL:                { label: 'Crítica',          cls: 'bg-red-500/15 text-red-400 border-red-500/30' },
}

const FALLBACK = { label: '—', cls: 'bg-slate-500/15 text-slate-400 border-slate-500/30' }

export default function Badge({ status, label }) {
  const { label: defaultLabel, cls } = CONFIG[status] ?? FALLBACK
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold border ${cls}`}>
      {label ?? defaultLabel}
    </span>
  )
}

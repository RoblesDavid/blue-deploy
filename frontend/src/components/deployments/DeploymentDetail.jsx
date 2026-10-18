import { useState, useEffect } from 'react'
import { X, CheckCircle2, XCircle, AlertTriangle, Clock, Monitor, Calendar, Download, Sparkles, UserCheck, ShieldAlert, Cpu, Check, Wrench, Edit3, Sliders, Save, RefreshCw } from 'lucide-react'
import Badge from '../common/Badge'
import deploymentsApi from '../../api/deploymentsApi'
import usersApi from '../../api/usersApi'
import { useAuth } from '../../auth/AuthContext'

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

export default function DeploymentDetail({ deployment, onClose, onUpdated }) {
  const { user } = useAuth()
  const isAdmin = user?.role === 'ADMIN'

  const [currentDep, setCurrentDep] = useState(deployment)
  const [steps, setSteps] = useState(deployment?.steps || [])
  const [checks, setChecks] = useState(deployment?.checks || [])
  const [technicians, setTechnicians] = useState([])
  const [assignedTechId, setAssignedTechId] = useState(deployment?.technicianId || '')

  // Manual status edit state
  const [isEditingStatus, setIsEditingStatus] = useState(false)
  const [editDepStatus, setEditDepStatus] = useState(deployment?.deploymentStatus || 'IN_PROGRESS')
  const [editCompStatus, setEditCompStatus] = useState(deployment?.complianceStatus || 'PENDING')
  const [editCurrentStep, setEditCurrentStep] = useState(deployment?.currentStep || 'DEPLOYMENT_CREATED')
  const [editNote, setEditNote] = useState('')
  const [savingStatus, setSavingStatus] = useState(false)

  const [aiSummary, setAiSummary] = useState(null)
  const [aiLoading, setAiLoading] = useState(false)
  const [pdfLoading, setPdfLoading] = useState(false)
  const [reassigning, setReassigning] = useState(false)
  const [claiming, setClaiming] = useState(false)
  const [resolvingCheckId, setResolvingCheckId] = useState(null)
  const [remediationNote, setRemediationNote] = useState('')
  const [resolvingLoading, setResolvingLoading] = useState(false)
  const [error, setError] = useState('')
  const [successMsg, setSuccessMsg] = useState('')

  useEffect(() => {
    async function loadExtraDetails() {
      try {
        const [detail, stepsRes, checksRes] = await Promise.all([
          deploymentsApi.getDeploymentById(deployment.deploymentId).catch(() => null),
          deploymentsApi.getSteps(deployment.deploymentId).catch(() => []),
          deploymentsApi.getChecks(deployment.deploymentId).catch(() => []),
        ])
        if (detail) {
          setCurrentDep(detail)
          setAssignedTechId(detail.technicianId || '')
          setEditDepStatus(detail.deploymentStatus || 'IN_PROGRESS')
          setEditCompStatus(detail.complianceStatus || 'PENDING')
          setEditCurrentStep(detail.currentStep || 'DEPLOYMENT_CREATED')

          if (detail.steps && detail.steps.length > 0) {
            setSteps(detail.steps)
          } else if (stepsRes && stepsRes.length > 0) {
            setSteps(stepsRes)
          } else if (deployment?.steps && deployment.steps.length > 0) {
            setSteps(deployment.steps)
          }

          if (detail.checks && detail.checks.length > 0) {
            setChecks(detail.checks)
          } else if (checksRes && checksRes.length > 0) {
            setChecks(checksRes)
          } else if (deployment?.checks && deployment.checks.length > 0) {
            setChecks(deployment.checks)
          }
        } else {
          if (stepsRes && stepsRes.length > 0) setSteps(stepsRes)
          if (checksRes && checksRes.length > 0) setChecks(checksRes)
        }
        if (isAdmin) {
          const techs = await usersApi.getTechnicians().catch(() => [])
          setTechnicians(techs || [])
        }
      } catch (err) {
        console.error('Error loading detail extras:', err)
      }
    }
    loadExtraDetails()
  }, [deployment, isAdmin])

  async function refreshDetails() {
    try {
      const [detail, stepsRes, checksRes] = await Promise.all([
        deploymentsApi.getDeploymentById(deployment.deploymentId).catch(() => null),
        deploymentsApi.getSteps(deployment.deploymentId).catch(() => []),
        deploymentsApi.getChecks(deployment.deploymentId).catch(() => []),
      ])
      if (detail) {
        setCurrentDep(detail)
        setAssignedTechId(detail.technicianId || '')
        setEditDepStatus(detail.deploymentStatus || 'IN_PROGRESS')
        setEditCompStatus(detail.complianceStatus || 'PENDING')
        setEditCurrentStep(detail.currentStep || 'DEPLOYMENT_CREATED')

        if (detail.steps && detail.steps.length > 0) {
          setSteps(detail.steps)
        } else if (stepsRes && stepsRes.length > 0) {
          setSteps(stepsRes)
        }

        if (detail.checks && detail.checks.length > 0) {
          setChecks(detail.checks)
        } else if (checksRes && checksRes.length > 0) {
          setChecks(checksRes)
        }
      }
      if (onUpdated) onUpdated()
    } catch (err) {
      console.error('Error refreshing details:', err)
    }
  }

  async function handleDownloadPdf() {
    setPdfLoading(true)
    setError('')
    try {
      await deploymentsApi.downloadReportPdf(deployment.deploymentId)
    } catch (err) {
      setError('Error al descargar el PDF: ' + err.message)
    } finally {
      setPdfLoading(false)
    }
  }

  async function handleAiAnalysis() {
    setAiLoading(true)
    setError('')
    try {
      const result = await deploymentsApi.generateAiSummary(deployment.deploymentId)
      setAiSummary(result)
    } catch (err) {
      setError('Error en diagnóstico IA: ' + err.message)
    } finally {
      setAiLoading(false)
    }
  }

  async function handleReassignTech(e) {
    const newTechId = e.target.value
    if (!newTechId || newTechId === String(assignedTechId)) return

    setReassigning(true)
    setError('')
    try {
      await deploymentsApi.assignTechnician(deployment.deploymentId, newTechId)
      setAssignedTechId(newTechId)
      await refreshDetails()
      setSuccessMsg('Técnico asignado con éxito.')
      setTimeout(() => setSuccessMsg(''), 3000)
    } catch (err) {
      setError('Error al reasignar técnico: ' + err.message)
    } finally {
      setReassigning(false)
    }
  }

  async function handleClaim() {
    setClaiming(true)
    setError('')
    try {
      await deploymentsApi.claimDeployment(deployment.deploymentId)
      await refreshDetails()
      setSuccessMsg('¡Te has asignado este despliegue con éxito!')
      setTimeout(() => setSuccessMsg(''), 3000)
    } catch (err) {
      setError('Error al asignarse el despliegue: ' + err.message)
    } finally {
      setClaiming(false)
    }
  }

  async function handleSaveManualStatus(e) {
    e.preventDefault()
    setSavingStatus(true)
    setError('')
    try {
      await deploymentsApi.updateDeploymentStatus(deployment.deploymentId, {
        deploymentStatus: editDepStatus,
        complianceStatus: editCompStatus,
        currentStep: editCurrentStep,
        note: editNote.trim(),
      })
      setIsEditingStatus(false)
      setEditNote('')
      await refreshDetails()
      setSuccessMsg('Estado del despliegue actualizado manualmente con éxito.')
      setTimeout(() => setSuccessMsg(''), 3500)
    } catch (err) {
      setError('Error al actualizar el estado: ' + err.message)
    } finally {
      setSavingStatus(false)
    }
  }

  async function submitResolveCheck(checkId) {
    setResolvingLoading(true)
    setError('')
    try {
      const note = remediationNote.trim() || 'Acción corregida y verificada manualmente por ' + (user?.name || 'técnico')
      await deploymentsApi.resolveCheck(deployment.deploymentId, checkId, note)
      setResolvingCheckId(null)
      setRemediationNote('')
      await refreshDetails()
      setSuccessMsg('Comprobación remediada con éxito y estado actualizado.')
      setTimeout(() => setSuccessMsg(''), 3000)
    } catch (err) {
      setError('Error al resolver comprobación: ' + err.message)
    } finally {
      setResolvingLoading(false)
    }
  }

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
              <h2 className="text-white font-semibold text-lg leading-none">{currentDep.hostname}</h2>
            </div>
            <p className="text-slate-500 text-xs font-mono">{currentDep.deploymentId}</p>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-all"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-5 space-y-6">
          {error && (
            <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-xs flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {successMsg && (
            <div className="p-3 bg-green-500/10 border border-green-500/20 rounded-xl text-green-400 text-xs flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 shrink-0" />
              <span>{successMsg}</span>
            </div>
          )}

          {/* Action buttons (PDF Export & AI Analysis) */}
          <div className="grid grid-cols-2 gap-3">
            <button
              onClick={handleDownloadPdf}
              disabled={pdfLoading}
              className="flex items-center justify-center gap-2 py-2.5 px-4 bg-slate-800 hover:bg-slate-700 text-white rounded-xl text-xs font-semibold border border-slate-700 transition-all disabled:opacity-50"
            >
              {pdfLoading ? (
                <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <Download className="w-3.5 h-3.5 text-blue-400" />
              )}
              {pdfLoading ? 'Generando PDF...' : 'Informe PDF'}
            </button>

            <button
              onClick={handleAiAnalysis}
              disabled={aiLoading}
              className="flex items-center justify-center gap-2 py-2.5 px-4 bg-gradient-to-r from-purple-600/30 to-blue-600/30 hover:from-purple-600/50 hover:to-blue-600/50 text-purple-200 border border-purple-500/30 rounded-xl text-xs font-semibold transition-all disabled:opacity-50"
            >
              {aiLoading ? (
                <span className="w-3.5 h-3.5 border-2 border-purple-300/30 border-t-purple-300 rounded-full animate-spin" />
              ) : (
                <Sparkles className="w-3.5 h-3.5 text-purple-400" />
              )}
              {aiLoading ? 'Analizando...' : 'Diagnóstico IA'}
            </button>
          </div>

          {/* AI Summary Diagnostic Box */}
          {aiSummary && (
            <div className="p-4 bg-purple-950/30 border border-purple-500/30 rounded-2xl space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-purple-300 font-semibold text-xs">
                  <Sparkles className="w-4 h-4 text-purple-400" />
                  <span>Análisis Inteligente Blue Team</span>
                </div>
                <span className={`px-2 py-0.5 rounded-full text-xs font-bold ${
                  aiSummary.severity === 'CRITICAL' ? 'bg-red-500/20 text-red-400 border border-red-500/30' :
                  aiSummary.severity === 'HIGH' ? 'bg-orange-500/20 text-orange-400 border border-orange-500/30' :
                  aiSummary.severity === 'MEDIUM' ? 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30' :
                  'bg-green-500/20 text-green-400 border border-green-500/30'
                }`}>
                  Severidad: {aiSummary.severity}
                </span>
              </div>

              <div>
                <p className="text-white text-xs font-medium">{aiSummary.summary}</p>
                {aiSummary.probableCause && (
                  <p className="text-slate-400 text-xs mt-1.5 italic">
                    <strong className="text-slate-300 not-italic">Causa probable:</strong> {aiSummary.probableCause}
                  </p>
                )}
              </div>

              {aiSummary.recommendedActions && aiSummary.recommendedActions.length > 0 && (
                <div className="pt-2 border-t border-purple-500/20 space-y-1.5">
                  <p className="text-slate-300 text-xs font-semibold">Acciones recomendadas:</p>
                  <ul className="space-y-1">
                    {aiSummary.recommendedActions.map((action, idx) => (
                      <li key={idx} className="flex items-start gap-1.5 text-xs text-purple-200">
                        <span className="text-purple-400 font-bold">•</span>
                        <span>{action}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}

          {/* Metadata grid */}
          <div className="grid grid-cols-2 gap-3">
            {[
              { label: 'Dispositivo', value: currentDep.deviceModel || 'N/D' },
              { label: 'Dominio', value: currentDep.domainName || 'N/D' },
              { label: 'Técnico asignado', value: currentDep.technicianName || 'No asignado' },
              { label: 'Fase actual', value: currentDep.currentStep || 'N/D' },
            ].map(({ label, value }) => (
              <div key={label} className="bg-slate-800 rounded-xl p-3">
                <p className="text-slate-500 text-xs mb-1">{label}</p>
                <p className="text-white text-sm font-medium break-all">{value}</p>
              </div>
            ))}
          </div>

          {/* Self-assign or Admin Reassign */}
          {(!currentDep.technicianId || !currentDep.technicianName || currentDep.technicianName === 'No asignado') && (
            <div className="p-3.5 bg-blue-950/40 rounded-xl border border-blue-500/30 flex items-center justify-between gap-3">
              <div>
                <p className="text-blue-200 text-xs font-semibold">Despliegue sin técnico asignado</p>
                <p className="text-slate-400 text-[11px]">Puedes asumir la supervisión y remediación de este equipo de forma inmediata.</p>
              </div>
              <button
                onClick={handleClaim}
                disabled={claiming}
                className="py-1.5 px-3 bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold rounded-lg transition-all shadow-md shadow-blue-900/30 shrink-0 disabled:opacity-50 flex items-center gap-1.5"
              >
                <UserCheck className="w-3.5 h-3.5" />
                {claiming ? 'Asignando...' : 'Asignarme este despliegue'}
              </button>
            </div>
          )}

          {/* Admin Technician Reassignment */}
          {isAdmin && (
            <div className="p-3.5 bg-slate-800/80 rounded-xl border border-slate-700/60 flex items-center justify-between gap-3">
              <div className="flex items-center gap-2">
                <UserCheck className="w-4 h-4 text-blue-400 shrink-0" />
                <span className="text-slate-300 text-xs font-medium">Reasignar técnico:</span>
              </div>
              <select
                value={assignedTechId || ''}
                onChange={handleReassignTech}
                disabled={reassigning}
                className="bg-slate-900 border border-slate-700 text-slate-200 text-xs rounded-lg px-2.5 py-1.5 focus:outline-none focus:border-blue-500"
              >
                <option value="">Seleccionar técnico...</option>
                {technicians.map((t) => (
                  <option key={t.id} value={t.id}>{t.name} (@{t.username})</option>
                ))}
              </select>
            </div>
          )}

          {/* Status badges & Manual Status Edit Button */}
          <div className="p-3.5 bg-slate-800/80 rounded-xl border border-slate-700/60 space-y-3">
            <div className="flex items-center justify-between gap-2 flex-wrap">
              <div className="flex items-center gap-3 flex-wrap">
                <div className="flex items-center gap-2">
                  <span className="text-slate-500 text-xs">Estado:</span>
                  <Badge status={currentDep.deploymentStatus} />
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-slate-500 text-xs">Compliance:</span>
                  <Badge status={currentDep.complianceStatus} />
                </div>
              </div>

              <button
                type="button"
                onClick={() => {
                  setIsEditingStatus(!isEditingStatus)
                  setEditDepStatus(currentDep.deploymentStatus || 'IN_PROGRESS')
                  setEditCompStatus(currentDep.complianceStatus || 'PENDING')
                  setEditCurrentStep(currentDep.currentStep || 'DEPLOYMENT_CREATED')
                  setEditNote('')
                }}
                className="py-1 px-2.5 bg-slate-700 hover:bg-slate-600 text-slate-200 hover:text-white rounded-lg text-xs font-medium border border-slate-600 flex items-center gap-1.5 transition-all"
              >
                <Sliders className="w-3.5 h-3.5 text-blue-400" />
                <span>{isEditingStatus ? 'Cancelar edición' : 'Cambiar Estado Manual'}</span>
              </button>
            </div>

            {/* Manual Status Editing Form */}
            {isEditingStatus && (
              <form onSubmit={handleSaveManualStatus} className="pt-3 border-t border-slate-700/60 space-y-3">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                  <div>
                    <label className="block text-[11px] font-semibold text-slate-300 mb-1">
                      Estado del Despliegue:
                    </label>
                    <select
                      value={editDepStatus}
                      onChange={(e) => setEditDepStatus(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-700 rounded-lg px-2.5 py-1.5 text-xs text-white focus:outline-none focus:border-blue-500"
                    >
                      <option value="IN_PROGRESS">IN_PROGRESS (En progreso)</option>
                      <option value="COMPLETED">COMPLETED (Completado)</option>
                      <option value="COMPLETED_WITH_WARNINGS">COMPLETED_WITH_WARNINGS (Con advertencias)</option>
                      <option value="FAILED">FAILED (Fallido)</option>
                      <option value="DOMAIN_JOINED">DOMAIN_JOINED (Dominio unido)</option>
                      <option value="POST_DOMAIN_PENDING">POST_DOMAIN_PENDING (Post-dominio pendiente)</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-[11px] font-semibold text-slate-300 mb-1">
                      Estado de Compliance:
                    </label>
                    <select
                      value={editCompStatus}
                      onChange={(e) => setEditCompStatus(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-700 rounded-lg px-2.5 py-1.5 text-xs text-white focus:outline-none focus:border-blue-500"
                    >
                      <option value="PENDING">PENDING (Pendiente)</option>
                      <option value="COMPLIANT">COMPLIANT (Conforme / Cumple)</option>
                      <option value="NON_COMPLIANT">NON_COMPLIANT (No conforme)</option>
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-[11px] font-semibold text-slate-300 mb-1">
                    Fase Actual (Current Step):
                  </label>
                  <input
                    type="text"
                    value={editCurrentStep}
                    onChange={(e) => setEditCurrentStep(e.target.value)}
                    placeholder="Ej: DEPLOYMENT_COMPLETED, DOMAIN_JOIN, INSTALL_SOFTWARE..."
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-2.5 py-1.5 text-xs text-white focus:outline-none focus:border-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-[11px] font-semibold text-slate-300 mb-1">
                    Nota o justificación de auditoría (opcional):
                  </label>
                  <input
                    type="text"
                    value={editNote}
                    onChange={(e) => setEditNote(e.target.value)}
                    placeholder="Ej: Verificación manual presencial completada por soporte..."
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-2.5 py-1.5 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-blue-500"
                  />
                </div>

                <div className="flex justify-end gap-2 pt-1">
                  <button
                    type="button"
                    onClick={() => setIsEditingStatus(false)}
                    className="px-3 py-1.5 text-xs text-slate-400 hover:text-slate-300 rounded-lg"
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    disabled={savingStatus}
                    className="flex items-center gap-1.5 bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold px-3.5 py-1.5 rounded-lg transition-all shadow-md shadow-blue-900/30 disabled:opacity-50"
                  >
                    <Save className="w-3.5 h-3.5" />
                    {savingStatus ? 'Guardando...' : 'Guardar Estado'}
                  </button>
                </div>
              </form>
            )}
          </div>

          {/* Steps */}
          <section>
            <h3 className="text-slate-300 text-sm font-semibold mb-3 flex items-center gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-blue-400 shrink-0" />
              Fases del despliegue
              <span className="text-slate-600 font-normal">({steps.length})</span>
            </h3>
            {steps.length === 0 ? (
              <p className="text-slate-500 text-xs p-3 bg-slate-800/40 rounded-xl">No hay pasos registrados para este despliegue.</p>
            ) : (
              <div className="space-y-2">
                {steps.map((step, i) => (
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
            )}
          </section>

          {/* Security checks */}
          <section>
            <h3 className="text-slate-300 text-sm font-semibold mb-3 flex items-center gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-purple-400 shrink-0" />
              Verificaciones de seguridad
              <span className="text-slate-600 font-normal">({checks.length})</span>
            </h3>
            {checks.length === 0 ? (
              <p className="text-slate-500 text-xs p-3 bg-slate-800/40 rounded-xl">No hay checks de seguridad registrados.</p>
            ) : (
              <div className="space-y-2.5">
                {checks.map((check, i) => {
                  const canRemediate = ['FAILED', 'WARNING', 'PENDING', 'UNKNOWN'].includes(check.checkStatus)
                  const isResolvingThis = resolvingCheckId === check.id

                  return (
                    <div key={check.id || i} className="p-3 bg-slate-800/80 rounded-xl space-y-2 border border-slate-700/40">
                      <div className="flex items-start gap-3">
                        <StatusIcon status={check.checkStatus} />
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 flex-wrap">
                            <p className="text-white text-sm font-medium leading-none">{check.checkName}</p>
                            <Badge status={check.severity} />
                          </div>
                          <p className="text-slate-400 text-xs mt-1">{check.message}</p>
                          <p className="text-slate-600 text-xs mt-0.5">
                            Esperado: <span className="text-slate-400">{check.expectedValue || '—'}</span>
                            {' · '}
                            Obtenido: <span className="text-slate-400">{check.actualValue || '—'}</span>
                          </p>
                        </div>
                        <div className="flex flex-col items-end gap-1.5 shrink-0">
                          <Badge status={check.checkStatus} />
                          <button
                            onClick={() => {
                              setResolvingCheckId(isResolvingThis ? null : check.id)
                              setRemediationNote('')
                            }}
                            className={`text-[11px] px-2 py-0.5 rounded-md border flex items-center gap-1 transition-all ${
                              canRemediate
                                ? 'text-blue-400 hover:text-blue-300 bg-blue-500/10 hover:bg-blue-500/20 border-blue-500/20 font-medium'
                                : 'text-slate-400 hover:text-slate-200 bg-slate-700/30 hover:bg-slate-700/60 border-slate-600/30'
                            }`}
                          >
                            <Wrench className="w-3 h-3" />
                            {isResolvingThis ? 'Cancelar' : (canRemediate ? 'Corregir / Remediar' : 'Revisar')}
                          </button>
                        </div>
                      </div>

                      {/* Remediation Note Form */}
                      {isResolvingThis && (
                        <div className="pt-2 mt-2 border-t border-slate-700/60 bg-slate-900/60 p-3 rounded-lg space-y-2">
                          <p className="text-slate-300 text-xs font-semibold">Reflejar acción de corrección / remediación:</p>
                          <input
                            type="text"
                            value={remediationNote}
                            onChange={(e) => setRemediationNote(e.target.value)}
                            placeholder="Ej: Sensor CrowdStrike instalado y sincronizado manualmente..."
                            className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-1.5 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-blue-500"
                          />
                          <div className="flex justify-end gap-2">
                            <button
                              type="button"
                              onClick={() => setResolvingCheckId(null)}
                              className="text-xs text-slate-400 hover:text-slate-300 px-2.5 py-1"
                            >
                              Cancelar
                            </button>
                            <button
                              type="button"
                              onClick={() => submitResolveCheck(check.id)}
                              disabled={resolvingLoading}
                              className="flex items-center gap-1 bg-green-600 hover:bg-green-500 text-white text-xs font-semibold px-3 py-1 rounded-lg transition-all disabled:opacity-50"
                            >
                              <Check className="w-3.5 h-3.5" />
                              {resolvingLoading ? 'Guardando...' : 'Marcar como Remediado'}
                            </button>
                          </div>
                        </div>
                      )}
                    </div>
                  )
                })}
              </div>
            )}
          </section>

          {/* Timestamps */}
          <section className="flex flex-col gap-1.5 pt-2 border-t border-slate-800">
            {[
              { label: 'Iniciado', value: fmt(currentDep.startedAt) },
              { label: 'Finalizado', value: fmt(currentDep.finishedAt) },
              { label: 'Última actividad', value: fmt(currentDep.lastSeenAt) },
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

import { useState } from 'react'
import { Link } from 'react-router-dom'
import {
  Shield,
  Terminal,
  Cpu,
  CheckCircle2,
  FileText,
  Bot,
  Users,
  Phone,
  ExternalLink,
  ArrowRight,
  LogIn,
  LayoutDashboard,
  Lock,
  Layers,
  Zap,
  Server,
  Database,
  Check,
  ChevronRight,
  ShieldCheck,
  Workflow
} from 'lucide-react'
import { useAuth } from '../auth/AuthContext'

function LinkedinIcon(props) {
  return (
    <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round" {...props}>
      <path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z" />
      <rect x="2" y="9" width="4" height="12" />
      <circle cx="4" cy="4" r="2" />
    </svg>
  )
}

export default function LandingPage() {
  const { user } = useAuth()
  const [activeTab, setActiveTab] = useState('all')

  const scrollTo = (id) => {
    const el = document.getElementById(id)
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' })
    }
  }

  return (
    <div
      className="min-h-screen text-slate-100 flex flex-col selection:bg-blue-500 selection:text-white"
      style={{
        background: 'radial-gradient(ellipse at top left, #1e3a5f 0%, transparent 45%), radial-gradient(ellipse at bottom right, #0c2340 0%, transparent 45%), linear-gradient(135deg, #020817 0%, #0b1329 50%, #020817 100%)',
      }}
    >
      {/* ── NAVBAR SUPERIOR ────────────────────────────────────────── */}
      <header className="sticky top-0 z-50 backdrop-blur-xl bg-slate-950/75 border-b border-slate-800/80">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-3 group">
            <div className="w-11 h-11 rounded-2xl bg-gradient-to-br from-blue-500 to-cyan-400 flex items-center justify-center shadow-lg shadow-blue-500/25 group-hover:scale-105 transition-transform duration-200">
              <Shield className="w-6 h-6 text-white" />
            </div>
            <div>
              <span className="text-xl font-bold tracking-tight text-white flex items-center gap-1.5">
                BlueDeploy
                <span className="text-[10px] font-semibold tracking-wider uppercase px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-400 border border-blue-500/20">
                  SecOps
                </span>
              </span>
              <p className="text-xs text-slate-400 font-medium">Orquestación & Auditoría OSDCloud</p>
            </div>
          </Link>

          {/* Menú Central */}
          <nav className="hidden md:flex items-center gap-1 bg-slate-900/60 p-1.5 rounded-2xl border border-slate-800/60 text-sm">
            <button
              onClick={() => scrollTo('intro')}
              className="px-4 py-2 rounded-xl text-slate-300 hover:text-white hover:bg-slate-800/60 transition-colors font-medium"
            >
              Introducción
            </button>
            <button
              onClick={() => scrollTo('features')}
              className="px-4 py-2 rounded-xl text-slate-300 hover:text-white hover:bg-slate-800/60 transition-colors font-medium"
            >
              Características
            </button>
            <button
              onClick={() => scrollTo('architecture')}
              className="px-4 py-2 rounded-xl text-slate-300 hover:text-white hover:bg-slate-800/60 transition-colors font-medium"
            >
              Arquitectura
            </button>
            <button
              onClick={() => scrollTo('contact')}
              className="px-4 py-2 rounded-xl text-slate-300 hover:text-white hover:bg-slate-800/60 transition-colors font-medium"
            >
              Contacto
            </button>
          </nav>

          {/* Acciones de Login / Panel */}
          <div className="flex items-center gap-3">
            {user ? (
              <Link
                to={user.role === 'ADMIN' ? '/admin' : '/technician'}
                className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-cyan-600 hover:from-blue-500 hover:to-cyan-500 text-white text-sm font-semibold shadow-lg shadow-blue-600/30 transition-all hover:scale-[1.02]"
              >
                <LayoutDashboard className="w-4 h-4" />
                <span>Ir al Panel ({user.name})</span>
              </Link>
            ) : (
              <Link
                to="/login"
                className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-sm font-semibold shadow-lg shadow-blue-600/25 transition-all hover:scale-[1.02]"
              >
                <LogIn className="w-4 h-4" />
                <span>Acceso a Plataforma</span>
              </Link>
            )}
          </div>
        </div>
      </header>

      {/* ── HERO SECTION ────────────────────────────────────────────── */}
      <section id="intro" className="relative pt-16 pb-24 lg:pt-24 lg:pb-32 overflow-hidden">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="text-center max-w-3xl mx-auto">
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-blue-500/10 border border-blue-500/20 text-blue-400 text-xs font-semibold uppercase tracking-wider mb-6 animate-pulse">
              <ShieldCheck className="w-4 h-4" />
              <span>Plataforma de Despliegue & Telemetría Zero-Trust</span>
            </div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-white leading-tight sm:leading-none">
              Control total en el despliegue masivo de{' '}
              <span className="bg-gradient-to-r from-blue-400 via-cyan-300 to-blue-500 bg-clip-text text-transparent">
                puestos de trabajo
              </span>
            </h1>

            <p className="mt-6 text-lg sm:text-xl text-slate-300 leading-relaxed font-normal">
              BlueDeploy integra el motor de instalación automatizada <strong>OSDCloud</strong> con telemetría en tiempo real, validación post-despliegue de directivas de seguridad corporativas, diagnósticos inteligentes con IA y generación de informes de auditoría inmutables.
            </p>

            <div className="mt-10 flex flex-wrap items-center justify-center gap-4">
              <Link
                to={user ? (user.role === 'ADMIN' ? '/admin' : '/technician') : '/login'}
                className="flex items-center gap-2.5 px-7 py-3.5 rounded-2xl bg-blue-600 hover:bg-blue-500 text-white font-semibold shadow-xl shadow-blue-600/30 transition-all hover:scale-105 text-base"
              >
                <span>{user ? 'Abrir Panel de Control' : 'Iniciar Sesión'}</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
              <button
                onClick={() => scrollTo('contact')}
                className="flex items-center gap-2 px-6 py-3.5 rounded-2xl bg-slate-800/80 hover:bg-slate-700/80 border border-slate-700 text-slate-200 font-semibold transition-all hover:scale-105 text-base"
              >
                <Phone className="w-4 h-4 text-blue-400" />
                <span>Información de Contacto</span>
              </button>
            </div>
          </div>

          {/* Tarjetas de Estadísticas Destacadas */}
          <div className="mt-16 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {[
              {
                icon: Terminal,
                title: 'OSDCloud Engine',
                desc: 'Automatización completa de WinPE a Windows 11 sin intervención física.',
                color: 'from-blue-500 to-blue-600',
              },
              {
                icon: ShieldCheck,
                title: 'Compliance Post-Dominio',
                desc: 'Verificación estricta de CrowdStrike, MicroClaudia, WSUS y BitLocker.',
                color: 'from-emerald-500 to-teal-600',
              },
              {
                icon: Bot,
                title: 'Diagnóstico con IA',
                desc: 'AiSummary contextual para resolución inmediata de errores 0x8007052E y SMART.',
                color: 'from-purple-500 to-indigo-600',
              },
              {
                icon: FileText,
                title: 'Auditoría & Informes PDF',
                desc: 'Exportación de actas ejecutivas con sello temporal y registro de trazabilidad.',
                color: 'from-amber-500 to-orange-600',
              },
            ].map((card, idx) => (
              <div
                key={idx}
                className="bg-slate-900/60 backdrop-blur-md border border-slate-800/80 rounded-2xl p-6 hover:border-slate-700 transition-all hover:shadow-xl hover:shadow-blue-900/10 group"
              >
                <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${card.color} flex items-center justify-center mb-4 shadow-lg group-hover:scale-110 transition-transform`}>
                  <card.icon className="w-6 h-6 text-white" />
                </div>
                <h3 className="text-lg font-bold text-white mb-2">{card.title}</h3>
                <p className="text-slate-400 text-sm leading-relaxed">{card.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── CARACTERÍSTICAS PRINCIPALES ─────────────────────────────── */}
      <section id="features" className="py-20 bg-slate-950/60 border-y border-slate-800/60">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <h2 className="text-xs font-semibold uppercase tracking-wider text-blue-400 mb-2">
              Capacidades Principales
            </h2>
            <p className="text-3xl sm:text-4xl font-extrabold text-white">
              Diseñado para infraestructuras críticas
            </p>
            <p className="mt-4 text-slate-400 text-base">
              Supervisión de extremo a extremo, desde el arranque en frío hasta la puesta en marcha con directivas corporativas.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {/* Feature 1 */}
            <div className="bg-slate-900/70 border border-slate-800 rounded-3xl p-8 hover:border-blue-500/40 transition-all flex flex-col justify-between">
              <div>
                <div className="w-12 h-12 rounded-2xl bg-blue-500/10 border border-blue-500/20 text-blue-400 flex items-center justify-center mb-6">
                  <Workflow className="w-6 h-6" />
                </div>
                <h3 className="text-xl font-bold text-white mb-3">Flujo de Telemetría Resiliente</h3>
                <p className="text-slate-400 text-sm leading-relaxed mb-6">
                  Desacoplamiento mediante colas JSON locales (<code className="text-blue-300 text-xs">010</code> a <code className="text-blue-300 text-xs">999</code>). Los eventos de instalación se almacenan localmente y se envían de forma ordenada al servidor central sin pérdida de datos ante cortes de red.
                </p>
              </div>
              <ul className="space-y-2 text-xs text-slate-300">
                <li className="flex items-center gap-2">
                  <Check className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>Purga atómica tras confirmación HTTP 200/201</span>
                </li>
                <li className="flex items-center gap-2">
                  <Check className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>Autenticación por API Key corporativa</span>
                </li>
              </ul>
            </div>

            {/* Feature 2 */}
            <div className="bg-slate-900/70 border border-slate-800 rounded-3xl p-8 hover:border-blue-500/40 transition-all flex flex-col justify-between">
              <div>
                <div className="w-12 h-12 rounded-2xl bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 flex items-center justify-center mb-6">
                  <ShieldCheck className="w-6 h-6" />
                </div>
                <h3 className="text-xl font-bold text-white mb-3">Auditoría de Seguridad Post-Dominio</h3>
                <p className="text-slate-400 text-sm leading-relaxed mb-6">
                  Comprobación automatizada de directivas sanitarias y empresariales: presencia de CrowdStrike Falcon, MicroClaudia, reglas de cortafuegos activas, membresía a grupos de WSUS y saneamiento de credenciales temporales.
                </p>
              </div>
              <ul className="space-y-2 text-xs text-slate-300">
                <li className="flex items-center gap-2">
                  <Check className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>Clasificación estricta: COMPLIANT / NON_COMPLIANT</span>
                </li>
                <li className="flex items-center gap-2">
                  <Check className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>Eliminación segura de AutoAdminLogon en registro</span>
                </li>
              </ul>
            </div>

            {/* Feature 3 */}
            <div className="bg-slate-900/70 border border-slate-800 rounded-3xl p-8 hover:border-blue-500/40 transition-all flex flex-col justify-between">
              <div>
                <div className="w-12 h-12 rounded-2xl bg-purple-500/10 border border-purple-500/20 text-purple-400 flex items-center justify-center mb-6">
                  <Bot className="w-6 h-6" />
                </div>
                <h3 className="text-xl font-bold text-white mb-3">Asistencia Diagnóstica con IA</h3>
                <p className="text-slate-400 text-sm leading-relaxed mb-6">
                  Motor de diagnóstico contextual que analiza la traza completa de errores (como fallos de replicación Kerberos <code className="text-purple-300 text-xs">0x8007052E</code> o advertencias de disco SMART) y propone pasos de remediación precisos.
                </p>
              </div>
              <ul className="space-y-2 text-xs text-slate-300">
                <li className="flex items-center gap-2">
                  <Check className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>Generación instantánea de AiSummary</span>
                </li>
                <li className="flex items-center gap-2">
                  <Check className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>Evaluación de severidad y causa raíz</span>
                </li>
              </ul>
            </div>

            {/* Feature 4 */}
            <div className="bg-slate-900/70 border border-slate-800 rounded-3xl p-8 hover:border-blue-500/40 transition-all flex flex-col justify-between">
              <div>
                <div className="w-12 h-12 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 flex items-center justify-center mb-6">
                  <Users className="w-6 h-6" />
                </div>
                <h3 className="text-xl font-bold text-white mb-3">Seguridad RBAC & Aislamiento</h3>
                <p className="text-slate-400 text-sm leading-relaxed mb-6">
                  Consola segmentada por roles: el Administrador supervisa métricas globales y auditoría de accesos; los Técnicos de campo gestionan exclusivamente sus puestos asignados con opción de autoasignación rápida.
                </p>
              </div>
              <ul className="space-y-2 text-xs text-slate-300">
                <li className="flex items-center gap-2">
                  <Check className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>Tokens JWT con expiración segura</span>
                </li>
                <li className="flex items-center gap-2">
                  <Check className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>Registro inmutable AuditLog en base de datos</span>
                </li>
              </ul>
            </div>

            {/* Feature 5 */}
            <div className="bg-slate-900/70 border border-slate-800 rounded-3xl p-8 hover:border-blue-500/40 transition-all flex flex-col justify-between">
              <div>
                <div className="w-12 h-12 rounded-2xl bg-amber-500/10 border border-amber-500/20 text-amber-400 flex items-center justify-center mb-6">
                  <FileText className="w-6 h-6" />
                </div>
                <h3 className="text-xl font-bold text-white mb-3">Reportes Ejecutivos en PDF</h3>
                <p className="text-slate-400 text-sm leading-relaxed mb-6">
                  Generación en tiempo de ejecución de actas de despliegue mediante OpenPDF. Incluye datos de inventario, tiempos por fase, matriz de comprobaciones y sello de validación técnica.
                </p>
              </div>
              <ul className="space-y-2 text-xs text-slate-300">
                <li className="flex items-center gap-2">
                  <Check className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>Formato profesional corporativo</span>
                </li>
                <li className="flex items-center gap-2">
                  <Check className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>Descarga directa con 1 clic desde el detalle</span>
                </li>
              </ul>
            </div>

            {/* Feature 6 */}
            <div className="bg-slate-900/70 border border-slate-800 rounded-3xl p-8 hover:border-blue-500/40 transition-all flex flex-col justify-between">
              <div>
                <div className="w-12 h-12 rounded-2xl bg-rose-500/10 border border-rose-500/20 text-rose-400 flex items-center justify-center mb-6">
                  <Server className="w-6 h-6" />
                </div>
                <h3 className="text-xl font-bold text-white mb-3">Infraestructura Hardened</h3>
                <p className="text-slate-400 text-sm leading-relaxed mb-6">
                  Despliegue contenerizado en Docker con MariaDB 11 en red privada aislada, Spring Boot 3 con Java 21, y Nginx como proxy frontal con cabeceras de seguridad CSP, HSTS y X-Frame-Options.
                </p>
              </div>
              <ul className="space-y-2 text-xs text-slate-300">
                <li className="flex items-center gap-2">
                  <Check className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>Alojamiento en servidor de producción Hetzner</span>
                </li>
                <li className="flex items-center gap-2">
                  <Check className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>Tolerante a pruebas virtuales y hardware real</span>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      {/* ── ARQUITECTURA TÉCNICA ─────────────────────────────────────── */}
      <section id="architecture" className="py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <h2 className="text-xs font-semibold uppercase tracking-wider text-blue-400 mb-2">
              Flujo & Arquitectura
            </h2>
            <p className="text-3xl sm:text-4xl font-extrabold text-white">
              Canalización de Despliegue en 4 Fases
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            {[
              {
                step: '01',
                title: 'WinPE / OSDCloud',
                desc: 'Captura interactiva de Hostname y CEGES, aplicación de WIM Windows 11 y copia de scripts SetupComplete.',
                badge: 'Pre-instalación',
              },
              {
                step: '02',
                title: 'SetupComplete.cmd',
                desc: 'Inyección de drivers por hardware, configuración de perfil RADIUS 802.1X y unión al Directorio Activo.',
                badge: 'Especialización',
              },
              {
                step: '03',
                title: 'Software & Security',
                desc: 'Instalación de paquetes de software corporativo, validación de agentes EDR y ejecución de Check-SecurityCompliance.',
                badge: 'Post-instalación',
              },
              {
                step: '04',
                title: 'BlueDeploy Core',
                desc: 'Ingesta de telemetría, evaluación de conformidad global, persistencia en MariaDB y visualización web.',
                badge: 'Control Central',
              },
            ].map((fase, idx) => (
              <div
                key={idx}
                className="bg-slate-900/50 border border-slate-800 rounded-2xl p-6 relative flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between mb-4">
                    <span className="text-3xl font-black text-blue-500/40">{fase.step}</span>
                    <span className="text-[11px] font-semibold uppercase px-2.5 py-1 rounded-full bg-slate-800 text-blue-300 border border-slate-700">
                      {fase.badge}
                    </span>
                  </div>
                  <h3 className="text-lg font-bold text-white mb-2">{fase.title}</h3>
                  <p className="text-slate-400 text-xs leading-relaxed">{fase.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── SECCIÓN DE CONTACTO & AUTOR ─────────────────────────────── */}
      <section id="contact" className="py-20 bg-slate-950/80 border-t border-slate-800/80">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="max-w-3xl mx-auto bg-gradient-to-b from-slate-900 to-slate-900/90 border border-slate-700/80 rounded-3xl p-8 sm:p-12 shadow-2xl relative overflow-hidden">
            {/* Glow decorativo */}
            <div className="absolute top-0 right-0 -mt-12 -mr-12 w-64 h-64 bg-blue-600/10 rounded-full blur-3xl pointer-events-none" />

            <div className="relative z-10">
              <div className="flex flex-col sm:flex-row items-center sm:items-start gap-6 mb-8 text-center sm:text-left">
                <div className="w-20 h-20 rounded-3xl bg-gradient-to-br from-blue-500 via-indigo-500 to-cyan-400 flex items-center justify-center shadow-xl shadow-blue-500/20 shrink-0 text-white text-2xl font-bold">
                  DR
                </div>
                <div>
                  <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-500/10 text-blue-400 border border-blue-500/20 text-xs font-semibold mb-2">
                    <span>Autor & Responsable del Proyecto</span>
                  </div>
                  <h2 className="text-2xl sm:text-3xl font-bold text-white">David Robles Gaona</h2>
                  <p className="text-slate-400 text-sm mt-1">
                    Administrador de Sistemas & Ingeniero de Software · Proyecto BlueDeploy
                  </p>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-8">
                {/* Teléfono */}
                <a
                  href="tel:629096538"
                  className="flex items-center gap-4 p-4 rounded-2xl bg-slate-800/70 hover:bg-slate-800 border border-slate-700/70 hover:border-blue-500/50 transition-all group"
                >
                  <div className="w-12 h-12 rounded-xl bg-blue-500/10 text-blue-400 flex items-center justify-center shrink-0 group-hover:scale-110 transition-transform">
                    <Phone className="w-5 h-5" />
                  </div>
                  <div>
                    <span className="text-xs text-slate-400 font-medium block">Teléfono de Contacto</span>
                    <span className="text-base font-bold text-white group-hover:text-blue-400 transition-colors">
                      629096538
                    </span>
                  </div>
                </a>

                {/* LinkedIn */}
                <a
                  href="https://www.linkedin.com/in/david-robles-gaona"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-4 p-4 rounded-2xl bg-slate-800/70 hover:bg-slate-800 border border-slate-700/70 hover:border-cyan-500/50 transition-all group"
                >
                  <div className="w-12 h-12 rounded-xl bg-cyan-500/10 text-cyan-400 flex items-center justify-center shrink-0 group-hover:scale-110 transition-transform">
                    <LinkedinIcon className="w-5 h-5" />
                  </div>
                  <div className="min-w-0 flex-1">
                    <span className="text-xs text-slate-400 font-medium block">Perfil Profesional</span>
                    <span className="text-sm font-bold text-white group-hover:text-cyan-400 transition-colors truncate flex items-center gap-1">
                      david-robles-gaona
                      <ExternalLink className="w-3.5 h-3.5 shrink-0" />
                    </span>
                  </div>
                </a>
              </div>

              {/* Botón de acceso a login */}
              <div className="mt-8 pt-8 border-t border-slate-800 flex flex-col sm:flex-row items-center justify-between gap-4">
                <p className="text-xs text-slate-400 text-center sm:text-left">
                  ¿Deseas acceder al panel administrativo o técnico?
                </p>
                <Link
                  to="/login"
                  className="flex items-center gap-2 px-6 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-sm font-semibold transition-all hover:scale-105 shadow-lg shadow-blue-600/20"
                >
                  <LogIn className="w-4 h-4" />
                  <span>Acceso a la Aplicación</span>
                </Link>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ── FOOTER ─────────────────────────────────────────────────── */}
      <footer className="mt-auto py-8 bg-slate-950 border-t border-slate-900 text-center text-xs text-slate-500">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2 text-slate-400">
            <Shield className="w-4 h-4 text-blue-500" />
            <span className="font-semibold text-slate-300">BlueDeploy</span>
            <span>· Plataforma de Despliegues & Auditoría</span>
          </div>
          <p>© 2026 David Robles Gaona · Todos los derechos reservados.</p>
        </div>
      </footer>
    </div>
  )
}

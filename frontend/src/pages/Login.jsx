import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Shield, Eye, EyeOff, LogIn } from 'lucide-react'
import { useAuth } from '../auth/AuthContext'

const DEMO_USERS = [
  { label: 'Administrador', username: 'admin', password: 'admin', color: 'purple' },
  { label: 'Carlos (Técnico)', username: 'carlos', password: 'tecnico1', color: 'blue' },
  { label: 'Ana (Técnico)', username: 'ana', password: 'tecnico2', color: 'cyan' },
]

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPass, setShowPass] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)

    await new Promise((r) => setTimeout(r, 300)) // UX delay
    const result = login(username, password)
    setLoading(false)

    if (!result.success) {
      setError(result.error)
      return
    }

    navigate(result.user.role === 'ADMIN' ? '/admin' : '/technician', { replace: true })
  }

  function fillDemo(u, p) {
    setUsername(u)
    setPassword(p)
    setError('')
  }

  return (
    <div
      className="min-h-screen flex items-center justify-center p-4"
      style={{ background: 'radial-gradient(ellipse at top left, #1e3a5f 0%, transparent 50%), linear-gradient(135deg, #020817 0%, #0f172a 50%, #020817 100%)' }}
    >
      <div className="w-full max-w-md">
        {/* Card */}
        <div className="bg-slate-900/80 backdrop-blur-xl border border-slate-700/50 rounded-3xl p-8 shadow-2xl shadow-black/50">
          {/* Brand */}
          <div className="flex items-center gap-4 mb-8">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-blue-500 to-cyan-400 flex items-center justify-center shadow-lg shadow-blue-900/40 shrink-0">
              <Shield className="w-7 h-7 text-white" />
            </div>
            <div>
              <h1 className="text-white text-2xl font-bold leading-none">Blue Deploy</h1>
              <p className="text-slate-400 text-sm mt-1.5">Panel de monitoreo y despliegue</p>
            </div>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="text-slate-400 text-xs font-semibold uppercase tracking-wider block mb-2">
                Usuario
              </label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Nombre de usuario"
                required
                autoFocus
                className="w-full bg-slate-800 border border-slate-700 rounded-xl px-4 py-3 text-white text-sm placeholder-slate-600 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30 transition-all"
              />
            </div>

            <div>
              <label className="text-slate-400 text-xs font-semibold uppercase tracking-wider block mb-2">
                Contraseña
              </label>
              <div className="relative">
                <input
                  type={showPass ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl px-4 py-3 pr-11 text-white text-sm placeholder-slate-600 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30 transition-all"
                />
                <button
                  type="button"
                  onClick={() => setShowPass((v) => !v)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 transition-colors"
                >
                  {showPass ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {error && (
              <div className="flex items-center gap-2 p-3 bg-red-500/10 border border-red-500/20 rounded-xl">
                <span className="w-1.5 h-1.5 rounded-full bg-red-400 shrink-0" />
                <p className="text-red-400 text-sm">{error}</p>
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center gap-2 py-3 mt-2 rounded-xl bg-blue-600 hover:bg-blue-500 disabled:opacity-60 text-white text-sm font-semibold transition-all shadow-lg shadow-blue-900/30"
            >
              {loading ? (
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <LogIn className="w-4 h-4" />
              )}
              {loading ? 'Iniciando...' : 'Iniciar sesión'}
            </button>
          </form>

          {/* Demo users */}
          <div className="mt-6 pt-5 border-t border-slate-800">
            <p className="text-slate-500 text-xs text-center mb-3">Acceso rápido (modo demo)</p>
            <div className="grid grid-cols-3 gap-2">
              {DEMO_USERS.map(({ label, username: u, password: p, color }) => (
                <button
                  key={u}
                  type="button"
                  onClick={() => fillDemo(u, p)}
                  className={`py-2 px-3 rounded-xl text-xs font-medium transition-all border ${
                    color === 'purple'
                      ? 'bg-purple-500/10 text-purple-400 border-purple-500/20 hover:bg-purple-500/20'
                      : color === 'cyan'
                      ? 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20 hover:bg-cyan-500/20'
                      : 'bg-blue-500/10 text-blue-400 border-blue-500/20 hover:bg-blue-500/20'
                  }`}
                >
                  {label}
                </button>
              ))}
            </div>
          </div>
        </div>

        <p className="text-slate-600 text-xs text-center mt-6">
          Blue Deploy · Blue Team Platform · Práctica 1
        </p>
      </div>
    </div>
  )
}

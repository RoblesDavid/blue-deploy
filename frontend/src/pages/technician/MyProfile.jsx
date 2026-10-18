import { useState, useEffect } from 'react'
import { Save, User, Mail, Building2, KeyRound, AlertCircle, CheckCircle2 } from 'lucide-react'
import { useAuth } from '../../auth/AuthContext'
import usersApi from '../../api/usersApi'

export default function MyProfile() {
  const { user, updateUser } = useAuth()
  const [form, setForm] = useState({
    name: user?.name ?? '',
    email: user?.email ?? '',
    department: user?.department ?? '',
    password: '',
  })
  const [loading, setLoading] = useState(false)
  const [saved, setSaved] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    async function fetchMe() {
      try {
        const me = await usersApi.getMe()
        if (me) {
          setForm({
            name: me.name || '',
            email: me.email || '',
            department: me.department || '',
            password: '',
          })
          updateUser(me)
        }
      } catch (err) {
        console.error('Error fetching profile:', err)
      }
    }
    fetchMe()
  }, [])

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))
    setSaved(false)
    setError('')
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const payload = {
        name: form.name,
        email: form.email,
        department: form.department,
      }
      if (form.password && form.password.trim() !== '') {
        payload.password = form.password
      }

      const updated = await usersApi.updateMe(payload)
      updateUser(updated)
      setSaved(true)
      setTimeout(() => setSaved(false), 4000)
    } catch (err) {
      setError(err.message || 'Error al actualizar perfil')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-2xl space-y-5">
      {/* Avatar card */}
      <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl p-6 flex items-center gap-5">
        <div className="w-16 h-16 rounded-2xl bg-blue-500/20 flex items-center justify-center text-blue-400 text-2xl font-bold shrink-0">
          {user?.initials || user?.username?.substring(0, 2).toUpperCase()}
        </div>
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-white font-bold text-lg">{user?.name}</h2>
            <span className="px-2.5 py-0.5 text-xs bg-blue-500/20 text-blue-400 border border-blue-500/30 rounded-full font-medium">
              {user?.role}
            </span>
          </div>
          <p className="text-slate-500 text-sm font-mono">@{user?.username}</p>
        </div>
      </div>

      {error && (
        <div className="flex items-center gap-2.5 p-3.5 bg-red-500/10 border border-red-500/20 rounded-xl">
          <AlertCircle className="w-4 h-4 text-red-400 shrink-0" />
          <p className="text-red-300 text-xs">{error}</p>
        </div>
      )}

      {saved && (
        <div className="flex items-center gap-2.5 p-3.5 bg-green-500/10 border border-green-500/20 rounded-xl">
          <CheckCircle2 className="w-4 h-4 text-green-400 shrink-0" />
          <p className="text-green-300 text-xs">Perfil actualizado correctamente en la base de datos.</p>
        </div>
      )}

      {/* Edit form */}
      <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl p-6">
        <h3 className="text-white font-semibold text-sm mb-5">Información de Cuenta</h3>
        <form onSubmit={handleSubmit} className="space-y-4">
          {[
            { name: 'name', label: 'Nombre completo', icon: User, type: 'text' },
            { name: 'email', label: 'Correo electrónico', icon: Mail, type: 'email' },
            { name: 'department', label: 'Departamento', icon: Building2, type: 'text' },
          ].map(({ name, label, icon: Icon, type }) => (
            <label key={name} className="block">
              <span className="text-slate-400 text-xs font-medium mb-1.5 flex items-center gap-1.5">
                <Icon className="w-3 h-3" />
                {label}
              </span>
              <input
                type={type}
                name={name}
                value={form[name]}
                onChange={handleChange}
                required
                className="w-full bg-slate-900 border border-slate-700 rounded-xl px-3.5 py-2.5 text-white text-sm focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30 transition-all"
              />
            </label>
          ))}

          <div className="pt-2 border-t border-slate-700">
            <h4 className="text-slate-400 text-xs font-medium mb-3 flex items-center gap-1.5">
              <KeyRound className="w-3 h-3" />
              Actualizar contraseña (opcional)
            </h4>
            <label className="block mb-3">
              <span className="text-slate-500 text-xs block mb-1.5">Nueva contraseña (dejar en blanco para no cambiar)</span>
              <input
                type="password"
                name="password"
                value={form.password}
                onChange={handleChange}
                placeholder="••••••••"
                className="w-full bg-slate-900 border border-slate-700 rounded-xl px-3.5 py-2.5 text-white text-sm placeholder-slate-700 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30 transition-all"
              />
            </label>
          </div>

          <button
            type="submit"
            disabled={loading}
            className={`w-full flex items-center justify-center gap-2 py-3 rounded-xl text-sm font-semibold transition-all ${
              saved
                ? 'bg-green-600 text-white'
                : 'bg-blue-600 hover:bg-blue-500 text-white shadow-lg shadow-blue-900/30'
            }`}
          >
            {loading && <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />}
            <Save className="w-4 h-4" />
            {loading ? 'Guardando...' : saved ? 'Guardado' : 'Guardar cambios'}
          </button>
        </form>
      </div>
    </div>
  )
}

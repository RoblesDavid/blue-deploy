import { useState } from 'react'
import { Save, Info, User, Mail, Building2, KeyRound, Calendar } from 'lucide-react'
import { useAuth } from '../../auth/AuthContext'

export default function MyProfile() {
  const { user } = useAuth()
  const [form, setForm] = useState({
    name: user?.name ?? '',
    email: user?.email ?? '',
    department: user?.department ?? '',
    currentPassword: '',
    newPassword: '',
  })
  const [saved, setSaved] = useState(false)

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))
    setSaved(false)
  }

  function handleSubmit(e) {
    e.preventDefault()
    // Mock: no real persistence
    setSaved(true)
    setTimeout(() => setSaved(false), 3000)
  }

  return (
    <div className="max-w-2xl space-y-5">
      {/* Avatar card */}
      <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl p-6 flex items-center gap-5">
        <div className="w-16 h-16 rounded-2xl bg-blue-500/20 flex items-center justify-center text-blue-400 text-2xl font-bold shrink-0">
          {user?.initials}
        </div>
        <div>
          <h2 className="text-white font-bold text-lg">{user?.name}</h2>
          <p className="text-slate-500 text-sm font-mono">@{user?.username}</p>
          <div className="flex items-center gap-1.5 mt-2 text-slate-500 text-xs">
            <Calendar className="w-3 h-3" />
            Registrado desde {new Date(user?.createdAt).toLocaleDateString('es-ES')}
          </div>
        </div>
      </div>

      {/* Demo notice */}
      <div className="flex items-start gap-2.5 p-3.5 bg-blue-500/10 border border-blue-500/20 rounded-xl">
        <Info className="w-4 h-4 text-blue-400 shrink-0 mt-0.5" />
        <p className="text-blue-300 text-xs leading-relaxed">
          Modo demo — los cambios no se persisten. La gestión real de usuarios se implementará en la práctica 2 con autenticación JWT.
        </p>
      </div>

      {/* Edit form */}
      <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl p-6">
        <h3 className="text-white font-semibold text-sm mb-5">Información personal</h3>
        <form onSubmit={handleSubmit} className="space-y-4">
          {[
            { name: 'name', label: 'Nombre completo', icon: User, type: 'text' },
            { name: 'email', label: 'Correo electrónico', icon: Mail, type: 'email' },
            { name: 'department', label: 'Departamento', icon: Building2, type: 'text' },
          ].map(({ name, label, icon: Icon, type }) => (
            <label key={name} className="block">
              <span className="text-slate-400 text-xs font-medium block mb-1.5 flex items-center gap-1.5">
                <Icon className="w-3 h-3" />
                {label}
              </span>
              <input
                type={type}
                name={name}
                value={form[name]}
                onChange={handleChange}
                className="w-full bg-slate-900 border border-slate-700 rounded-xl px-3.5 py-2.5 text-white text-sm focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30 transition-all"
              />
            </label>
          ))}

          <div className="pt-2 border-t border-slate-700">
            <h4 className="text-slate-400 text-xs font-medium mb-3 flex items-center gap-1.5">
              <KeyRound className="w-3 h-3" />
              Cambiar contraseña
            </h4>
            {[
              { name: 'currentPassword', label: 'Contraseña actual' },
              { name: 'newPassword', label: 'Nueva contraseña' },
            ].map(({ name, label }) => (
              <label key={name} className="block mb-3">
                <span className="text-slate-500 text-xs block mb-1.5">{label}</span>
                <input
                  type="password"
                  name={name}
                  value={form[name]}
                  onChange={handleChange}
                  placeholder="••••••••"
                  className="w-full bg-slate-900 border border-slate-700 rounded-xl px-3.5 py-2.5 text-white text-sm placeholder-slate-700 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30 transition-all"
                />
              </label>
            ))}
          </div>

          <button
            type="submit"
            className={`w-full flex items-center justify-center gap-2 py-3 rounded-xl text-sm font-semibold transition-all ${
              saved
                ? 'bg-green-600 text-white'
                : 'bg-blue-600 hover:bg-blue-500 text-white shadow-lg shadow-blue-900/30'
            }`}
          >
            <Save className="w-4 h-4" />
            {saved ? 'Cambios guardados (demo)' : 'Guardar cambios'}
          </button>
        </form>
      </div>
    </div>
  )
}

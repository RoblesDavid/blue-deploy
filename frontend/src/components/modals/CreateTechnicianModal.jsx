import { useState } from 'react'
import { X, UserPlus, AlertCircle } from 'lucide-react'
import usersApi from '../../api/usersApi'

export default function CreateTechnicianModal({ onClose, onSuccess }) {
  const [form, setForm] = useState({
    name: '',
    username: '',
    email: '',
    department: 'Blue Team - Soporte',
    password: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      await usersApi.createTechnician(form)
      if (onSuccess) onSuccess()
      onClose()
    } catch (err) {
      setError(err.message || 'Error al crear técnico')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />

      <div className="relative w-full max-w-md bg-slate-900 rounded-2xl border border-slate-700 shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between p-5 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-blue-500/20 flex items-center justify-center">
              <UserPlus className="w-4 h-4 text-blue-400" />
            </div>
            <h2 className="text-white font-semibold text-base">Nuevo técnico</h2>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-all"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {error && (
          <div className="mx-5 mt-4 flex items-center gap-2 p-3 bg-red-500/10 border border-red-500/20 rounded-xl">
            <AlertCircle className="w-4 h-4 text-red-400 shrink-0" />
            <p className="text-red-300 text-xs">{error}</p>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {[
            { name: 'name', label: 'Nombre completo', placeholder: 'Ej: María López', type: 'text' },
            { name: 'username', label: 'Usuario', placeholder: 'Ej: maria.lopez', type: 'text' },
            { name: 'email', label: 'Email', placeholder: 'Ej: maria.lopez@blue.local', type: 'email' },
            { name: 'department', label: 'Departamento', placeholder: 'Ej: Blue Team - Soporte', type: 'text' },
            { name: 'password', label: 'Contraseña inicial', placeholder: '••••••••', type: 'password' },
          ].map(({ name, label, placeholder, type }) => (
            <label key={name} className="block">
              <span className="text-slate-400 text-xs font-medium block mb-1.5">{label}</span>
              <input
                type={type}
                name={name}
                value={form[name]}
                onChange={handleChange}
                placeholder={placeholder}
                required
                className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3.5 py-2.5 text-white text-sm placeholder-slate-600 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30 transition-all"
              />
            </label>
          ))}

          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="flex-1 py-2.5 rounded-xl text-slate-400 hover:text-white bg-slate-800 hover:bg-slate-700 text-sm font-medium transition-all"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-white bg-blue-600 hover:bg-blue-500 disabled:opacity-60 text-sm font-medium transition-all shadow-lg shadow-blue-900/30"
            >
              {loading && <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />}
              {loading ? 'Guardando...' : 'Crear técnico'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

const COLOR_MAP = {
  blue:   'text-blue-400 bg-blue-500/10',
  green:  'text-green-400 bg-green-500/10',
  red:    'text-red-400 bg-red-500/10',
  yellow: 'text-yellow-400 bg-yellow-500/10',
  purple: 'text-purple-400 bg-purple-500/10',
  cyan:   'text-cyan-400 bg-cyan-500/10',
  orange: 'text-orange-400 bg-orange-500/10',
}

export default function StatCard({ title, value, icon: Icon, color = 'blue', subtitle }) {
  const iconCls = COLOR_MAP[color] ?? COLOR_MAP.blue

  return (
    <div className="bg-slate-800/60 rounded-2xl p-5 border border-slate-700/50 hover:border-slate-600 transition-colors">
      <div className="flex items-center justify-between mb-4">
        <p className="text-slate-400 text-sm font-medium">{title}</p>
        <div className={`w-9 h-9 rounded-xl flex items-center justify-center ${iconCls}`}>
          <Icon className="w-4 h-4" />
        </div>
      </div>
      <p className="text-white text-3xl font-bold">{value}</p>
      {subtitle && <p className="text-slate-500 text-xs mt-1.5">{subtitle}</p>}
    </div>
  )
}

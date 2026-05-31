import { createContext, useContext, useState } from 'react'
import { MOCK_USERS } from '../data/mockUsers'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('bluedeploy_user')
      return saved ? JSON.parse(saved) : null
    } catch {
      return null
    }
  })

  function login(username, password) {
    const found = MOCK_USERS.find(
      (u) => u.username === username && u.password === password
    )
    if (!found) return { success: false, error: 'Usuario o contraseña incorrectos' }

    const { password: _pw, ...safeUser } = found
    setUser(safeUser)
    localStorage.setItem('bluedeploy_user', JSON.stringify(safeUser))
    return { success: true, user: safeUser }
  }

  function logout() {
    setUser(null)
    localStorage.removeItem('bluedeploy_user')
  }

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth/AuthContext'
import ProtectedRoute from './auth/ProtectedRoute'
import Layout from './components/layout/Layout'
import LandingPage from './pages/LandingPage'
import Login from './pages/Login'
import AdminDashboard from './pages/admin/AdminDashboard'
import AllDeployments from './pages/admin/AllDeployments'
import TechnicianManagement from './pages/admin/TechnicianManagement'
import TechnicianDashboard from './pages/technician/TechnicianDashboard'
import MyDeployments from './pages/technician/MyDeployments'
import MyProfile from './pages/technician/MyProfile'

function RoleRedirect() {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/technician'} replace />
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/contact" element={<LandingPage />} />
          <Route path="/login" element={<Login />} />
          <Route path="/dashboard" element={<RoleRedirect />} />

          {/* Admin */}
          <Route
            path="/admin"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route index element={<AdminDashboard />} />
            <Route path="deployments" element={<AllDeployments />} />
            <Route path="technicians" element={<TechnicianManagement />} />
          </Route>

          {/* Technician */}
          <Route
            path="/technician"
            element={
              <ProtectedRoute requiredRole="TECHNICIAN">
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route index element={<TechnicianDashboard />} />
            <Route path="deployments" element={<MyDeployments />} />
            <Route path="profile" element={<MyProfile />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

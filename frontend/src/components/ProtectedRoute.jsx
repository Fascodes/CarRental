import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function ProtectedRoute() {
  const { token } = useAuth()
  return token ? <Outlet /> : <Navigate to="/login" replace />
}

export function AdminRoute() {
  const { token, role } = useAuth()
  if (!token) return <Navigate to="/login" replace />
  if (role !== 'ADMIN') return <Navigate to="/" replace />
  return <Outlet />
}

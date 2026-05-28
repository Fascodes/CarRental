import { createContext, useContext, useState, useCallback } from 'react'

function decodeToken(token) {
  try {
    const payload = token.split('.')[1]
    return JSON.parse(atob(payload))
  } catch {
    return null
  }
}

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('token'))

  const user = token ? decodeToken(token) : null
  const role = user?.role ?? null

  const loginCtx = useCallback((newToken) => {
    localStorage.setItem('token', newToken)
    setToken(newToken)
  }, [])

  const logoutCtx = useCallback(() => {
    localStorage.removeItem('token')
    setToken(null)
  }, [])

  return (
    <AuthContext.Provider value={{ token, user, role, login: loginCtx, logout: logoutCtx }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}

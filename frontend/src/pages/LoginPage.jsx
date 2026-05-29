import { useState } from 'react'
import { useNavigate, Navigate, Link, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { login } from '../api/auth'
import { parseApiError } from '../utils/apiError'

export default function LoginPage() {
  const { token, login: loginCtx } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (token) return <Navigate to="/" replace />

  const successMsg = location.state?.success

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await login(form)
      loginCtx(res.data.token)
      navigate('/')
    } catch (err) {
      setError(parseApiError(err, 'Błąd logowania'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <div className="form-card">
        <h2>Logowanie</h2>
        {successMsg && <div className="alert alert-success">{successMsg}</div>}
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email</label>
            <input name="email" type="email" value={form.email} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Hasło</label>
            <input name="password" type="password" value={form.password} onChange={handleChange} required />
          </div>
          <div className="btn-group">
            <button className="btn btn-primary" type="submit" disabled={loading}>
              {loading ? 'Logowanie...' : 'Zaloguj'}
            </button>
          </div>
        </form>
        <p style={{ marginTop: '1rem', fontSize: '0.85rem' }}>
          Nie masz konta? <Link to="/register" style={{ color: '#1a1a2e', fontWeight: 600 }}>Zarejestruj się</Link>
        </p>
      </div>
    </div>
  )
}

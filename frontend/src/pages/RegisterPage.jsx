import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { register } from '../api/auth'

export default function RegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ username: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register(form)
      navigate('/login', { state: { success: 'Rejestracja zakończona sukcesem. Możesz się teraz zalogować.' } })
    } catch (err) {
      setError(err.response?.data?.message ?? 'Błąd rejestracji')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <div className="form-card">
        <h2>Rejestracja</h2>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Nazwa użytkownika</label>
            <input name="username" value={form.username} onChange={handleChange} required />
          </div>
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
              {loading ? 'Rejestrowanie...' : 'Zarejestruj'}
            </button>
          </div>
        </form>
        <p style={{ marginTop: '1rem', fontSize: '0.85rem' }}>
          Masz już konto? <Link to="/login" style={{ color: '#1a1a2e', fontWeight: 600 }}>Zaloguj się</Link>
        </p>
      </div>
    </div>
  )
}

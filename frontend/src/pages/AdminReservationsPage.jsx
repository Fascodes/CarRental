import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAdminReservations } from '../api/reservations'
import { formatDate } from '../utils/date'

const STATUSES = ['', 'PENDING', 'RENTER_CONFIRMED', 'CONFIRMED', 'ACTIVE', 'CANCELLED', 'COMPLETED']

const statusClass = {
  PENDING: 'badge-pending',
  RENTER_CONFIRMED: 'badge-renter-confirmed',
  CONFIRMED: 'badge-confirmed',
  ACTIVE: 'badge-renting',
  CANCELLED: 'badge-cancelled',
  COMPLETED: 'badge-completed',
}

export default function AdminReservationsPage() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [status, setStatus] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [searched, setSearched] = useState(false)

  const handleSearch = async (e) => {
    e.preventDefault()
    if (!email) return
    setLoading(true)
    setError('')
    const params = { email }
    if (status) params.status = status
    try {
      const res = await getAdminReservations(params)
      setResults(res.data)
      setSearched(true)
    } catch {
      setError('Błąd podczas wyszukiwania')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <h1>Zarządzanie rezerwacjami</h1>

      <div className="filters" style={{ marginTop: '1rem' }}>
        <div className="form-group">
          <label>Email użytkownika *</label>
          <input
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="user@example.com"
            style={{ minWidth: 240 }}
          />
        </div>
        <div className="form-group">
          <label>Status</label>
          <select value={status} onChange={(e) => setStatus(e.target.value)}>
            {STATUSES.map((s) => <option key={s} value={s}>{s || '— wszystkie —'}</option>)}
          </select>
        </div>
        <button className="btn btn-primary" onClick={handleSearch} disabled={loading || !email}>
          {loading ? 'Szukanie...' : 'Szukaj'}
        </button>
      </div>

      {error && <div className="alert alert-error" style={{ marginTop: '1rem' }}>{error}</div>}

      {searched && (
        <div className="res-list">
          {results.length === 0 ? (
            <p style={{ color: '#888', marginTop: '1rem' }}>Brak wyników.</p>
          ) : results.map((r) => (
            <div
              key={r.id}
              className="res-item"
              onClick={() => navigate(`/admin/reservations/${r.id}`)}
            >
              <h4>{r.listingTitle}</h4>
              <div className="res-meta">
                {formatDate(r.dateStart)} – {formatDate(r.dateEnd)}
              </div>
              <div className="res-meta">Właściciel: {r.ownerUsername} · Najemca: {r.renterUsername}</div>
              <span className={`status-badge ${statusClass[r.status] ?? ''}`}>{r.status}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

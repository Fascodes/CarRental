import { useState, useEffect, useCallback } from 'react'
import { getOwnerReservations } from '../api/reservations'
import ReservationCard from '../components/ReservationCard'

const STATUSES = ['', 'PENDING', 'RENTER_CONFIRMED', 'CONFIRMED', 'ACTIVE', 'CANCELLED', 'COMPLETED']

export default function OwnerReservationsPage() {
  const [reservations, setReservations] = useState([])
  const [status, setStatus] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const fetchReservations = useCallback(() => {
    setLoading(true)
    const params = {}
    if (status) params.status = status
    getOwnerReservations(params)
      .then((res) => setReservations(res.data))
      .catch(() => setError('Błąd podczas ładowania rezerwacji'))
      .finally(() => setLoading(false))
  }, [status])

  useEffect(() => { fetchReservations() }, [fetchReservations])


  return (
    <div className="page">
      <div className="page-header">
        <h1>Rezerwacje (właściciel)</h1>
      </div>

      <div className="filters">
        <div className="form-group">
          <label>Status</label>
          <select value={status} onChange={(e) => setStatus(e.target.value)}>
            {STATUSES.map((s) => <option key={s} value={s}>{s || '— wszystkie —'}</option>)}
          </select>
        </div>
      </div>

      {error && <div className="alert alert-error" style={{ marginTop: '1rem' }}>{error}</div>}

      {loading ? (
        <div className="loading">Ładowanie...</div>
      ) : reservations.length === 0 ? (
        <p style={{ marginTop: '1rem', color: '#888' }}>Brak rezerwacji.</p>
      ) : (
        <div className="res-list">
          {reservations.map((r) => (
            <ReservationCard key={r.id} reservation={r} showRenter role="owner" />
          ))}
        </div>
      )}
    </div>
  )
}

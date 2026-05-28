import { useState, useEffect, useCallback } from 'react'
import { getRenterReservations, cancelReservation } from '../api/reservations'
import ReservationCard from '../components/ReservationCard'

const STATUSES = ['', 'PENDING', 'CONFIRMED', 'ACTIVE', 'CANCELLED', 'COMPLETED']

export default function RenterReservationsPage() {
  const [reservations, setReservations] = useState([])
  const [status, setStatus] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const fetchReservations = useCallback(() => {
    setLoading(true)
    const params = {}
    if (status) params.status = status
    getRenterReservations(params)
      .then((res) => setReservations(res.data))
      .catch(() => setError('Błąd podczas ładowania rezerwacji'))
      .finally(() => setLoading(false))
  }, [status])

  useEffect(() => { fetchReservations() }, [fetchReservations])

  const handleCancel = async (id, e) => {
    e.stopPropagation()
    if (!confirm('Czy na pewno chcesz anulować tę rezerwację?')) return
    try {
      await cancelReservation(id)
      fetchReservations()
    } catch {
      setError('Nie udało się anulować rezerwacji')
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Rezerwacje (najemca)</h1>
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
            <div key={r.id}>
              <ReservationCard reservation={r} showOwner />
              {(r.status === 'PENDING' || r.status === 'CONFIRMED') && (
                <div style={{ marginTop: '-0.25rem', marginBottom: '0.5rem', paddingLeft: '1rem' }}>
                  <button
                    className="btn btn-danger btn-sm"
                    onClick={(e) => handleCancel(r.id, e)}
                  >
                    Anuluj rezerwację
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

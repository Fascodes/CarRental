import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getReservation, confirmReservation, cancelReservation } from '../api/reservations'
import { useAuth } from '../context/AuthContext'
import { formatDate } from '../utils/date'

const statusClass = {
  PENDING: 'badge-pending',
  CONFIRMED: 'badge-confirmed',
  ACTIVE: 'badge-renting',
  CANCELLED: 'badge-cancelled',
  COMPLETED: 'badge-completed',
}

export default function ReservationDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [res, setRes] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionLoading, setActionLoading] = useState(false)

  const fetchRes = () => {
    getReservation(id)
      .then((r) => setRes(r.data))
      .catch(() => setError('Nie znaleziono rezerwacji'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchRes() }, [id])

  const isOwner = user?.sub === res?.ownerEmail
  const isRenter = user?.sub === res?.renterEmail

  const handleConfirm = async () => {
    setActionLoading(true)
    try {
      const r = await confirmReservation(id)
      setRes(r.data)
    } catch {
      setError('Nie udało się potwierdzić rezerwacji')
    } finally {
      setActionLoading(false)
    }
  }

  const handleCancel = async () => {
    if (!confirm('Czy na pewno chcesz anulować tę rezerwację?')) return
    setActionLoading(true)
    try {
      const r = await cancelReservation(id)
      setRes(r.data)
    } catch {
      setError('Nie udało się anulować rezerwacji')
    } finally {
      setActionLoading(false)
    }
  }

  if (loading) return <div className="loading">Ładowanie...</div>
  if (!res) return <div className="page"><div className="alert alert-error">{error}</div></div>

  return (
    <div className="page">
      <div className="detail-card">
        <h1>{res.listingTitle}</h1>
        <span className={`status-badge ${statusClass[res.status] ?? ''}`} style={{ marginBottom: '1rem', display: 'inline-block' }}>
          {res.status}
        </span>

        <div className="detail-grid">
          <div className="field"><label>Data od</label><p>{formatDate(res.dateStart)}</p></div>
          <div className="field"><label>Data do</label><p>{formatDate(res.dateEnd)}</p></div>
          <div className="field"><label>Właściciel</label><p>{res.ownerEmail}</p></div>
          <div className="field"><label>Najemca</label><p>{res.renterEmail}</p></div>
        </div>

        {error && <div className="alert alert-error" style={{ marginTop: '1rem' }}>{error}</div>}

        <div className="btn-group">
          {isOwner && res.status === 'PENDING' && (
            <button className="btn btn-success" onClick={handleConfirm} disabled={actionLoading}>
              Potwierdź
            </button>
          )}
          {isRenter && (res.status === 'PENDING' || res.status === 'CONFIRMED') && (
            <button className="btn btn-danger" onClick={handleCancel} disabled={actionLoading}>
              Anuluj rezerwację
            </button>
          )}
          <button className="btn btn-secondary" onClick={() => navigate(-1)}>Wróć</button>
        </div>
      </div>
    </div>
  )
}

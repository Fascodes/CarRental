import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import { getReservation, confirmReservation, ownerConfirmReservation, cancelReservation } from '../api/reservations'
import { formatDate } from '../utils/date'
import { parseApiError } from '../utils/apiError'

const statusClass = {
  PENDING: 'badge-pending',
  RENTER_CONFIRMED: 'badge-renter-confirmed',
  CONFIRMED: 'badge-confirmed',
  ACTIVE: 'badge-renting',
  CANCELLED: 'badge-cancelled',
  COMPLETED: 'badge-completed',
}

export default function ReservationDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { state } = useLocation()
  const role = state?.role  // 'owner' | 'renter' | undefined (direct URL)
  const isOwner = role === 'owner'
  const isRenter = role === 'renter'

  const [res, setRes] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionLoading, setActionLoading] = useState(false)

  useEffect(() => {
    getReservation(id)
      .then((r) => setRes(r.data))
      .catch(() => setError('Nie znaleziono rezerwacji'))
      .finally(() => setLoading(false))
  }, [id])

  const handle = (apiFn, errMsg) => async () => {
    setActionLoading(true)
    setError('')
    try {
      const r = await apiFn(id)
      setRes(r.data)
    } catch (err) {
      setError(parseApiError(err, errMsg))
    } finally {
      setActionLoading(false)
    }
  }

  const handleCancel = async () => {
    if (!confirm('Czy na pewno chcesz anulować tę rezerwację?')) return
    await handle(cancelReservation, 'Nie udało się anulować rezerwacji')()
  }

  if (loading) return <div className="loading">Ładowanie...</div>
  if (!res) return <div className="page"><div className="alert alert-error">{error}</div></div>

  const isPending = res.status === 'PENDING'
  const isRenterConfirmed = res.status === 'RENTER_CONFIRMED'
  const isConfirmed = res.status === 'CONFIRMED'
  const canCancel = isPending || isRenterConfirmed || isConfirmed

  // Gdy brak state (bezpośredni URL) – pokaż wszystkie przyciski pasujące do statusu;
  // backend odrzuci nieautoryzowane akcje przez 403
  const showRenterConfirm = (isRenter || !role) && isPending
  const showOwnerConfirm  = (isOwner  || !role) && isRenterConfirmed
  const showCancel        = (isRenter || !role) && canCancel

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
          <div className="field"><label>Właściciel</label><p>{res.ownerUsername}</p></div>
          <div className="field"><label>Najemca</label><p>{res.renterUsername}</p></div>
        </div>

        {error && <div className="alert alert-error" style={{ marginTop: '1rem' }}>{error}</div>}

        <div className="btn-group">
          {showRenterConfirm && (
            <button className="btn btn-success" onClick={handle(confirmReservation, 'Nie udało się potwierdzić rezerwacji')} disabled={actionLoading}>
              Potwierdź rezerwację
            </button>
          )}
          {showOwnerConfirm && (
            <button className="btn btn-success" onClick={handle(ownerConfirmReservation, 'Nie udało się zatwierdzić rezerwacji')} disabled={actionLoading}>
              Zatwierdź
            </button>
          )}
          {showCancel && (
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

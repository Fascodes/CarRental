import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getReservation, patchReservationAdmin } from '../api/reservations'
import { formatDate } from '../utils/date'

const STATUSES = ['PENDING', 'RENTER_CONFIRMED', 'CONFIRMED', 'ACTIVE', 'CANCELLED', 'COMPLETED']

const statusClass = {
  PENDING: 'badge-pending',
  RENTER_CONFIRMED: 'badge-renter-confirmed',
  CONFIRMED: 'badge-confirmed',
  ACTIVE: 'badge-renting',
  CANCELLED: 'badge-cancelled',
  COMPLETED: 'badge-completed',
}

export default function AdminReservationDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [res, setRes] = useState(null)
  const [newStatus, setNewStatus] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    getReservation(id)
      .then((r) => {
        setRes(r.data)
        setNewStatus(r.data.status)
      })
      .catch(() => setError('Nie znaleziono rezerwacji'))
      .finally(() => setLoading(false))
  }, [id])

  const handleSave = async () => {
    setSaving(true)
    setError('')
    setSaved(false)
    try {
      const r = await patchReservationAdmin(id, { status: newStatus })
      setRes(r.data)
      setSaved(true)
    } catch {
      setError('Nie udało się zmienić statusu')
    } finally {
      setSaving(false)
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
          <div className="field"><label>Właściciel</label><p>{res.ownerUsername}</p></div>
          <div className="field"><label>Najemca</label><p>{res.renterUsername}</p></div>
        </div>

        <div className="form-group" style={{ marginTop: '1.5rem', maxWidth: 260 }}>
          <label>Zmień status</label>
          <select value={newStatus} onChange={(e) => setNewStatus(e.target.value)}>
            {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>

        {error && <div className="alert alert-error">{error}</div>}
        {saved && <div className="alert alert-success">Status zaktualizowany!</div>}

        <div className="btn-group">
          <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
            {saving ? 'Zapisywanie...' : 'Zapisz status'}
          </button>
          <button className="btn btn-secondary" onClick={() => navigate(-1)}>Wróć</button>
        </div>
      </div>
    </div>
  )
}

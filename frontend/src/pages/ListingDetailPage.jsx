import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { getListing, deleteListing } from '../api/listings'
import { createReservation } from '../api/reservations'
import { useAuth } from '../context/AuthContext'
import { formatDate } from '../utils/date'

export default function ListingDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user, token } = useAuth()
  const [listing, setListing] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showReserve, setShowReserve] = useState(false)
  const [resForm, setResForm] = useState({ dateStart: '', dateEnd: '' })
  const [resError, setResError] = useState('')
  const [resLoading, setResLoading] = useState(false)
  const [resSuccess, setResSuccess] = useState(false)

  useEffect(() => {
    getListing(id)
      .then((res) => setListing(res.data))
      .catch(() => setError('Nie znaleziono listingu'))
      .finally(() => setLoading(false))
  }, [id])

  const isOwner = user?.sub === listing?.ownerEmail

  const handleDelete = async () => {
    if (!confirm('Czy na pewno chcesz usunąć ten listing?')) return
    try {
      await deleteListing(id)
      navigate('/panel/listings')
    } catch {
      setError('Nie udało się usunąć listingu')
    }
  }

  const handleReserve = async (e) => {
    e.preventDefault()
    setResError('')
    setResLoading(true)
    try {
      await createReservation({ listingId: Number(id), dateStart: resForm.dateStart, dateEnd: resForm.dateEnd })
      setResSuccess(true)
      setShowReserve(false)
    } catch (err) {
      setResError(err.response?.data?.message ?? 'Błąd podczas rezerwacji')
    } finally {
      setResLoading(false)
    }
  }

  if (loading) return <div className="loading">Ładowanie...</div>
  if (error) return <div className="page"><div className="alert alert-error">{error}</div></div>

  const statusClass = {
    ACTIVE: 'badge-active', INACTIVE: 'badge-inactive'
  }

  return (
    <div className="page">
      <div className="detail-card">
        <div className="img-placeholder">Brak zdjęcia</div>
        <h1>{listing.title}</h1>
        <div className="price-big">{listing.price} zł / dzień</div>

        {isOwner && (
          <span className={`status-badge ${statusClass[listing.status] ?? ''}`} style={{ marginBottom: '1rem', display: 'inline-block' }}>
            {listing.status}
          </span>
        )}

        <div className="detail-grid">
          <div className="field"><label>Marka</label><p>{listing.brand}</p></div>
          <div className="field"><label>Model</label><p>{listing.model}</p></div>
          <div className="field"><label>Rok</label><p>{listing.modelYear}</p></div>
          <div className="field"><label>Skrzynia</label><p>{listing.gearboxType}</p></div>
          <div className="field"><label>Miejsca</label><p>{listing.seatNumber}</p></div>
          <div className="field"><label>KM</label><p>{listing.horsePower}</p></div>
          <div className="field"><label>Spalanie</label><p>{listing.avgLiters} l/100km</p></div>
          <div className="field"><label>Właściciel</label><p>{listing.ownerEmail}</p></div>
        </div>

        {listing.body && (
          <p style={{ margin: '1rem 0', lineHeight: 1.6 }}>{listing.body}</p>
        )}

        {isOwner ? (
          <div className="btn-group">
            <Link to={`/listing/${id}/edit`} className="btn btn-outline">Edytuj</Link>
            <button className="btn btn-danger" onClick={handleDelete}>Usuń</button>
          </div>
        ) : token && (
          <div className="btn-group">
            <button className="btn btn-primary" onClick={() => setShowReserve((v) => !v)}>
              {showReserve ? 'Anuluj' : 'Zarezerwuj'}
            </button>
          </div>
        )}

        {resSuccess && <div className="alert alert-success" style={{ marginTop: '1rem' }}>Rezerwacja złożona pomyślnie!</div>}

        {showReserve && (
          <div className="reserve-form">
            <h3>Złóż rezerwację</h3>
            {resError && <div className="alert alert-error">{resError}</div>}
            <form onSubmit={handleReserve}>
              <div className="form-row">
                <div className="form-group">
                  <label>Data od</label>
                  <input
                    type="datetime-local"
                    value={resForm.dateStart}
                    onChange={(e) => setResForm({ ...resForm, dateStart: e.target.value })}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Data do</label>
                  <input
                    type="datetime-local"
                    value={resForm.dateEnd}
                    onChange={(e) => setResForm({ ...resForm, dateEnd: e.target.value })}
                    required
                  />
                </div>
              </div>
              <div className="btn-group">
                <button className="btn btn-success" type="submit" disabled={resLoading}>
                  {resLoading ? 'Wysyłanie...' : 'Potwierdź rezerwację'}
                </button>
              </div>
            </form>
          </div>
        )}
      </div>
    </div>
  )
}

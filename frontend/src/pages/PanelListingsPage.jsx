import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { getMyListings, deleteListing } from '../api/listings'

export default function PanelListingsPage() {
  const [listings, setListings] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    getMyListings()
      .then((res) => setListings(res.data))
      .catch(() => setError('Błąd podczas ładowania listingów'))
      .finally(() => setLoading(false))
  }, [])

  const handleDelete = async (id) => {
    if (!confirm('Czy na pewno chcesz usunąć ten listing?')) return
    try {
      await deleteListing(id)
      setListings((prev) => prev.filter((l) => l.id !== id))
    } catch {
      setError('Nie udało się usunąć listingu')
    }
  }

  const statusClass = { ACTIVE: 'badge-active', INACTIVE: 'badge-inactive' }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Moje listingi</h1>
        <Link to="/panel/listings/add" className="btn btn-primary">+ Dodaj listing</Link>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {loading ? (
        <div className="loading">Ładowanie...</div>
      ) : listings.length === 0 ? (
        <p style={{ color: '#888' }}>Nie masz jeszcze żadnych listingów.</p>
      ) : (
        <div className="res-list">
          {listings.map((l) => (
            <div key={l.id} className="res-item" style={{ cursor: 'default' }}>
              <h4>{l.title}</h4>
              <div className="res-meta">{l.brand} {l.model} ({l.modelYear}) · {l.price} zł/dzień</div>
              <span className={`status-badge ${statusClass[l.status] ?? ''}`}>{l.status}</span>
              <div className="btn-group">
                <button className="btn btn-outline btn-sm" onClick={() => navigate(`/listing/${l.id}/edit`)}>
                  Edytuj
                </button>
                <button className="btn btn-danger btn-sm" onClick={() => handleDelete(l.id)}>Usuń</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

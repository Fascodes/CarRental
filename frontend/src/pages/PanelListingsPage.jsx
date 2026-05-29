import { useState, useEffect, useCallback } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { getMyListings, deleteListing } from '../api/listings'

export default function PanelListingsPage() {
  const [listings, setListings] = useState([])
  const [statusFilter, setStatusFilter] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  const fetchListings = useCallback(() => {
    setLoading(true)
    const params = {}
    if (statusFilter) params.status = statusFilter
    getMyListings(params)
      .then((res) => setListings(res.data))
      .catch(() => setError('Błąd podczas ładowania listingów'))
      .finally(() => setLoading(false))
  }, [statusFilter])

  useEffect(() => { fetchListings() }, [fetchListings])

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

      <div className="filters">
        <div className="form-group">
          <label>Status</label>
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <option value="">— wszystkie —</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
          </select>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {loading ? (
        <div className="loading">Ładowanie...</div>
      ) : listings.length === 0 ? (
        <p style={{ color: '#888', marginTop: '1rem' }}>Nie masz jeszcze żadnych listingów.</p>
      ) : (
        <div className="res-list">
          {listings.map((l) => (
            <div key={l.id} className="res-item" style={{ cursor: 'default' }}>
              <h4>{l.title}</h4>
              <div className="res-meta">{l.brand} {l.model} ({l.modelYear}) · {l.price} zł/dzień · {l.localization}</div>
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

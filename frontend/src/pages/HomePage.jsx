import { useState, useEffect, useCallback } from 'react'
import { filterListings } from '../api/listings'
import ListingCard from '../components/ListingCard'

export default function HomePage() {
  const [listings, setListings] = useState([])
  const [totalPages, setTotalPages] = useState(0)
  const [page, setPage] = useState(0)
  const [filters, setFilters] = useState({ brand: '', priceMax: '' })
  const [applied, setApplied] = useState({ brand: '', priceMax: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const fetchListings = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const params = { page, size: 10 }
      if (applied.brand) params.brand = applied.brand
      if (applied.priceMax) params.priceMax = applied.priceMax
      const res = await filterListings(params)
      setListings(res.data.content)
      setTotalPages(res.data.totalPages)
    } catch {
      setError('Błąd podczas ładowania listingów')
    } finally {
      setLoading(false)
    }
  }, [page, applied])

  useEffect(() => { fetchListings() }, [fetchListings])

  const handleSearch = () => {
    setPage(0)
    setApplied({ ...filters })
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Dostępne samochody</h1>
      </div>

      <div className="filters">
        <div className="form-group">
          <label>Marka</label>
          <input
            value={filters.brand}
            onChange={(e) => setFilters({ ...filters, brand: e.target.value })}
            placeholder="np. Toyota"
          />
        </div>
        <div className="form-group">
          <label>Maks. cena (zł/dzień)</label>
          <input
            type="number"
            value={filters.priceMax}
            onChange={(e) => setFilters({ ...filters, priceMax: e.target.value })}
            placeholder="np. 200"
            min="0"
          />
        </div>
        <button className="btn btn-primary" onClick={handleSearch}>Szukaj</button>
      </div>

      {error && <div className="alert alert-error" style={{ marginTop: '1rem' }}>{error}</div>}

      {loading ? (
        <div className="loading">Ładowanie...</div>
      ) : (
        <>
          {listings.length === 0 ? (
            <p style={{ marginTop: '2rem', color: '#888' }}>Brak wyników.</p>
          ) : (
            <div className="card-grid">
              {listings.map((l) => <ListingCard key={l.id} listing={l} />)}
            </div>
          )}

          {totalPages > 1 && (
            <div className="pagination">
              <button
                className="btn btn-outline btn-sm"
                onClick={() => setPage((p) => p - 1)}
                disabled={page === 0}
              >
                ← Poprzednia
              </button>
              <span>Strona {page + 1} z {totalPages}</span>
              <button
                className="btn btn-outline btn-sm"
                onClick={() => setPage((p) => p + 1)}
                disabled={page >= totalPages - 1}
              >
                Następna →
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}

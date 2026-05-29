import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getListing, patchListing } from '../api/listings'
import { getMyCars } from '../api/cars'
import { parseApiError } from '../utils/apiError'

export default function ListingEditPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState({ title: '', price: '', body: '', status: '', carId: '' })
  const [cars, setCars] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    Promise.all([getListing(id), getMyCars()])
      .then(([listingRes, carsRes]) => {
        const l = listingRes.data
        setForm({ title: l.title, localization: l.localization ?? '', price: l.price, body: l.body ?? '', status: l.status, carId: '' })
        setCars(carsRes.data)
      })
      .catch(() => setError('Nie udało się załadować danych'))
      .finally(() => setLoading(false))
  }, [id])

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      const payload = {}
      if (form.title) payload.title = form.title
      if (form.localization) payload.localization = form.localization
      if (form.price) payload.price = Number(form.price)
      if (form.body) payload.body = form.body
      if (form.status) payload.status = form.status
      if (form.carId) payload.carId = Number(form.carId)
      await patchListing(id, payload)
      navigate(`/listing/${id}`)
    } catch (err) {
      setError(parseApiError(err, 'Błąd podczas zapisywania'))
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="loading">Ładowanie...</div>

  return (
    <div className="page">
      <div className="form-card">
        <h2>Edytuj listing</h2>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Tytuł</label>
            <input name="title" value={form.title} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Lokalizacja</label>
            <input name="localization" value={form.localization} onChange={handleChange} placeholder="np. Warszawa" />
          </div>
          <div className="form-group">
            <label>Cena (zł / dzień)</label>
            <input name="price" type="number" value={form.price} onChange={handleChange} min="0" required />
          </div>
          <div className="form-group">
            <label>Opis</label>
            <textarea name="body" value={form.body} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Status</label>
            <select name="status" value={form.status} onChange={handleChange}>
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
            </select>
          </div>
          <div className="form-group">
            <label>Zmień samochód (opcjonalne)</label>
            <select name="carId" value={form.carId} onChange={handleChange}>
              <option value="">— nie zmieniaj —</option>
              {cars.map((c) => (
                <option key={c.id} value={c.id}>{c.brand} {c.model} ({c.modelYear})</option>
              ))}
            </select>
          </div>
          <div className="btn-group">
            <button className="btn btn-primary" type="submit" disabled={saving}>
              {saving ? 'Zapisywanie...' : 'Zapisz zmiany'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>Anuluj</button>
          </div>
        </form>
      </div>
    </div>
  )
}

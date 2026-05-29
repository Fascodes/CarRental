import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { addListing } from '../api/listings'
import { getMyCars } from '../api/cars'
import { parseApiError } from '../utils/apiError'

export default function ListingAddPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ title: '', localization: '', price: '', body: '', carId: '', status: 'ACTIVE' })
  const [cars, setCars] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    getMyCars().then((res) => setCars(res.data)).catch(() => {})
  }, [])

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await addListing({ ...form, price: Number(form.price), carId: Number(form.carId) })
      navigate('/panel/listings')
    } catch (err) {
      setError(parseApiError(err, 'Błąd podczas dodawania listingu'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <div className="form-card">
        <h2>Dodaj listing</h2>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Tytuł *</label>
            <input name="title" value={form.title} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Lokalizacja *</label>
            <input name="localization" value={form.localization} onChange={handleChange} required placeholder="np. Warszawa" />
          </div>
          <div className="form-group">
            <label>Cena (zł / dzień) *</label>
            <input name="price" type="number" value={form.price} onChange={handleChange} required min="0" />
          </div>
          <div className="form-group">
            <label>Opis</label>
            <textarea name="body" value={form.body} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Status *</label>
            <select name="status" value={form.status} onChange={handleChange}>
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
            </select>
          </div>
          <div className="form-group">
            <label>Samochód *</label>
            <select name="carId" value={form.carId} onChange={handleChange} required>
              <option value="">— wybierz samochód —</option>
              {cars.map((c) => (
                <option key={c.id} value={c.id}>{c.brand} {c.model} ({c.modelYear})</option>
              ))}
            </select>
          </div>
          <div className="btn-group">
            <button className="btn btn-primary" type="submit" disabled={loading}>
              {loading ? 'Dodawanie...' : 'Dodaj listing'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>Anuluj</button>
          </div>
        </form>
      </div>
    </div>
  )
}

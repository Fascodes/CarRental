import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getCar, patchCar } from '../api/cars'
import { parseApiError } from '../utils/apiError'

export default function CarDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    getCar(id)
      .then((res) => {
        const c = res.data
        setForm({
          brand: c.brand ?? '',
          model: c.model ?? '',
          modelYear: c.modelYear ?? '',
          seatNumber: c.seatNumber ?? '',
          gearboxType: c.gearboxType ?? 'MANUAL',
          horsePower: c.horsePower ?? '',
          avgLiters: c.avgLiters ?? '',
          info: c.info ?? '',
        })
      })
      .catch(() => setError('Nie znaleziono samochodu'))
      .finally(() => setLoading(false))
  }, [id])

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    setSaved(false)
    try {
      await patchCar(id, {
        ...form,
        modelYear: Number(form.modelYear),
        seatNumber: Number(form.seatNumber),
        horsePower: Number(form.horsePower),
        avgLiters: parseFloat(form.avgLiters),
      })
      setSaved(true)
    } catch (err) {
      setError(parseApiError(err, 'Błąd podczas zapisywania'))
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="loading">Ładowanie...</div>
  if (!form) return <div className="page"><div className="alert alert-error">{error}</div></div>

  return (
    <div className="page">
      <div className="form-card" style={{ maxWidth: 600 }}>
        <h2>Szczegóły / Edycja samochodu</h2>
        {error && <div className="alert alert-error">{error}</div>}
        {saved && <div className="alert alert-success">Zmiany zapisane!</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label>Marka</label>
              <input name="brand" value={form.brand} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label>Model</label>
              <input name="model" value={form.model} onChange={handleChange} />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Rok produkcji</label>
              <input name="modelYear" type="number" value={form.modelYear} onChange={handleChange} min="1900" max="2100" />
            </div>
            <div className="form-group">
              <label>Liczba miejsc</label>
              <input name="seatNumber" type="number" value={form.seatNumber} onChange={handleChange} min="1" />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Skrzynia biegów</label>
              <select name="gearboxType" value={form.gearboxType} onChange={handleChange}>
                <option value="MANUAL">MANUAL</option>
                <option value="AUTOMATIC">AUTOMATIC</option>
              </select>
            </div>
            <div className="form-group">
              <label>Moc (KM)</label>
              <input name="horsePower" type="number" value={form.horsePower} onChange={handleChange} min="1" />
            </div>
          </div>
          <div className="form-group">
            <label>Spalanie (l/100km)</label>
            <input name="avgLiters" type="number" step="0.1" value={form.avgLiters} onChange={handleChange} min="0" />
          </div>
          <div className="form-group">
            <label>Dodatkowe informacje</label>
            <textarea name="info" value={form.info} onChange={handleChange} />
          </div>
          <div className="btn-group">
            <button className="btn btn-primary" type="submit" disabled={saving}>
              {saving ? 'Zapisywanie...' : 'Zapisz zmiany'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => navigate('/panel/cars')}>Wróć</button>
          </div>
        </form>
      </div>
    </div>
  )
}

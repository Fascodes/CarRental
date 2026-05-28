import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { addCar } from '../api/cars'

const INITIAL = {
  brand: '', model: '', modelYear: '', vin: '',
  seatNumber: '', gearboxType: 'MANUAL', horsePower: '', avgLiters: '', info: ''
}

export default function CarAddPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState(INITIAL)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await addCar({
        ...form,
        modelYear: Number(form.modelYear),
        seatNumber: Number(form.seatNumber),
        horsePower: Number(form.horsePower),
        avgLiters: parseFloat(form.avgLiters),
      })
      navigate('/panel/cars')
    } catch (err) {
      setError(err.response?.data?.message ?? 'Błąd podczas dodawania samochodu')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <div className="form-card" style={{ maxWidth: 600 }}>
        <h2>Dodaj samochód</h2>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label>Marka *</label>
              <input name="brand" value={form.brand} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label>Model *</label>
              <input name="model" value={form.model} onChange={handleChange} required />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Rok produkcji *</label>
              <input name="modelYear" type="number" value={form.modelYear} onChange={handleChange} required min="1900" max="2100" />
            </div>
            <div className="form-group">
              <label>VIN *</label>
              <input name="vin" value={form.vin} onChange={handleChange} required />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Liczba miejsc *</label>
              <input name="seatNumber" type="number" value={form.seatNumber} onChange={handleChange} required min="1" />
            </div>
            <div className="form-group">
              <label>Skrzynia biegów *</label>
              <select name="gearboxType" value={form.gearboxType} onChange={handleChange}>
                <option value="MANUAL">MANUAL</option>
                <option value="AUTOMATIC">AUTOMATIC</option>
              </select>
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Moc (KM) *</label>
              <input name="horsePower" type="number" value={form.horsePower} onChange={handleChange} required min="1" />
            </div>
            <div className="form-group">
              <label>Spalanie (l/100km) *</label>
              <input name="avgLiters" type="number" step="0.1" value={form.avgLiters} onChange={handleChange} required min="0" />
            </div>
          </div>
          <div className="form-group">
            <label>Dodatkowe informacje</label>
            <textarea name="info" value={form.info} onChange={handleChange} />
          </div>
          <div className="btn-group">
            <button className="btn btn-primary" type="submit" disabled={loading}>
              {loading ? 'Dodawanie...' : 'Dodaj samochód'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>Anuluj</button>
          </div>
        </form>
      </div>
    </div>
  )
}

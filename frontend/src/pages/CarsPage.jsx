import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { getMyCars, deleteCar } from '../api/cars'

export default function CarsPage() {
  const [cars, setCars] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  const fetchCars = () => {
    setLoading(true)
    getMyCars()
      .then((res) => setCars(res.data))
      .catch(() => setError('Błąd podczas ładowania samochodów'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchCars() }, [])

  const handleDelete = async (id, e) => {
    e.stopPropagation()
    if (!confirm('Czy na pewno chcesz usunąć ten samochód?')) return
    try {
      await deleteCar(id)
      setCars((prev) => prev.filter((c) => c.id !== id))
    } catch {
      setError('Nie udało się usunąć samochodu')
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Moje samochody</h1>
        <Link to="/panel/cars/add" className="btn btn-primary">+ Dodaj samochód</Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {loading ? (
        <div className="loading">Ładowanie...</div>
      ) : cars.length === 0 ? (
        <p style={{ color: '#888' }}>Nie masz jeszcze żadnych samochodów.</p>
      ) : (
        <div className="res-list">
          {cars.map((car) => (
            <div key={car.id} className="res-item" style={{ cursor: 'default' }}>
              <h4>{car.brand} {car.model} ({car.modelYear})</h4>
              <div className="res-meta">{car.gearboxType} · {car.seatNumber} miejsc · {car.horsePower} KM</div>
              <div className="btn-group">
                <button className="btn btn-outline btn-sm" onClick={() => navigate(`/panel/cars/${car.id}`)}>
                  Szczegóły / Edytuj
                </button>
                <button className="btn btn-danger btn-sm" onClick={(e) => handleDelete(car.id, e)}>Usuń</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

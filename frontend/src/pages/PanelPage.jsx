import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getMe } from '../api/user'

export default function PanelPage() {
  const [username, setUsername] = useState('')

  useEffect(() => {
    getMe().then((res) => setUsername(res.data.username)).catch(() => {})
  }, [])

  return (
    <div className="page">
      <div className="page-header">
        <h1>Panel użytkownika</h1>
        {username && <span style={{ color: '#666', fontSize: '0.95rem' }}>Zalogowany jako: <strong>{username}</strong></span>}
      </div>
      <div className="panel-links">
        <Link to="/panel/cars">Moje samochody</Link>
        <Link to="/panel/listings">Moje listingi</Link>
        <Link to="/panel/reservations/owner">Moje rezerwacje (właściciel)</Link>
        <Link to="/panel/reservations/renter">Moje rezerwacje (najemca)</Link>
      </div>
    </div>
  )
}

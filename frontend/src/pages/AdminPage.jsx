import { Link } from 'react-router-dom'

export default function AdminPage() {
  return (
    <div className="page">
      <h1>Panel administratora</h1>
      <div className="panel-links">
        <Link to="/admin/reservations">Zarządzanie rezerwacjami</Link>
      </div>
    </div>
  )
}

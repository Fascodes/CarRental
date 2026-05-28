import { useNavigate } from 'react-router-dom'
import { formatDate } from '../utils/date'

const statusClass = {
  PENDING: 'badge-pending',
  CONFIRMED: 'badge-confirmed',
  ACTIVE: 'badge-renting',
  CANCELLED: 'badge-cancelled',
  COMPLETED: 'badge-completed',
}

export default function ReservationCard({ reservation, showOwner, showRenter }) {
  const navigate = useNavigate()
  return (
    <div className="res-item" onClick={() => navigate(`/panel/reservations/${reservation.id}`)}>
      <h4>{reservation.listingTitle}</h4>
      <div className="res-meta">
        {formatDate(reservation.dateStart)} – {formatDate(reservation.dateEnd)}
      </div>
      {showRenter && <div className="res-meta">Najemca: {reservation.renterEmail}</div>}
      {showOwner && <div className="res-meta">Właściciel: {reservation.ownerEmail}</div>}
      <span className={`status-badge ${statusClass[reservation.status] ?? ''}`}>
        {reservation.status}
      </span>
    </div>
  )
}

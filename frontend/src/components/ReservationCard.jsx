import { useNavigate } from 'react-router-dom'
import { formatDate } from '../utils/date'

const statusClass = {
  PENDING: 'badge-pending',
  RENTER_CONFIRMED: 'badge-renter-confirmed',
  CONFIRMED: 'badge-confirmed',
  ACTIVE: 'badge-renting',
  CANCELLED: 'badge-cancelled',
  COMPLETED: 'badge-completed',
}

export default function ReservationCard({ reservation, showOwner, showRenter, role }) {
  const navigate = useNavigate()
  return (
    <div className="res-item" onClick={() => navigate(`/panel/reservations/${reservation.id}`, { state: { role } })}>
      <h4>{reservation.listingTitle}</h4>
      <div className="res-meta">
        {formatDate(reservation.dateStart)} – {formatDate(reservation.dateEnd)}
      </div>
      {showRenter && <div className="res-meta">Najemca: {reservation.renterUsername}</div>}
      {showOwner && <div className="res-meta">Właściciel: {reservation.ownerUsername}</div>}
      <span className={`status-badge ${statusClass[reservation.status] ?? ''}`}>
        {reservation.status}
      </span>
    </div>
  )
}

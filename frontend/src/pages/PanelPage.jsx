import { Link } from 'react-router-dom'

export default function PanelPage() {
  return (
    <div className="page">
      <h1>Panel użytkownika</h1>
      <div className="panel-links">
        <Link to="/panel/cars">Moje samochody</Link>
        <Link to="/panel/listings">Moje listingi</Link>
        <Link to="/panel/reservations/owner">Moje rezerwacje (właściciel)</Link>
        <Link to="/panel/reservations/renter">Moje rezerwacje (najemca)</Link>
      </div>
    </div>
  )
}

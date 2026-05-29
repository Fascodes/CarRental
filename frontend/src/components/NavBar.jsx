import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function NavBar() {
  const { token, role, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <nav className="navbar">
      <Link to="/" className="brand">CarRental</Link>
      {!token && (
        <>
          <Link to="/login">Logowanie</Link>
          <Link to="/register">Rejestracja</Link>
        </>
      )}
      {token && (
        <>
          <Link to="/">Strona główna</Link>
          <Link to="/panel">Panel</Link>
          {role === 'ADMIN' && <Link to="/admin">Admin</Link>}
          <button onClick={handleLogout}>Wyloguj</button>
        </>
      )}
    </nav>
  )
}

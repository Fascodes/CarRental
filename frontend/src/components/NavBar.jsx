import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getMyNotifications, markNotificationRead } from '../api/notifications'
import { formatDate } from '../utils/date'

function roleFromMessage(message) {
  // Backend sends deterministic strings:
  // owner: "...awaiting your confirmation."
  // renter: "...confirmed by the owner."
  if (typeof message === 'string' && message.includes('awaiting your confirmation')) {
    return 'owner'
  }
  return 'renter'
}

export default function NavBar() {
  const { token, role, logout } = useAuth()
  const navigate = useNavigate()

  const [notifications, setNotifications] = useState([])
  const [open, setOpen] = useState(false)
  const wrapperRef = useRef(null)

  const unreadCount = notifications.filter((n) => !n.read).length

  const fetchNotifications = () => {
    if (!token) return
    getMyNotifications()
      .then((res) => setNotifications(res.data))
      .catch(() => {})
  }

  // Fetch on mount and every 60s
  useEffect(() => {
    fetchNotifications()
    const interval = setInterval(fetchNotifications, 60_000)
    return () => clearInterval(interval)
  }, [token])

  // Close dropdown on outside click
  useEffect(() => {
    const handleClick = (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [])

  const handleBellClick = () => {
    setOpen((v) => !v)
    if (!open) fetchNotifications()
  }

  const handleNotifClick = async (notif) => {
    setOpen(false)
    if (!notif.read) {
      try {
        await markNotificationRead(notif.id)
        setNotifications((prev) =>
          prev.map((n) => (n.id === notif.id ? { ...n, read: true } : n))
        )
      } catch {}
    }
    const userRole = roleFromMessage(notif.message)
    navigate(userRole === 'owner' ? '/panel/reservations/owner' : '/panel/reservations/renter')
  }

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

          <div className="notif-wrapper" ref={wrapperRef}>
            <button className="notif-btn" onClick={handleBellClick} title="Powiadomienia">
              🔔
              {unreadCount > 0 && (
                <span className="notif-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
              )}
            </button>

            {open && (
              <div className="notif-dropdown">
                <div className="notif-header">Powiadomienia</div>
                {notifications.length === 0 ? (
                  <div className="notif-empty">Brak powiadomień</div>
                ) : (
                  notifications.map((n) => (
                    <div
                      key={n.id}
                      className={`notif-item${n.read ? '' : ' unread'}`}
                      onClick={() => handleNotifClick(n)}
                    >
                      <p>{n.message}</p>
                      <time>{formatDate(n.createdAt)}</time>
                    </div>
                  ))
                )}
              </div>
            )}
          </div>

          <button onClick={handleLogout}>Wyloguj</button>
        </>
      )}
    </nav>
  )
}

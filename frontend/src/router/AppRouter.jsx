import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import NavBar from '../components/NavBar'
import { ProtectedRoute, AdminRoute } from '../components/ProtectedRoute'

import LoginPage from '../pages/LoginPage'
import RegisterPage from '../pages/RegisterPage'
import HomePage from '../pages/HomePage'
import ListingDetailPage from '../pages/ListingDetailPage'
import ListingEditPage from '../pages/ListingEditPage'
import PanelPage from '../pages/PanelPage'
import CarsPage from '../pages/CarsPage'
import CarAddPage from '../pages/CarAddPage'
import CarDetailPage from '../pages/CarDetailPage'
import PanelListingsPage from '../pages/PanelListingsPage'
import ListingAddPage from '../pages/ListingAddPage'
import OwnerReservationsPage from '../pages/OwnerReservationsPage'
import RenterReservationsPage from '../pages/RenterReservationsPage'
import ReservationDetailPage from '../pages/ReservationDetailPage'
import AdminPage from '../pages/AdminPage'
import AdminReservationsPage from '../pages/AdminReservationsPage'
import AdminReservationDetailPage from '../pages/AdminReservationDetailPage'
import AdminCarDetailPage from '../pages/AdminCarDetailPage'

export default function AppRouter() {
  return (
    <BrowserRouter>
      <NavBar />
      <Routes>
        {/* Public */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Protected */}
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/listing/:id" element={<ListingDetailPage />} />
          <Route path="/listing/:id/edit" element={<ListingEditPage />} />
          <Route path="/panel" element={<PanelPage />} />
          <Route path="/panel/cars" element={<CarsPage />} />
          <Route path="/panel/cars/add" element={<CarAddPage />} />
          <Route path="/panel/cars/:id" element={<CarDetailPage />} />
          <Route path="/panel/listings" element={<PanelListingsPage />} />
          <Route path="/panel/listings/add" element={<ListingAddPage />} />
          <Route path="/panel/reservations/owner" element={<OwnerReservationsPage />} />
          <Route path="/panel/reservations/renter" element={<RenterReservationsPage />} />
          <Route path="/panel/reservations/:id" element={<ReservationDetailPage />} />
        </Route>

        {/* Admin */}
        <Route element={<AdminRoute />}>
          <Route path="/admin" element={<AdminPage />} />
          <Route path="/admin/reservations" element={<AdminReservationsPage />} />
          <Route path="/admin/reservations/:id" element={<AdminReservationDetailPage />} />
          <Route path="/admin/cars/:id" element={<AdminCarDetailPage />} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

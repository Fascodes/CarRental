import api from './axiosInstance'

export const createReservation = (data) => api.post('/api/reservation', data)
export const confirmReservation = (id) => api.post(`/api/reservation/${id}/confirm`)
export const patchReservationAdmin = (id, data) => api.patch(`/api/reservation/${id}/admin`, data)
export const cancelReservation = (id) => api.patch(`/api/reservation/cancel/${id}`)
export const getOwnerReservations = (params) => api.get('/api/reservation/my/owner', { params })
export const getRenterReservations = (params) => api.get('/api/reservation/my/renter', { params })
export const getReservation = (id) => api.get(`/api/reservation/${id}`)
export const getAdminReservations = (params) => api.get('/api/reservation/admin', { params })

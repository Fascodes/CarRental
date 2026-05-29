import api from './axiosInstance'

export const getMyNotifications = () => api.get('/api/notification/my')
export const markNotificationRead = (id) => api.patch(`/api/notification/${id}/read`)

import api from './axiosInstance'

export const getMe = () => api.get('/api/user/me')

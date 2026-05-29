import api from './axiosInstance'

export const addCar = (data) => api.post('/api/car/add', data)
export const deleteCar = (id) => api.delete(`/api/car/${id}`)
export const getCar = (id) => api.get(`/api/car/${id}`)
export const getMyCars = () => api.get('/api/car/my')
export const patchCar = (id, data) => api.patch(`/api/car/${id}`, data)
export const patchCarAdmin = (id, data) => api.patch(`/api/car/${id}/admin`, data)

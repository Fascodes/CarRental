import api from './axiosInstance'

export const addListing = (data) => api.post('/api/listing/add', data)
export const deleteListing = (id) => api.delete(`/api/listing/${id}`)
export const filterListings = (params) => api.get('/api/listing/filter', { params })
export const getListing = (id) => api.get(`/api/listing/${id}`)
export const getMyListings = () => api.get('/api/listing/my')
export const patchListing = (id, data) => api.patch(`/api/listing/${id}`, data)

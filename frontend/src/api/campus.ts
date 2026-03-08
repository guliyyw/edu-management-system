import api from './index'

export interface Campus {
  id: number
  name: string
  address: string
  status: string
  createdAt: string
}

export const campusApi = {
  getAll: () => api.get('/campuses'),
  getActive: () => api.get('/campuses/active'),
  getById: (id: number) => api.get(`/campuses/${id}`),
  create: (data: Partial<Campus>) => api.post('/campuses', data),
  update: (id: number, data: Partial<Campus>) => api.put(`/campuses/${id}`, data),
  delete: (id: number) => api.delete(`/campuses/${id}`)
}

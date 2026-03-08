import api from './index'

export interface Teacher {
  id: number
  name: string
  phone: string
  campuses: any[]
  status: string
  createdAt: string
}

export const teacherApi = {
  getAll: () => api.get('/teachers'),
  getActive: () => api.get('/teachers/active'),
  getById: (id: number) => api.get(`/teachers/${id}`),
  getByCampus: (campusId: number) => api.get(`/teachers/campus/${campusId}`),
  create: (data: Partial<Teacher>) => api.post('/teachers', data),
  update: (id: number, data: Partial<Teacher>) => api.put(`/teachers/${id}`, data),
  delete: (id: number) => api.delete(`/teachers/${id}`)
}

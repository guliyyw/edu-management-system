import api from './index'

export interface Course {
  id: number
  name: string
  type: string
  unitPrice: number
  trialPrice: number
  status: string
  createdAt: string
}

export const courseApi = {
  getAll: () => api.get('/courses'),
  getActive: () => api.get('/courses/active'),
  getById: (id: number) => api.get(`/courses/${id}`),
  create: (data: Partial<Course>) => api.post('/courses', data),
  update: (id: number, data: Partial<Course>) => api.put(`/courses/${id}`, data),
  delete: (id: number) => api.delete(`/courses/${id}`)
}

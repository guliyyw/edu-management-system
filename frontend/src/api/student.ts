import api from './index'

export interface Student {
  id: number
  name: string
  parentName: string
  parentPhone: string
  status: string
  createdAt: string
}

export const studentApi = {
  getAll: () => api.get('/students'),
  getById: (id: number) => api.get(`/students/${id}`),
  getClasses: (id: number) => api.get(`/students/${id}/classes`),
  search: (keyword: string) => api.get(`/students/search?keyword=${keyword}`),
  create: (data: Partial<Student>) => api.post('/students', data),
  update: (id: number, data: Partial<Student>) => api.put(`/students/${id}`, data),
  delete: (id: number) => api.delete(`/students/${id}`)
}

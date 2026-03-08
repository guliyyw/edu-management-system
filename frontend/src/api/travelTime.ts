import api from './index'

export interface TravelTime {
  id: number
  teacher: any
  fromCampus: any
  toCampus: any
  travelMinutes: number
  effectiveDate: string
  createdAt: string
}

export const travelTimeApi = {
  getById: (id: number) => api.get(`/travel-times/${id}`),
  getByTeacher: (teacherId: number) => api.get(`/travel-times/teacher/${teacherId}`),
  getEffective: (teacherId: number, fromCampusId: number, toCampusId: number, date: string) => 
    api.get(`/travel-times/teacher/${teacherId}/effective?fromCampusId=${fromCampusId}&toCampusId=${toCampusId}&date=${date}`),
  create: (data: Partial<TravelTime>) => api.post('/travel-times', data),
  update: (id: number, data: Partial<TravelTime>) => api.put(`/travel-times/${id}`, data),
  delete: (id: number) => api.delete(`/travel-times/${id}`)
}

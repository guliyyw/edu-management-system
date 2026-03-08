import api from './index'

export interface Class {
  id: number
  course: any
  teacher: any
  campus: any
  classroom: string
  dayOfWeek: number
  startTime: string
  endTime: string
  status: string
  students: any[]
  createdAt: string
}

export const classApi = {
  getAll: () => api.get('/classes'),
  getById: (id: number) => api.get(`/classes/${id}`),
  getByTeacher: (teacherId: number) => api.get(`/classes/teacher/${teacherId}`),
  getByCampus: (campusId: number) => api.get(`/classes/campus/${campusId}`),
  getByStudent: (studentId: number) => api.get(`/classes/student/${studentId}`),
  create: (data: Partial<Class>) => api.post('/classes', data),
  update: (id: number, data: Partial<Class>) => api.put(`/classes/${id}`, data),
  delete: (id: number) => api.delete(`/classes/${id}`),
  addStudent: (classId: number, studentId: number, isTrial: boolean) => 
    api.post(`/classes/${classId}/students/${studentId}?isTrial=${isTrial}`),
  removeStudent: (classId: number, studentId: number) => 
    api.delete(`/classes/${classId}/students/${studentId}`),
  convertTrial: (classId: number, studentId: number) => 
    api.post(`/classes/${classId}/students/${studentId}/convert`)
}

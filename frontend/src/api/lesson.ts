import api from './index'

export interface Lesson {
  id: number
  classInfo: any
  date: string
  startTime: string
  endTime: string
  classroom: string
  status: string
  attendances: any[]
  createdAt: string
}

export const lessonApi = {
  getById: (id: number) => api.get(`/lessons/${id}`),
  getByClass: (classId: number) => api.get(`/lessons/class/${classId}`),
  getByTeacherAndDate: (teacherId: number, date: string) => 
    api.get(`/lessons/teacher/${teacherId}?date=${date}`),
  getByTeacherAndRange: (teacherId: number, startDate: string, endDate: string) => 
    api.get(`/lessons/teacher/${teacherId}/range?startDate=${startDate}&endDate=${endDate}`),
  getByCampusAndDate: (campusId: number, date: string) => 
    api.get(`/lessons/campus/${campusId}?date=${date}`),
  create: (data: Partial<Lesson>) => api.post('/lessons', data),
  update: (id: number, data: Partial<Lesson>) => api.put(`/lessons/${id}`, data),
  delete: (id: number) => api.delete(`/lessons/${id}`),
  generate: (classId: number, startDate: string, endDate: string) => 
    api.post(`/lessons/generate?classId=${classId}&startDate=${startDate}&endDate=${endDate}`),
  markAttendance: (lessonId: number, studentId: number, status: string, remark?: string) => 
    api.post(`/lessons/${lessonId}/attendance/${studentId}?status=${status}&remark=${remark || ''}`),
  modifyAttendance: (lessonId: number, studentId: number, status: string, remark: string, modifyReason: string) => 
    api.post(`/lessons/${lessonId}/attendance/${studentId}/modify?status=${status}&remark=${remark}&modifyReason=${modifyReason}`)
}

import api from './index'

export const debugApi = {
  clearLessons: () => api.delete('/debug/lessons'),
  clearClasses: () => api.delete('/debug/classes'),
  clearStudents: () => api.delete('/debug/students'),
  clearTeachers: () => api.delete('/debug/teachers'),
  clearAll: () => api.delete('/debug/all')
}

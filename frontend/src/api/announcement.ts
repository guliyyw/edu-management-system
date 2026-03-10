import api from './index'

export interface Announcement {
  id: number
  title: string
  content: string
  type: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  isPinned: boolean
  isActive: boolean
  publishTime: string
  expireTime: string
  createdBy: number
  createdByName: string
  createdAt: string
  updatedAt: string
}

export const announcementApi = {
  getAll: () => api.get('/announcements'),
  getById: (id: number) => api.get(`/announcements/${id}`),
  getActive: () => api.get('/announcements/active'),
  getTop5: () => api.get('/announcements/top5'),
  create: (data: Partial<Announcement>) => api.post('/announcements', data),
  update: (id: number, data: Partial<Announcement>) => api.put(`/announcements/${id}`, data),
  delete: (id: number) => api.delete(`/announcements/${id}`)
}

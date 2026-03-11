import { get } from '../utils/request'

export interface Announcement {
  id: number
  title: string
  content: string
  publisher: string
  publishTime: string
  important: boolean
}

export const getAnnouncements = () => {
  return get<Announcement[]>('/announcements')
}

export const getAnnouncementDetail = (id: number) => {
  return get<Announcement>(`/announcements/${id}`)
}

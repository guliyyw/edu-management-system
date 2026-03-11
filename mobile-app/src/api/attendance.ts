import { get, post } from '../utils/request'

export interface AttendanceRecord {
  id: number
  lessonId: number
  courseName: string
  studentName: string
  status: 'PRESENT' | 'ABSENT' | 'LATE' | 'LEAVE'
  checkInTime?: string
  remark?: string
}

export const getMyAttendance = (params?: { startDate?: string; endDate?: string }) => {
  return get<AttendanceRecord[]>('/attendance/my', params)
}

export const checkIn = (lessonId: number) => {
  return post('/attendance/check-in', { lessonId })
}

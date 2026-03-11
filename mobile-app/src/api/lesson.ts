import { get, post, put, del } from '../utils/request'

export interface Lesson {
  id: number
  classInfo: {
    id: number
    course: {
      id: number
      name: string
      type: string
    }
    campus: {
      id: number
      name: string
    }
    teacherFee: number
    gradeLevel: string
  }
  date: string
  startTime: string
  endTime: string
  classroom: string
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  attendances: Attendance[]
}

export interface Attendance {
  id: number
  student: {
    id: number
    name: string
    parentPhone?: string
    gradeLevel?: string
  }
  status: 'PENDING' | 'PRESENT' | 'ABSENT' | 'LEAVE' | 'TRIAL'
  remark?: string
}

// 获取老师某日的课程
export const getTeacherLessonsByDate = (teacherId: number, date: string) => {
  return get<Lesson[]>(`/lessons/teacher/${teacherId}?date=${date}`)
}

// 获取老师某时间段的课程
export const getTeacherLessonsByRange = (teacherId: number, startDate: string, endDate: string) => {
  return get<Lesson[]>(`/lessons/teacher/${teacherId}/range?startDate=${startDate}&endDate=${endDate}`)
}

// 标记考勤
export const markAttendance = (lessonId: number, studentId: number, status: string, remark?: string) => {
  return post(`/lessons/${lessonId}/attendance/${studentId}?status=${status}&remark=${remark || ''}`)
}

// 修改考勤
export const modifyAttendance = (lessonId: number, studentId: number, status: string, remark: string, modifyReason: string) => {
  return post(`/lessons/${lessonId}/attendance/${studentId}/modify?status=${status}&remark=${remark}&modifyReason=${modifyReason}`)
}

// 批量签到
export const batchMarkAttendance = (lessonId: number, data: { studentId: number; status: string; remark?: string }[]) => {
  return post(`/lessons/${lessonId}/attendance/batch`, data)
}

import { get } from '../utils/request'

export interface Student {
  id: number
  name: string
  parentPhone?: string
  gradeLevel: string
  status: 'ACTIVE' | 'INACTIVE'
}

// 获取所有学生
export const getAllStudents = () => {
  return get<Student[]>('/students')
}

// 获取班级学生
export const getStudentsByClass = (classId: number) => {
  return get<Student[]>(`/students/class/${classId}`)
}

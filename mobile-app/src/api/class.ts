import { get } from '../utils/request'

export interface ClassInfo {
  id: number
  name: string
  course: {
    id: number
    name: string
    type: string
  }
  campus: {
    id: number
    name: string
  }
  teacher: {
    id: number
    name: string
  }
  students: any[]
  status: 'ACTIVE' | 'INACTIVE'
}

// 获取所有班级
export const getAllClasses = () => {
  return get<ClassInfo[]>('/classes')
}

// 获取老师负责的班级
export const getClassesByTeacher = (teacherId: number) => {
  return get<ClassInfo[]>(`/classes/teacher/${teacherId}`)
}

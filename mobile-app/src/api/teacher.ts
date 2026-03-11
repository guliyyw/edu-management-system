import { get } from '../utils/request'

export interface Teacher {
  id: number
  name: string
  phone: string
  email?: string
  status: 'ACTIVE' | 'INACTIVE'
}

// 获取所有老师
export const getAllTeachers = () => {
  return get<Teacher[]>('/teachers')
}

// 获取老师详情
export const getTeacherById = (id: number) => {
  return get<Teacher>(`/teachers/${id}`)
}

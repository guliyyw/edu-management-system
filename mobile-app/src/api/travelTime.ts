import { get, post, put, del } from '../utils/request'

export interface TravelTime {
  id: number
  fromCampus: {
    id: number
    name: string
  }
  toCampus: {
    id: number
    name: string
  }
  travelMinutes: number
  effectiveDate: string
}

export interface TravelTimeForm {
  id?: number
  fromCampusId: number
  toCampusId: number
  travelMinutes: number
  effectiveDate: string
}

// 获取老师的路程配置
export const getTeacherTravelTimes = (teacherId: number) => {
  return get<TravelTime[]>(`/travel-times/teacher/${teacherId}`)
}

// 创建路程配置
export const createTravelTime = (data: TravelTimeForm) => {
  return post<TravelTime>('/travel-times', data)
}

// 更新路程配置
export const updateTravelTime = (id: number, data: TravelTimeForm) => {
  return put<TravelTime>(`/travel-times/${id}`, data)
}

// 删除路程配置
export const deleteTravelTime = (id: number) => {
  return del(`/travel-times/${id}`)
}

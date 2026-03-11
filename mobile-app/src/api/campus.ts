import { get } from '../utils/request'

export interface Campus {
  id: number
  name: string
  address: string
  status: 'ACTIVE' | 'INACTIVE'
}

// 获取所有校区
export const getAllCampuses = () => {
  return get<Campus[]>('/campuses')
}

// 获取活跃校区
export const getActiveCampuses = () => {
  return get<Campus[]>('/campuses/active')
}

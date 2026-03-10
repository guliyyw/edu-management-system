import api from './index'

export interface BackupRecord {
  id: number
  fileName: string
  filePath: string
  fileSize: number
  fileSizeFormatted: string
  backupType: string
  description: string
  status: string
  errorMessage: string
  createdAt: string
  updatedAt: string
}

export interface BackupConfig {
  id: number
  maxBackups: number
  backupInterval: string
  backupTime: string
  enabled: boolean
  backupPath: string
  createdAt: string
  updatedAt: string
}

export interface DatabaseInfo {
  url: string
  path: string
  type: string
}

export const backupApi = {
  getAll: () => api.get('/backups'),
  create: (description?: string) => api.post(`/backups?description=${description || '手动备份'}`),
  delete: (id: number) => api.delete(`/backups/${id}`),
  getConfig: () => api.get('/backups/config'),
  updateConfig: (data: Partial<BackupConfig>) => api.put('/backups/config', data),
  restore: (id: number) => api.post(`/backups/${id}/restore`),
  download: (id: number) => api.get(`/backups/${id}/download`, { responseType: 'blob' }),
  import: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/backups/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  getDatabaseInfo: () => api.get('/backups/database-info')
}

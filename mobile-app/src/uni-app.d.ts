/// <reference types="@dcloudio/types" />

declare namespace UniNamespace {
  interface RequestOptions {
    url: string
    method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'OPTIONS' | 'HEAD' | 'TRACE' | 'CONNECT'
    data?: any
    header?: any
    timeout?: number
    dataType?: string
    responseType?: string
    sslVerify?: boolean
    withCredentials?: boolean
    firstIpv4?: boolean
    success?: (result: RequestSuccessCallbackResult) => void
    fail?: (result: GeneralCallbackResult) => void
    complete?: (result: any) => void
  }

  interface RequestSuccessCallbackResult {
    data: any
    statusCode: number
    header: any
    cookies: string[]
    errMsg: string
  }

  interface GeneralCallbackResult {
    errMsg: string
  }

  interface ShowToastOptions {
    title: string
    icon?: 'success' | 'loading' | 'none' | 'error'
    image?: string
    duration?: number
    mask?: boolean
    position?: 'top' | 'center' | 'bottom'
    success?: (result: any) => void
    fail?: (result: any) => void
    complete?: (result: any) => void
  }

  interface ShowModalOptions {
    title?: string
    content?: string
    showCancel?: boolean
    cancelText?: string
    cancelColor?: string
    confirmText?: string
    confirmColor?: string
    success?: (result: ShowModalRes) => void
    fail?: (result: any) => void
    complete?: (result: any) => void
  }

  interface ShowModalRes {
    confirm: boolean
    cancel: boolean
  }

  interface ShowLoadingOptions {
    title?: string
    mask?: boolean
    success?: (result: any) => void
    fail?: (result: any) => void
    complete?: (result: any) => void
  }

  interface NavigateToOptions {
    url: string
    animationType?: string
    animationDuration?: number
    events?: any
    success?: (result: any) => void
    fail?: (result: any) => void
    complete?: (result: any) => void
  }

  interface SwitchTabOptions {
    url: string
    success?: (result: any) => void
    fail?: (result: any) => void
    complete?: (result: any) => void
  }

  interface RedirectToOptions {
    url: string
    success?: (result: any) => void
    fail?: (result: any) => void
    complete?: (result: any) => void
  }

  interface ReLaunchOptions {
    url: string
    success?: (result: any) => void
    fail?: (result: any) => void
    complete?: (result: any) => void
  }

  interface StorageOptions {
    key: string
    data?: any
    success?: (result: any) => void
    fail?: (result: any) => void
    complete?: (result: any) => void
  }

  interface GetStorageOptions {
    key: string
    success?: (result: any) => void
    fail?: (result: any) => void
    complete?: (result: any) => void
  }

  interface RemoveStorageOptions {
    key: string
    success?: (result: any) => void
    fail?: (result: any) => void
    complete?: (result: any) => void
  }
}

declare const uni: {
  request(options: UniNamespace.RequestOptions): void
  showToast(options: UniNamespace.ShowToastOptions): void
  showModal(options: UniNamespace.ShowModalOptions): void
  showLoading(options: UniNamespace.ShowLoadingOptions): void
  hideLoading(): void
  navigateTo(options: UniNamespace.NavigateToOptions): void
  switchTab(options: UniNamespace.SwitchTabOptions): void
  redirectTo(options: UniNamespace.RedirectToOptions): void
  reLaunch(options: UniNamespace.ReLaunchOptions): void
  setStorageSync(key: string, data: any): void
  getStorageSync(key: string): any
  removeStorageSync(key: string): void
}

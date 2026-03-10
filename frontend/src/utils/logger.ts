type LogLevel = 'debug' | 'info' | 'warn' | 'error';

interface LogEntry {
  level: LogLevel;
  message: string;
  timestamp: string;
  data?: any;
}

class Logger {
  private static instance: Logger;
  private logBuffer: LogEntry[] = [];
  private readonly maxBufferSize = 100;
  private readonly isProduction = import.meta.env.PROD;

  private constructor() {
    this.setupErrorHandler();
  }

  static getInstance(): Logger {
    if (!Logger.instance) {
      Logger.instance = new Logger();
    }
    return Logger.instance;
  }

  private setupErrorHandler() {
    if (typeof window !== 'undefined') {
      window.onerror = (message, source, lineno, colno, error) => {
        this.error('Global Error:', { message, source, lineno, colno, error: error?.stack });
        return false;
      };

      window.addEventListener('unhandledrejection', (event) => {
        this.error('Unhandled Promise Rejection:', event.reason);
      });
    }
  }

  private addToBuffer(entry: LogEntry) {
    this.logBuffer.push(entry);
    if (this.logBuffer.length > this.maxBufferSize) {
      this.logBuffer.shift();
    }
  }

  private formatMessage(level: LogLevel, message: string, data?: any): LogEntry {
    return {
      level,
      message,
      timestamp: new Date().toISOString(),
      data
    };
  }

  private shouldLog(level: LogLevel): boolean {
    // 生产环境只输出 info 及以上级别的日志
    if (this.isProduction) {
      const levels: LogLevel[] = ['info', 'warn', 'error'];
      return levels.includes(level);
    }
    return true;
  }

  debug(message: string, ...data: any[]) {
    if (this.shouldLog('debug')) {
      const entry = this.formatMessage('debug', message, data);
      this.addToBuffer(entry);
      console.debug(`[${entry.timestamp}] [DEBUG]`, message, ...data);
    }
  }

  info(message: string, ...data: any[]) {
    if (this.shouldLog('info')) {
      const entry = this.formatMessage('info', message, data);
      this.addToBuffer(entry);
      console.info(`[${entry.timestamp}] [INFO]`, message, ...data);
    }
  }

  warn(message: string, ...data: any[]) {
    if (this.shouldLog('warn')) {
      const entry = this.formatMessage('warn', message, data);
      this.addToBuffer(entry);
      console.warn(`[${entry.timestamp}] [WARN]`, message, ...data);
    }
  }

  error(message: string, ...data: any[]) {
    if (this.shouldLog('error')) {
      const entry = this.formatMessage('error', message, data);
      this.addToBuffer(entry);
      console.error(`[${entry.timestamp}] [ERROR]`, message, ...data);
    }
  }

  // 获取日志缓冲区内容（用于上报）
  getLogs(): LogEntry[] {
    return [...this.logBuffer];
  }

  // 清空日志缓冲区
  clearLogs() {
    this.logBuffer = [];
  }
}

export const logger = Logger.getInstance();
export default logger;

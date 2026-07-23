/** 后端统一响应信封 */
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  traceId?: string
}

/** 分页查询参数（页码从 1 开始，默认每页 10） */
export interface PageQuery {
  page?: number
  size?: number
}

/** 统一分页结果 */
export interface PageResult<T> {
  page: number
  size: number
  total: number
  records: T[]
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
  userId: number
  username: string
  deptId: number
  dataScope: string
  roles: string[]
  permissions: string[]
}

export interface MenuTreeNode {
  id: number
  parentId: number
  type: 'DIR' | 'MENU' | 'BUTTON' | string
  name: string
  path?: string | null
  component?: string | null
  icon?: string | null
  perms?: string | null
  sort?: number
  visible?: boolean
  status?: boolean
  children?: MenuTreeNode[]
}

export interface CurrentUserView {
  userId: number
  username: string
  /** 当前访问令牌 jti */
  jti?: string
  nickname: string
  realName?: string
  mobile?: string
  email?: string
  gender?: string
  avatarFileId?: number
  avatarUrl?: string
  deptId: number
  deptName?: string
  posts?: string[]
  dataScope: string
  roles: string[]
  permissions: string[]
  /** 后端动态权限开关；开启时前端会在路由切换时刷新 /me */
  dynamicPermission?: boolean
  menus: MenuTreeNode[]
}

export interface UserDetailView {
  id: number
  username: string
  nickname: string
  realName?: string
  mobile?: string
  email?: string
  gender?: string
  avatarFileId?: number
  avatarUrl?: string
  deptId: number
  deptName?: string
  enabled: boolean
  postIds: number[]
  posts: string[]
  roleIds: number[]
  roles: string[]
  permissions: string[]
}

export interface PostView {
  id: number
  code: string
  name: string
  sort: number
  status: boolean
}

export interface DictTypeView {
  id: number
  code: string
  name: string
  remark?: string
  sort: number
  status: boolean
  dataCount?: number
}

export interface DictDataView {
  id: number
  typeCode: string
  label: string
  value: string
  sort: number
  cssClass?: string
  defaultFlag: boolean
  status: boolean
  remark?: string
}

export interface DictOption {
  label: string
  value: string
  defaultFlag?: boolean
  cssClass?: string
}

export interface ConfigView {
  id: number
  configKey: string
  configName: string
  configValue?: string
  remark?: string
  sort: number
  status: boolean
  builtin: boolean
}

export interface JobView {
  id: number
  jobName: string
  jobGroup: string
  invokeTarget: string
  jobParams?: string
  cronExpression: string
  misfirePolicy: number
  concurrent: boolean
  status: boolean
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface JobLogView {
  id: number
  jobId: number
  jobName: string
  invokeTarget: string
  jobParams?: string
  status: boolean
  message?: string
  startTime: string
  endTime?: string
  costMs?: number
}

export interface CronValidateView {
  valid: boolean
  message?: string
  nextFireTimes: string[]
}

export interface IpWhitelistView {
  id: number
  ipAddr: string
  remark?: string
  status: boolean
  createdAt?: string
  updatedAt?: string
}

export interface IpVisitItemView {
  ip: string
  count: number
}

export interface IpVisitTodayView {
  date: string
  total: number
  items: IpVisitItemView[]
}

export interface OnlineUserView {
  jti: string
  userId: number
  username: string
  deptId?: number
  deptName?: string
  ip?: string
  userAgent?: string
  loginTime: number
  expireAt: number
}

export interface NoticeView {
  id: number
  title: string
  content: string
  type: 'NOTICE' | 'ANNOUNCE' | string
  status: boolean
  publisherId?: number
  publisherName?: string
  publishTime?: string
  readFlag?: boolean
}

export interface RoleView {
  id: number
  code: string
  name: string
  dataScope: string
  status: boolean
  userCount: number
  menuIds: number[]
}

export interface DeptView {
  id: number
  parentId: number
  name: string
  sort: number
  ancestors?: string
  status: boolean
  userCount?: number
  children?: DeptView[]
}

export interface LoginLogView {
  id: number
  userId?: number | null
  username: string
  ip?: string
  userAgent?: string
  status: string
  message?: string
  traceId?: string
  loginTime: string
}

export interface OperLogView {
  id: number
  userId?: number | null
  username?: string
  module?: string
  action?: string
  method?: string
  requestUri?: string
  requestMethod?: string
  ip?: string
  status: string
  errorMsg?: string
  costMs?: number
  params?: string
  traceId?: string
  operTime: string
}

export interface MysqlOverviewView {
  version?: string
  schema?: string
  tableCount?: number
  totalRows?: number
  dataLength?: number
  indexLength?: number
  dataFree?: number
  characterSet?: string
  collation?: string
}

export interface MysqlTableView {
  name: string
  engine?: string
  tableRows?: number
  dataLength?: number
  indexLength?: number
  dataFree?: number
  collation?: string
  comment?: string
  createTime?: string
  updateTime?: string
}

export interface MysqlColumnView {
  name: string
  type?: string
  nullable?: string
  columnKey?: string
  defaultValue?: string
  extra?: string
  comment?: string
  ordinalPosition?: number
}

export interface MysqlIndexView {
  name: string
  unique: boolean
  indexType?: string
  columns?: string
  cardinality?: number
}

export interface MysqlTableDetailView {
  table: MysqlTableView
  columns: MysqlColumnView[]
  indexes: MysqlIndexView[]
  ddl?: string
}

export interface MysqlProcessView {
  id: number
  user?: string
  host?: string
  db?: string
  command?: string
  time?: number
  state?: string
  info?: string
}

export interface RedisInfoView {
  redisVersion?: string
  mode?: string
  os?: string
  uptimeInSeconds?: number
  connectedClients?: number
  usedMemory?: number
  usedMemoryHuman?: string
  maxMemory?: number
  maxMemoryHuman?: string
  totalKeys?: number
  instantaneousOpsPerSec?: number
  role?: string
  extras?: Record<string, string>
}

export interface RedisKeyView {
  key: string
  type: string
  ttlSeconds?: number
}

export interface RedisKeyDetailView {
  key: string
  type: string
  ttlSeconds?: number
  size?: number
  value?: string
  truncated?: boolean
}

export interface ServerRuntimeView {
  collectedAt?: string
  app: {
    name?: string
    version?: string
    activeProfiles?: string[]
    startTime?: string
    uptimeMs?: number
    javaHome?: string
    userDir?: string
    userTimezone?: string
    fileEncoding?: string
    virtualThreadsEnabled?: boolean
  }
  jvm: {
    name?: string
    version?: string
    vendor?: string
    runtimeName?: string
    vmName?: string
    pid?: string
    inputArguments?: string[]
    availableProcessors?: number
    threadCount?: number
    peakThreadCount?: number
    daemonThreadCount?: number
    loadedClassCount?: number
    unloadedClassCount?: number
    processCpuLoad?: number
    systemCpuLoad?: number
  }
  memory: {
    heapInit?: number
    heapUsed?: number
    heapCommitted?: number
    heapMax?: number
    nonHeapInit?: number
    nonHeapUsed?: number
    nonHeapCommitted?: number
    nonHeapMax?: number
    freeMemory?: number
    totalMemory?: number
    maxMemory?: number
  }
  os: {
    name?: string
    arch?: string
    version?: string
    availableProcessors?: number
    systemLoadAverage?: number
    totalMemorySize?: number
    freeMemorySize?: number
    hostName?: string
  }
  disks: Array<{
    path: string
    totalSpace?: number
    freeSpace?: number
    usableSpace?: number
  }>
  dataSource: {
    available?: boolean
    poolType?: string
    jdbcUrl?: string
    username?: string
    driverClassName?: string
    maximumPoolSize?: number
    minimumIdle?: number
    activeConnections?: number
    idleConnections?: number
    totalConnections?: number
    threadsAwaitingConnection?: number
    message?: string
  }
  redis: {
    available?: boolean
    pong?: string
    redisVersion?: string
    dbSize?: number
    usedMemoryHuman?: string
    message?: string
  }
  systemProperties: Record<string, string>
  environment: Record<string, string>
}

import type { CronValidateView, JobLogView, JobView, PageQuery, PageResult } from '@/types/api'
import { deleteData, getData, postData, putData } from '@/utils/request'

export interface JobWriteBody {
  jobName: string
  jobGroup?: string
  invokeTarget: string
  jobParams?: string
  cronExpression: string
  misfirePolicy: number
  concurrent: boolean
  status: boolean
  remark?: string
}

export function listJobs(params?: PageQuery & { keyword?: string; status?: boolean }): Promise<PageResult<JobView>> {
  return getData<PageResult<JobView>>('/system/jobs', params)
}

export function getJob(id: number): Promise<JobView> {
  return getData<JobView>(`/system/jobs/${id}`)
}

export function createJob(body: JobWriteBody): Promise<JobView> {
  return postData<JobView>('/system/jobs', body)
}

export function updateJob(id: number, body: JobWriteBody): Promise<JobView> {
  return putData<JobView>(`/system/jobs/${id}`, body)
}

export function changeJobStatus(id: number, status: boolean): Promise<JobView> {
  return putData<JobView>(`/system/jobs/${id}/status`, { status })
}

export function removeJob(id: number): Promise<void> {
  return deleteData(`/system/jobs/${id}`)
}

export function runJobOnce(id: number): Promise<void> {
  return postData(`/system/jobs/${id}/run`)
}

export function validateCron(cronExpression: string): Promise<CronValidateView> {
  return postData<CronValidateView>('/system/jobs/cron/validate', { cronExpression })
}

export function listJobLogs(id: number, params?: PageQuery): Promise<PageResult<JobLogView>> {
  return getData<PageResult<JobLogView>>(`/system/jobs/${id}/logs`, params)
}

export function clearJobLogs(id: number): Promise<void> {
  return deleteData(`/system/jobs/${id}/logs`)
}

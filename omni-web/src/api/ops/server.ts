import type { ServerRuntimeView } from '@/types/api'
import { getData } from '@/utils/request'

export function fetchServerRuntime(): Promise<ServerRuntimeView> {
  return getData<ServerRuntimeView>('/ops/server/runtime')
}

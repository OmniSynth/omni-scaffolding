import type { MenuTreeNode } from '@/types/api'
import { deleteData, getData, postData, putData } from '@/utils/request'

export function fetchMenuTree(): Promise<MenuTreeNode[]> {
  return getData<MenuTreeNode[]>('/system/menus/tree')
}

export function createMenu(body: {
  parentId: number
  type: string
  name: string
  path?: string
  component?: string
  icon?: string
  perms?: string
  sort: number
  visible: boolean
  status: boolean
}): Promise<MenuTreeNode> {
  return postData<MenuTreeNode>('/system/menus', body)
}

export function updateMenu(
  id: number,
  body: {
    parentId: number
    type: string
    name: string
    path?: string
    component?: string
    icon?: string
    perms?: string
    sort: number
    visible: boolean
    status: boolean
  },
): Promise<MenuTreeNode> {
  return putData<MenuTreeNode>(`/system/menus/${id}`, body)
}

export function removeMenu(id: number): Promise<void> {
  return deleteData(`/system/menus/${id}`)
}

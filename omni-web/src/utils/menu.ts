import type { MenuTreeNode } from '@/types/api'

export function joinMenuPath(parentPath: string | undefined | null, path: string | undefined | null): string {
  const child = (path || '').trim()
  if (!child) {
    return parentPath || ''
  }
  if (child.startsWith('/')) {
    return child
  }
  const base = (parentPath || '').replace(/\/$/, '')
  if (!base) {
    return `/${child}`
  }
  return `${base}/${child}`
}

export interface SidebarItem {
  id: number
  title: string
  path?: string
  icon?: string | null
  children?: SidebarItem[]
}

export function toSidebarItems(nodes: MenuTreeNode[], parentPath = ''): SidebarItem[] {
  const items: SidebarItem[] = []
  for (const node of nodes) {
    if (node.type === 'BUTTON') {
      continue
    }
    if (node.type === 'DIR') {
      const children = toSidebarItems(node.children || [], node.path || parentPath)
      if (children.length) {
        items.push({ id: node.id, title: node.name, icon: node.icon, children })
      }
      continue
    }
    if (node.type === 'MENU') {
      items.push({
        id: node.id,
        title: node.name,
        icon: node.icon,
        path: joinMenuPath(parentPath, node.path),
      })
    }
  }
  return items
}

export function collectMenuPaths(nodes: MenuTreeNode[], parentPath = ''): string[] {
  const paths: string[] = []
  for (const node of nodes) {
    if (node.type === 'DIR') {
      paths.push(...collectMenuPaths(node.children || [], node.path || parentPath))
    } else if (node.type === 'MENU') {
      paths.push(joinMenuPath(parentPath, node.path))
    }
  }
  return paths
}

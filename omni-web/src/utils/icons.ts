import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import type { Component } from 'vue'

/**
 * 侧栏 / 菜单管理常用的 Element Plus Icons 名称（与 @element-plus/icons-vue 导出一致）。
 */
export const MENU_ICON_OPTIONS: string[] = [
  'HomeFilled',
  'Menu',
  'Grid',
  'List',
  'Setting',
  'Tools',
  'Operation',
  'SetUp',
  'User',
  'UserFilled',
  'Avatar',
  'Lock',
  'Key',
  'OfficeBuilding',
  'Postcard',
  'Collection',
  'Ticket',
  'Tickets',
  'Document',
  'Folder',
  'FolderOpened',
  'Files',
  'Monitor',
  'Timer',
  'AlarmClock',
  'Cpu',
  'Coin',
  'Money',
  'Wallet',
  'ShoppingCart',
  'Goods',
  'Bell',
  'Message',
  'ChatDotRound',
  'DataAnalysis',
  'DataBoard',
  'PieChart',
  'Histogram',
  'TrendCharts',
  'Calendar',
  'Clock',
  'Search',
  'Edit',
  'Delete',
  'Plus',
  'Upload',
  'Download',
  'Link',
  'Share',
  'Star',
  'Flag',
  'Location',
  'Phone',
  'Iphone',
  'Platform',
  'Connection',
  'Cloudy',
  'Odometer',
]

const iconMap = ElementPlusIconsVue as Record<string, Component>

/** 按名称解析图标组件；未知名称返回 undefined。 */
export function resolveMenuIcon(name?: string | null): Component | undefined {
  if (!name) return undefined
  return iconMap[name]
}

/** 全局常量与前端侧配置（成员A 提供，后续可扩展）。 */

export const APP_NAME = 'CampusOverflow';

/** 列表默认分页大小，与后端 PageRequest 的默认值保持一致。 */
export const DEFAULT_PAGE_SIZE = 20;

/** 后端约定的业务错误码前缀，成员C 的拦截器按前缀分类提示。 */
export const ERROR_CODE_PREFIX = {
  auth: 'AUTH-',
  qa: 'QA-',
  identity: 'ID-',
  reputation: 'REP-',
  discovery: 'DIS-',
  system: 'SYS-',
} as const;

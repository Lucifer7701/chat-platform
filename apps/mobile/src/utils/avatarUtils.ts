import { API_BASE } from './api';

// 仅保留URL解析工具，默认头像交给Avatar组件渲染
export const getAvatarUrl = (avatar?: string | null): string | undefined => {
  if (!avatar) return undefined;
  if (avatar.startsWith('http')) return avatar;
  if (avatar.startsWith('/')) return `${API_BASE}${avatar}`;
  return avatar;
};

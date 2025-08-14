export const API_BASE = process.env.EXPO_PUBLIC_API_BASE ?? 'http://localhost:8080';

export type ApiResult<T> = {
  code: number;
  message: string;
  data: T;
};

export async function get<T>(path: string, token?: string): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    method: 'GET',
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

export async function post<T>(path: string, body: any, token?: string): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

export async function markAsRead(fromUserId: number, token: string): Promise<ApiResult<void>> {
  return post<ApiResult<void>>(`/api/chat/read/${fromUserId}`, {}, token);
}

export function buildWebSocketUrl(path: string): string {
  // 将 http(s)://host:port 转为 ws(s)://host:port 并拼接 path
  try {
    const url = new URL(API_BASE);
    const protocol = url.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${protocol}//${url.host}${path.startsWith('/') ? path : '/' + path}`;
  } catch {
    // 兜底：若 API_BASE 不是合法 URL，则使用相对 ws
    const isHttps = API_BASE.startsWith('https://');
    const host = API_BASE.replace(/^https?:\/\//, '');
    return `${isHttps ? 'wss' : 'ws'}://${host}${path.startsWith('/') ? path : '/' + path}`;
  }
}



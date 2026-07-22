import { apiFetch } from '../../api/client';

export type NotificationCategory = 'TIME_ENTRY' | 'ABSENCE' | 'SYSTEM';

export interface Notification {
  id: string;
  category: NotificationCategory;
  title: string;
  message: string;
  link: string;
  read: boolean;
  readAt: string | null;
  createdAt: string;
}

export interface NotificationPage {
  content: Notification[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export function fetchNotifications(page = 0) {
  return apiFetch<NotificationPage>(`/notifications?page=${page}`);
}

export function fetchUnreadCount() {
  return apiFetch<{ count: number }>('/notifications/unread-count');
}

export function markNotificationRead(id: string) {
  return apiFetch<Notification>(`/notifications/${id}/read`, { method: 'POST' });
}

export function markAllNotificationsRead() {
  return apiFetch<{ updated: number }>('/notifications/read-all', { method: 'POST' });
}
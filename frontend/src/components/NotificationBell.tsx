import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Spinner from './Spinner';
import Button from './Button';
import { useMarkAllNotificationsRead, useMarkNotificationRead, useNotifications, useUnreadCount } from '../features/notifications/hooks';
import type { Notification } from '../features/notifications/api';
import styles from './NotificationBell.module.css';

const CATEGORY_LABELS: Record<string, string> = {
  TIME_ENTRY: 'Registo de horas',
  ABSENCE: 'Ausência',
  SYSTEM: 'Sistema',
};

function NotificationBell() {
  const [open, setOpen] = useState(false);
  const [page, setPage] = useState(0);
  const containerRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  const { data: countData } = useUnreadCount();
  const { data: pageData, isLoading } = useNotifications(page);
  const markReadMutation = useMarkNotificationRead();
  const markAllMutation = useMarkAllNotificationsRead();

  useEffect(() => {
    if (!open) {
      return;
    }
    function handleClickOutside(event: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [open]);

  const unreadCount = countData?.count ?? 0;
  const notifications = pageData?.content ?? [];

  function openNotification(notification: Notification) {
    if (!notification.read) {
      markReadMutation.mutate(notification.id);
    }
    setOpen(false);
    navigate(notification.link);
  }

  return (
    <div className={styles.wrapper} ref={containerRef}>
      <button
        type="button"
        className={styles.bell}
        onClick={() => setOpen((current) => !current)}
        aria-label="Notificações"
      >
        <span className={styles.bellIcon}>🔔</span>
        {unreadCount > 0 && <span className={styles.badge}>{unreadCount > 9 ? '9+' : unreadCount}</span>}
      </button>

      {open && (
        <div className={styles.dropdown}>
          <div className={styles.dropdownHeader}>
            <span className={styles.dropdownTitle}>Notificações</span>
            {unreadCount > 0 && (
              <button type="button" className={styles.linkButton} onClick={() => markAllMutation.mutate()}>
                Marcar todas como lidas
              </button>
            )}
          </div>

          <div className={styles.list}>
            {isLoading && <Spinner />}
            {!isLoading && notifications.length === 0 && (
              <p className={styles.empty}>Sem notificações.</p>
            )}
            {!isLoading &&
              notifications.map((notification) => (
                <button
                  key={notification.id}
                  type="button"
                  className={`${styles.item} ${notification.read ? styles.itemRead : ''}`}
                  onClick={() => openNotification(notification)}
                >
                  <span className={styles.itemTitle}>
                    {!notification.read && <span className={styles.unreadDot} />}
                    {notification.title}
                  </span>
                  <span className={styles.itemMessage}>{notification.message}</span>
                  <span className={styles.itemMeta}>
                    {CATEGORY_LABELS[notification.category] ?? notification.category} ·{' '}
                    {new Date(notification.createdAt).toLocaleString('pt-PT')}
                  </span>
                </button>
              ))}
          </div>

          {pageData && pageData.totalPages > 1 && (
            <div className={styles.dropdownFooter}>
              {page > 0 && (
                <Button type="button" className={styles.footerButton} onClick={() => setPage((current) => current - 1)}>
                  ← anteriores
                </Button>
              )}
              {page < pageData.totalPages - 1 && (
                <Button type="button" className={styles.footerButton} onClick={() => setPage((current) => current + 1)}>
                  seguintes →
                </Button>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default NotificationBell;
import { useState } from 'react';
import Spinner from '../../components/Spinner';
import EmptyState from '../../components/EmptyState';
import { useAuth } from '../../auth/useAuth';
import { useTeams } from '../teams/hooks';
import { useDeleteUser, useRestoreUser, useUsers } from './hooks';
import type { User } from './api';
import styles from './UsersPage.module.css';

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: 'Ativo',
  INACTIVE: 'Inativo',
  LOCKED: 'Bloqueado',
};

const PAGE_SIZE = 20;

function UsersPage() {
  const { user: currentUser } = useAuth();
  const canManage = currentUser?.permissions.includes('user:write') ?? false;

  const [page, setPage] = useState(0);

  const { data: pageData, isLoading } = useUsers(page, PAGE_SIZE);
  const { data: teams } = useTeams();

  const deleteMutation = useDeleteUser();
  const restoreMutation = useRestoreUser();

  const users = pageData?.content ?? [];

  function handleDelete(user: User) {
    if (window.confirm(`Desativar o utilizador ${user.fullName}?`)) {
      deleteMutation.mutate(user.id);
    }
  }

  return (
    <div>
      <div className={styles.header}>
        <h2 className={styles.title}>Utilizadores</h2>
      </div>

      {isLoading && <Spinner />}

      {!isLoading && users.length === 0 && <EmptyState message="Sem utilizadores." />}

      {!isLoading && users.length > 0 && (
        <>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Nome</th>
                <th>Utilizador</th>
                <th>Email</th>
                <th>Departamento</th>
                <th>Perfis</th>
                <th>Estado</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>{user.fullName}</td>
                  <td>{user.username}</td>
                  <td>{user.email}</td>
                  <td>{user.teamId ? (teams ?? []).find((team) => team.id === user.teamId)?.name ?? '—' : '—'}</td>
                  <td>{user.roles.join(', ')}</td>
                  <td>
                    <span className={`${styles.statusBadge} ${user.status === 'ACTIVE' ? styles.statusActive : styles.statusInactive}`}>
                      {STATUS_LABELS[user.status] ?? user.status}
                    </span>
                  </td>
                  <td className={styles.actionsCell}>
                    {canManage && (
                      <>
                        {user.status === 'ACTIVE' ? (
                          <button type="button" className={styles.dangerLink} onClick={() => handleDelete(user)}>
                            Desativar
                          </button>
                        ) : (
                          <button type="button" className={styles.linkButton} onClick={() => restoreMutation.mutate(user.id)}>
                            Restaurar
                          </button>
                        )}
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {pageData && pageData.totalPages > 1 && (
              <div className={styles.pagination}>
                  <button
                      type="button"
                      className={styles.navButton}
                      disabled={page === 0}
                      onClick={() => setPage((current) => current - 1)}
                  >
                      ← anteriores
                  </button>
                  <span className={styles.pageLabel}>
                      Página {page + 1} de {pageData.totalPages}
                  </span>
                  <button
                      type="button"
                      className={styles.navButton}
                      disabled={page >= pageData.totalPages - 1}
                      onClick={() => setPage((current) => current + 1)}
                  >
                      seguintes →
                  </button>
              </div>
          )}
        </>
      )}
    </div>
  );
}

export default UsersPage;
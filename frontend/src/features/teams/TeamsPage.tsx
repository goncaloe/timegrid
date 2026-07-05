import Button from '../../components/Button';
import Spinner from '../../components/Spinner';
import { ApiError } from '../../api/client';
import { useDeleteTeam, useTeams } from './hooks';
import type { Team } from './api';
import styles from './TeamsPage.module.css';

function TeamsPage() {

  const { data: teams, isLoading, isError, error } = useTeams();
  const deleteMutation = useDeleteTeam();

  function handleDelete(team: Team) {
    if (window.confirm(`Eliminar o departamento ${team.name}?`)) {
      deleteMutation.mutate(team.id);
    }
  }

  return (
    <div>
      <div className={styles.header}>
        <h2 className={styles.title}>Departamentos</h2>
        
        <Button type="button" disabled>
          Novo departamento
        </Button>
      </div>

      {isLoading && <Spinner />}

      {isError && (error instanceof ApiError && error.problem.detail ? error.problem.detail : "<div>Não foi possível carregar os dados.</div>")}

      {!isLoading && !isError && (teams ?? []).length === 0 && <div className={styles.emtyState}>Sem departamentos nesta organização.</div>}

      {!isLoading && !isError && (teams ?? []).length > 0 && (
        <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Nome</th>
              <th>Descrição</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {(teams ?? []).map((team) => (
              <tr key={team.id}>
                <td>{team.name}</td>
                <td className={styles.notesCell}>{team.description}</td>
                <td className={styles.actionsCell}>
                  <div className={styles.actionsInner}>
                    <button type="button" disabled className={styles.linkButton}>
                      Editar
                    </button>
                    <button type="button" className={styles.dangerLink} onClick={() => handleDelete(team)}>
                      Eliminar
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        </div>
      )}

    </div>
  );
}

export default TeamsPage;
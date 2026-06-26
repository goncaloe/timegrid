# TimeGrid

## A ideia

Uma das empresas onde trabalhei tinha registo de entradas e saídas num terminal de picar o ponto. Cada colaborador também tinha acesso a uma área reservada na intranet para marcação de férias, depois sujeito a aprovação pela chefia do departamento.

Nunca conheci o software que estava por detrás disso, mas isso fez-me pensar como seria um software para organizar registos dos colaboradores para empresas de maior dimensão. Pensei em algumas ideias a implementar, com potencial de aplicar vários conceitos de programação e regras de negócio, e assim demonstrar as minhas capacidades como programador fullstack.

## O projeto

A minha proposta é desenvolver um software de gestão de presenças e ausências com registo de banco de horas, pedidos de férias, perfis de acesso, notificações, etc. O desafio é desenvolver o projeto sem usar agentes de IA diretamente.

## Funcionalidades

- **Multi-tenancy**: várias empresas numa só BD, mas cada uma só vê os seus dados. 
- **Registo de horas**: registo por dia de cada funcionário das horas trabalhadas. Validar sobreposições.
- **Ausências**: férias, doença, formação e assuntos pessoais, com contagem de dias úteis e cancelamento.
- **Banco de horas**: calcular a partir do saldo de cada dia, descontando fins de semana, feriados e ausências aprovadas.
- **Feriados**: os nacionais portugueses são calculados automaticamente (incluindo dias móveis como a Páscoa); cada empresa pode acrescentar os seus.
- **Perfis (Admin, Gestor, Colaborador)**, com permissões RBAC.

## Tecnologias

- Java 21
- Spring Boot 4
- Gradle
- PostgreSQL

## Como executar


Pre-requisitos: Java21, Node LTS, Docker

```sh
# 1. Subir a base de dados no docker:
docker compose up -d db mailpit

# 2. Backend em ambiente dev
./gradlew bootRun --args='--spring.profiles.active=dev'

# 3. Frontend, noutro terminal
cd frontend
npm install
npm run dev     # http://localhost:5173
```

## Especificações, ADRs e Outras Considerações

### 25 Junho

- Usar Gradle em vez de Maven: é uma ferramenta de build recente, rápida e flexível. Usar ficheiros groovy para a configuração. 
- Criar tabelas do PostgreSQL: organizations, users, roles, time_entries, absents, log_events.
- Usar UUID para chaves das tabelas: melhor segurança numa aplicação multi-tenant. Custo: mais espaço e índices mais lentos.
- Usar os campos `created_at` e `updated_at` do tipo timestamp nas tabelas relevantes.
- Usar soft-delete em vez de hard-delete, tendo o campo `deleted_at` a `NULL` para registos ativos. Custo: todas as queries JPA devem ficar com o filtro AndDeletedAtIsNull.
- Usar Flyway para gerir o schema da base de dados. Configurar o Hibernate com `ddl-auto: validate`
- Criar dados de demonstração para ambiente dev.
- Ficheiros java organizados por camada. A alternativa seria organizar por funcionalidade, mas espera-se que o projeto não cresça demasiado em número de classes.
- Enums guardados como texto: abordagem diferente do que fiz em projetos grandes que desenvolvi, mas aceitável para a dimensão deste projeto; previne os problemas de usar campos de estado numéricos quando alteramos a lista dos enums.
- Usar `record` para os DTOs.
- Usar Lombock para código boilerplate.

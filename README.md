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
- **Segurança**: Spring Security com JWT. CORS.
- **Perfis (Admin, Gestor, Colaborador)**, com permissões RBAC.

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Security 6 com JWT
- Flyway
- Gradle
- JUnit e Mockito
- PostgreSQL
- React 19
- TypeScript
- Vite

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

### 27 Junho

- Campo timezone nas empresas (tabela organizations).
- Departamentos das empresas (tabela teams): Para separar quem aprova registos de outros colaboradores.
- Users e Roles com 3 tipos de perfis: Administrador, Gestor e Colaborador.
- Implementar permissões RBAC que definem o que cada perfil pode fazer
- Utilizadores com role `EMPLOYEE` não têm permissões explicitas na tabela permissions, nem precisam: as ações que conseguem ver são as que não requerem permissões.
- Estender de `BaseEntity` para entidades com id e datas automáticas

### 29 Junho

- Erro 404 (e não 403) para recursos de outra organização: desta forma não revela a existência de ids de outras organizações.
- Usar `@Transactional` nos serviços para garantir operações atómicas: se algo falhar a meio, faz rollback e tudo o que foi feito é desfeito.
- Usar `@Transactional(readOnly = true)` em operações de consulta, para melhor eficiência.
- As entities dento de `@Transactional` não precisa de `save()`: fica gerido pelo Hibernate.
- Usar a configuração `open-in-view: false` para fechar sessões JPA e evitar problemas de serialização JSON em entidades com relações LAZY.
- Usar JPQL em algumas consultas mais complexas nos repository.
- Usar SQL nativo para queries com funções do PostgreSQL ou condições ON nos LEFT JOIN.
- Usar `@EntityGraph` para trazer relações LAZY na mesma consulta com join, e evitar o problema N+1.

### 01 Julho

- Implementar JWT (JSON Web Token) com 2 tokens: acesso e refresh. Detalhes de implementação JWT: https://www.bezkoder.com/spring-boot-refresh-token-jwt/ e https://medium.com/@vishipatil/refresh-tokens-jwt-expiry-the-complete-guide-with-spring-boot-and-react-749674551005
- Access token é de vida curta: 15 minutos.
- Refresh token tem vida mais longa. Serve para pedir um access token novo. É guardado na bd e pode ser revogado.
- Como o servidor é stateless (não guarda sessão) não é possível desligar um token JWT de acesso. Se o token de acesso fosse válido por vários dias, se fosse roubado dava acesso durante todo esse tempo. Por esse motivo existe o refresh token para mitigar estragos como esse.
- Refresh token tem rotação: cada refresh só usa uma vez, gera um novo, e revoga o anterior.
- Logout revoga o refresh token, mas o access token continua válido (no máximo 15 minutos). Este é o preço de um servidor stateless e do funcionamento do JWT.
- CSRF desligado: Como estamos a usar JWT em que os tokens vem nos header dos pedidos, o browser não precisa enviar cookies, logo não se aplica este tipo de ataques.
- Tratar exceções por falta de autenticação (401) e por falta de permissão (403) nos filtros do spring security e não no `@RestControllerAdvice`. Para os dois casos escrevem no `ProblemDetail` à mão para uniformizar o formato dos erros. Referência: https://devlach.com/blog/java/spring-security-custom-authentication-failure

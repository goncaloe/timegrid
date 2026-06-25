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
- PostgreSQL

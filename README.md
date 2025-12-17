# ftc-online-course-notification-serverless

Projeto Tech Challenge Online Course Notification Serverless do Curso de Pós-Graduação Lato Sensu Arquitetura e Desenvolvimento em JAVA da Faculdade de Informática e Administração Paulista (FIAP). Aplicação Serverless responsável por enviar um relatório de feedbacks ao administrador semanalmente via Amazon EventBridge.

## Diagrama de Arquitetura

O diagrama abaixo ilustra a arquitetura e o fluxo de dados de todos os serviços envolvidos:

<img width="1466" height="1091" alt="diagrama-arquitetural" src="https://github.com/user-attachments/assets/dafefab7-a3bc-4219-b373-a9a776ff689f" />

## Diagrama de Banco de Dados

O diagrama abaixo ilustra a estrutura de dados do sistema:

<img width="1346" height="725" alt="ftc-online-course-database-diagram" src="https://github.com/user-attachments/assets/17a2a67d-9b36-40a7-aade-941b5ccf1b5e" />

## Arquitetura do Sistema

Este sistema foi desenvolvido utilizando Java 21 e PostgreSQL 17.4.

### Tecnologias Utilizadas
- **Java 21** como linguagem de programação da aplicação
- **PostgreSQL** como banco de dados relacional
- **JDBC** para integrar a aplicação com o banco de dados
- **AWS Lambda** para definição da estrutura serverless
- **AWS KMS** para criptografia das variáveis de ambiente
- **Jackson** para conversão de estrutura de dados (serializar e desserializar)
- **Javax Mail** para envio de e-mail

### Benefícios da Arquitetura
Essa estrutura simplificada possibilita:
- Desenvolvimento mais independente (responsabilidade única)
- Manutenção facilitada devido à separação de serviços
- Maior flexibilidade para futuras expansões
- Custo por requisição (arquitetura serverless)

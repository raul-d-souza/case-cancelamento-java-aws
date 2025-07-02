# Case Técnico: Cancelamento de Operações

## Contexto

Você faz parte de uma equipe de desenvolvimento responsável por criar soluções resilientes e escaláveis na nuvem. Seu desafio é implementar um sistema distribuído para gerenciar solicitações de cancelamento de operações de usuários, utilizando AWS DynamoDB e SQS, com aplicações escritas em Java.

## Desafio

Implemente **dois microserviços Java** que trabalham em conjunto para processar solicitações de cancelamento de operações:

### 1. Microserviço de Recebimento de Solicitações

- Deve expor um endpoint REST (exemplo: `POST /cancel-operation`).
- Ao receber uma requisição, o payload será um JSON contendo:
  - `userId` (string ou número)
  - `operationId` (string ou número)
- Ao receber a solicitação:
  - Grave a solicitação em uma tabela DynamoDB com os campos: `userId`, `operationId` e `status` (inicialmente `"pending"`).
  - Envie uma mensagem para uma fila SQS contendo os mesmos dados da solicitação.

### 2. Microserviço de Processamento de Cancelamentos

- Deve consumir as mensagens da fila SQS.
- Para cada mensagem recebida:
  - Localize a solicitação correspondente na tabela DynamoDB (pelo par `userId` e `operationId`).
  - Atualize o campo `status` de `"pending"` para `"successful"`.

## Requisitos Técnicos

- Ambas as aplicações devem ser escritas em **Java 21** (Spring Boot é sugerido, mas não obrigatório).
- Utilizar integração com DynamoDB e SQS.
- O código deve ser organizado, testável e seguir boas práticas.
- Documente como rodar localmente (pode usar [LocalStack](https://github.com/localstack/localstack) para simular AWS, se desejar).

## Pontos de Atenção

- Trate possíveis falhas de comunicação com AWS.
- Implemente logs relevantes.
- Considere cenários de concorrência e idempotência.

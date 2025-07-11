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

## Configuração do Ambiente Local

### Executando o LocalStack com Podman Compose

Para executar o LocalStack usando o arquivo `podman-compose.yml`, siga os passos abaixo:

1. **Certifique-se de ter o Podman instalado** em seu sistema
2. **Execute o comando** na raiz do projeto onde está o arquivo `podman-compose.yml`:

```bash
# Iniciar os serviços em background
podman-compose up -d

# Verificar se os containers estão rodando
podman-compose ps

# Visualizar logs (opcional)
podman-compose logs -f localstack
```

3. **Para parar os serviços**:
```bash
podman-compose down
```

**Nota**: O LocalStack estará disponível em `http://localhost:4566` após a inicialização.

### Configuração Inicial dos Recursos AWS

Após o LocalStack estar rodando, você precisa criar os recursos necessários:

#### 1. Criar a Tabela DynamoDB

```bash
# Criar a tabela Cancelamento
aws --endpoint-url=http://localhost:4566 dynamodb create-table \
    --table-name Cancelamento \
    --attribute-definitions \
        AttributeName=requestID,AttributeType=S \
    --key-schema \
        AttributeName=requestID,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST
```

#### 2. Criar a Fila SQS

```bash
# Criar a fila de cancelamento
aws --endpoint-url=http://localhost:4566 sqs create-queue \
    --queue-name cancelamento-requisicoes
```

### Comandos Úteis para Desenvolvimento

#### DynamoDB - Comandos de Gerenciamento

```bash
# Listar todas as tabelas
aws --endpoint-url=http://localhost:4566 dynamodb list-tables

# Descrever uma tabela específica
aws --endpoint-url=http://localhost:4566 dynamodb describe-table \
    --table-name Cancelamento

# Verificar estrutura da tabela (resumido)
aws --endpoint-url=http://localhost:4566 dynamodb describe-table \
    --table-name Cancelamento \
    --query 'Table.{TableName:TableName,KeySchema:KeySchema,AttributeDefinitions:AttributeDefinitions}'

# Listar itens da tabela
aws --endpoint-url=http://localhost:4566 dynamodb scan \
    --table-name Cancelamento

# Buscar item específico
aws --endpoint-url=http://localhost:4566 dynamodb get-item \
    --table-name Cancelamento \
    --key '{"requestID":{"S":"019"}}'

# Deletar tabela (se necessário)
aws --endpoint-url=http://localhost:4566 dynamodb delete-table \
    --table-name Cancelamento
```

#### SQS - Comandos de Gerenciamento

```bash
# Listar todas as filas
aws --endpoint-url=http://localhost:4566 sqs list-queues

# Obter URL da fila
aws --endpoint-url=http://localhost:4566 sqs get-queue-url \
    --queue-name cancelamento-requisicoes

# Obter atributos da fila
aws --endpoint-url=http://localhost:4566 sqs get-queue-attributes \
    --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/cancelamento-requisicoes \
    --attribute-names All

# Enviar mensagem de teste
aws --endpoint-url=http://localhost:4566 sqs send-message \
    --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/cancelamento-requisicoes \
    --message-body '{"requestID":"test-123","userID":"user-456","status":"PENDENTE","datetime":"2025-07-10T20:00:00Z"}'

# Receber mensagens
aws --endpoint-url=http://localhost:4566 sqs receive-message \
    --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/cancelamento-requisicoes

# Purgar fila (remover todas as mensagens)
aws --endpoint-url=http://localhost:4566 sqs purge-queue \
    --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/cancelamento-requisicoes

# Deletar fila
aws --endpoint-url=http://localhost:4566 sqs delete-queue \
    --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/cancelamento-requisicoes
```

### Script de Configuração Automatizada

Para facilitar a configuração inicial, você pode criar um script `setup-localstack.sh`:

```bash
#!/bin/bash

echo "Configurando recursos do LocalStack..."

# Aguardar LocalStack estar pronto
sleep 5

# Criar tabela DynamoDB
echo "Criando tabela DynamoDB..."
aws --endpoint-url=http://localhost:4566 dynamodb create-table \
    --table-name Cancelamento \
    --attribute-definitions AttributeName=requestID,AttributeType=S \
    --key-schema AttributeName=requestID,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST

# Criar fila SQS
echo "Criando fila SQS..."
aws --endpoint-url=http://localhost:4566 sqs create-queue \
    --queue-name cancelamento-requisicoes

echo "Configuração concluída!"
echo "DynamoDB: http://localhost:4566"
echo "SQS: http://localhost:4566"
```

Para executar o script:
```bash
chmod +x setup-localstack.sh
./setup-localstack.sh
```

## Troubleshooting

### Problemas Comuns

1. **Erro de conexão com LocalStack**:
   - Verifique se o LocalStack está rodando: `podman-compose ps`
   - Teste a conectividade: `curl http://localhost:4566`

2. **Tabela DynamoDB não encontrada**:
   - Verifique se a tabela foi criada: `aws --endpoint-url=http://localhost:4566 dynamodb list-tables`

3. **Fila SQS não encontrada**:
   - Verifique se a fila foi criada: `aws --endpoint-url=http://localhost:4566 sqs list-queues`

4. **Erro de chave primária no DynamoDB**:
   - Certifique-se de que a estrutura da tabela corresponde ao código da aplicação

### Logs Úteis

```bash
# Logs do LocalStack
podman-compose logs -f localstack

# Logs da aplicação Java
tail -f logs/application.log
```

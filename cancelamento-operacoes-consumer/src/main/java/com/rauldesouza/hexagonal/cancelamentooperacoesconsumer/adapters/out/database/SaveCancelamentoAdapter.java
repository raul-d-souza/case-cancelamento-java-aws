// adapters/out/SaveCancelamentoAdapter.java
package com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.adapters.out.database;

import com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.application.ports.out.ISaveCancelamentoPort;
import com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.domain.entities.CancelamentoPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class SaveCancelamentoAdapter implements ISaveCancelamentoPort {

    private static final Logger logger = LoggerFactory.getLogger(SaveCancelamentoAdapter.class);

    private final DynamoDbAsyncClient dynamoDbAsyncClient;
    private final String tableName;

    public SaveCancelamentoAdapter(
            DynamoDbAsyncClient dynamoDbAsyncClient,
            @Value("${config.infrastructure.database.dynamodb.table}") String tableName) {
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
        this.tableName = tableName;
    }

    @Override
    public CompletableFuture<Void> salvar(CancelamentoPersistence cancelamento) {
        logger.debug("Salvando cancelamento no DynamoDB: {}", cancelamento.requestID());

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("requestID", AttributeValue.builder().s(cancelamento.requestID()).build());
        item.put("userID", AttributeValue.builder().s(cancelamento.userID()).build());
        item.put("status", AttributeValue.builder().s(cancelamento.status()).build());
        item.put("datetime", AttributeValue.builder().s(cancelamento.datetime()).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        return dynamoDbAsyncClient.putItem(putItemRequest)
                .thenRun(() -> {
                    logger.debug("Item salvo com sucesso no DynamoDB: {}", cancelamento.requestID());
                })
                .exceptionally(throwable -> {
                    logger.error("Erro ao salvar item no DynamoDB: {}", cancelamento.requestID(), throwable);
                    throw new RuntimeException("Falha ao salvar no DynamoDB", throwable);
                });

    }
}

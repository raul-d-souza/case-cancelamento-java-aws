package com.rauldsouza.cancelamentooperacoesproducer.adapters.out.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauldsouza.cancelamentooperacoesproducer.application.ports.out.IPersistCancelamentoPort;
import com.rauldsouza.cancelamentooperacoesproducer.domain.entities.CancelamentoPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class PersistCancelamentoDynamodbAdapter implements IPersistCancelamentoPort {

    private final DynamoDbAsyncClient dynamoDbAsyncClient;
    private final ObjectMapper objectMapper;
    private final String tableName;

    public PersistCancelamentoDynamodbAdapter(
            DynamoDbAsyncClient dynamoDbAsyncClient,
            ObjectMapper objectMapper,
            @Value("${config.infrastructure.database.dynamodb.table}") String tableName
    ) {
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
        this.objectMapper = objectMapper;
        this.tableName = tableName;
    }

    @Override
    public void persist(CancelamentoPersistence persistence) {
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("requestID", AttributeValue.builder().s(persistence.requestID()).build());
            item.put("userID", AttributeValue.builder().s(persistence.userID()).build());
            item.put("status", AttributeValue.builder().s(persistence.status()).build());
            item.put("datetime", AttributeValue.builder().s(persistence.datetime()).build());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            CompletableFuture<?> future = dynamoDbAsyncClient.putItem(request);
            future.join();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao persistir no DynamoDB", e);
        }
    }
}

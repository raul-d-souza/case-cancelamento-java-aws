package com.rauldsouza.cancelamentooperacoesproducer.adapters.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauldsouza.cancelamentooperacoesproducer.application.ports.out.ISendCancelamentoMessagePort;
import com.rauldsouza.cancelamentooperacoesproducer.domain.entities.CancelamentoMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
public class SendCancelamentoMessageSqsAdapter implements ISendCancelamentoMessagePort {

    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;
    private final String queueUrl;

    public SendCancelamentoMessageSqsAdapter(
            SqsAsyncClient sqsAsyncClient,
            ObjectMapper objectMapper,
            @Value("${config.infrastructure.messaging.sqs.name}") String queueName
    ) {
        this.sqsAsyncClient = sqsAsyncClient;
        this.objectMapper = objectMapper;

        try {
            this.queueUrl = sqsAsyncClient.getQueueUrl(builder -> builder.queueName(queueName))
                    .get()
                    .queueUrl();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar a URL da fila SQS", e);
        }
    }

    @Override
    public void send(CancelamentoMessage message) {
        try {
            String messageBody = objectMapper.writeValueAsString(message);
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();
            sqsAsyncClient.sendMessage(request);


        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar mensagem para SQS", e);
        }
    }
}

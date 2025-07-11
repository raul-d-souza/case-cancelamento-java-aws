package com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.adapters.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.application.ports.in.IReceiveCancelamentoRequestPort;
import com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.domain.entities.CancelamentoMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ReceiveCancelamentoMessageAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveCancelamentoMessageAdapter.class);

    private final SqsAsyncClient sqsAsyncClient;
    private final IReceiveCancelamentoRequestPort receiveCancelamentoRequestPort;
    private final ObjectMapper objectMapper;
    private final String queueName;
    private final ScheduledExecutorService scheduler;

    private String queueUrl;

    public ReceiveCancelamentoMessageAdapter(
            SqsAsyncClient sqsAsyncClient,
            IReceiveCancelamentoRequestPort receiveCancelamentoRequestPort,
            @Value("${config.infrastructure.messaging.sqs.name}") String queueName) {
        this.sqsAsyncClient = sqsAsyncClient;
        this.receiveCancelamentoRequestPort = receiveCancelamentoRequestPort;
        this.queueName = queueName;
        this.objectMapper = new ObjectMapper();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @PostConstruct
    public void inicializar() {
        try {
            this.queueUrl = sqsAsyncClient.getQueueUrl(builder -> builder.queueName(queueName))
                    .get()
                    .queueUrl();

            logger.info("URL da fila SQS obtida: {}", queueUrl);

            // Inicia o polling da fila
            iniciarPolling();

        } catch (Exception e) {
            logger.error("Erro ao buscar a URL da fila SQS", e);
            throw new RuntimeException("Erro ao buscar a URL da fila SQS", e);
        }
    }

    private void iniciarPolling() {
        scheduler.scheduleWithFixedDelay(this::processarMensagens, 0, 5, TimeUnit.SECONDS);
        logger.info("Polling da fila SQS iniciado");
    }

    private void processarMensagens() {
        try {
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(20) // Long polling
                    .build();

            sqsAsyncClient.receiveMessage(receiveRequest)
                    .thenAccept(response -> {
                        for (Message message : response.messages()) {
                            processarMensagem(message);
                        }
                    })
                    .exceptionally(throwable -> {
                        logger.error("Erro ao receber mensagens da fila SQS", throwable);
                        return null;
                    });

        } catch (Exception e) {
            logger.error("Erro no polling da fila SQS", e);
        }
    }

    private void processarMensagem(Message message) {
        try {
            logger.info("Processando mensagem: {}", message.messageId());

            // Deserializar a mensagem JSON
            CancelamentoMessage cancelamentoMessage = objectMapper.readValue(
                    message.body(),
                    CancelamentoMessage.class
            );

            // Processar através do caso de uso
            receiveCancelamentoRequestPort.processarCancelamento(cancelamentoMessage);

            // Deletar mensagem da fila após processamento bem-sucedido
            deletarMensagem(message);

        } catch (Exception e) {
            logger.error("Erro ao processar mensagem: {}", message.messageId(), e);
            // Em um cenário real, você poderia implementar DLQ (Dead Letter Queue)
        }
    }

    private void deletarMensagem(Message message) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();

        sqsAsyncClient.deleteMessage(deleteRequest)
                .thenRun(() -> logger.debug("Mensagem deletada: {}", message.messageId()))
                .exceptionally(throwable -> {
                    logger.error("Erro ao deletar mensagem: {}", message.messageId(), throwable);
                    return null;
                });
    }

    @PreDestroy
    public void finalizar() {
        scheduler.shutdown();
        logger.info("Polling da fila SQS finalizado");
    }
}

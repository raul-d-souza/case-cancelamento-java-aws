package com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.adapters.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.application.ports.in.IReceiveCancelamentoRequestPort;
import com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.application.ports.out.IReceiveMessagePort;
import com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.domain.entities.CancelamentoMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SQSMessageListener implements IReceiveMessagePort {

    private final SqsAsyncClient sqsClient;
    private final IReceiveCancelamentoRequestPort receiveCancelamentoRequestPort;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler;

    @Value("${config.infrastructure.messaging.sqs.name}")
    private String queueUrl;

    private volatile boolean listening = false;

    public SQSMessageListener(SqsAsyncClient sqsClient,
                              IReceiveCancelamentoRequestPort receiveCancelamentoRequestPort) {
        this.sqsClient = sqsClient;
        this.receiveCancelamentoRequestPort = receiveCancelamentoRequestPort;
        this.objectMapper = new ObjectMapper();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        startListening();
    }

    @Override
    public void startListening() {
        listening = true;
        scheduler.scheduleWithFixedDelay(this::pollMessages, 0, 5, TimeUnit.SECONDS);
        System.out.println("Iniciando escuta da fila SQS: " + queueUrl);
    }

    @Override
    public void stopListening() {
        listening = false;
        scheduler.shutdown();
        System.out.println("Parando escuta da fila SQS");
    }

    private void pollMessages() {
        if (!listening) return;

        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(20)
                .build();

        CompletableFuture<Void> future = sqsClient.receiveMessage(receiveRequest)
                .thenAccept(response -> {
                    for (Message message : response.messages()) {
                        processMessage(message);
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("Erro ao receber mensagens do SQS: " + throwable.getMessage());
                    return null;
                });
    }

    private void processMessage(Message message) {
        try {
            // Deserializar a mensagem JSON para CancelamentoMessage
            CancelamentoMessage cancelamentoMessage = objectMapper.readValue(
                    message.body(), CancelamentoMessage.class);

            // Processar a mensagem através do use case
            receiveCancelamentoRequestPort.processarCancelamento(cancelamentoMessage);

            // Deletar a mensagem da fila após processamento bem-sucedido
            deleteMessage(message);

        } catch (Exception e) {
            System.err.println("Erro ao processar mensagem: " + e.getMessage());
            // Aqui você pode implementar lógica de retry ou dead letter queue
        }
    }

    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();

        sqsClient.deleteMessage(deleteRequest)
                .thenAccept(response -> {
                    System.out.println("Mensagem deletada da fila: " + message.messageId());
                })
                .exceptionally(throwable -> {
                    System.err.println("Erro ao deletar mensagem: " + throwable.getMessage());
                    return null;
                });
    }
}

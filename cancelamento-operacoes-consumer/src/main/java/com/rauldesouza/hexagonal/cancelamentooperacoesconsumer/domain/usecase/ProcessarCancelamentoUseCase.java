package com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.domain.usecase;

import com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.application.ports.in.IReceiveCancelamentoRequestPort;
import com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.application.ports.out.ISaveCancelamentoPort;
import com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.domain.entities.CancelamentoMessage;
import com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.domain.entities.CancelamentoPersistence;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProcessarCancelamentoUseCase implements IReceiveCancelamentoRequestPort {

    private static final Logger logger = LoggerFactory.getLogger(ProcessarCancelamentoUseCase.class);

    private final ISaveCancelamentoPort saveCancelamentoPort;

    public ProcessarCancelamentoUseCase(ISaveCancelamentoPort saveCancelamentoPort) {
        this.saveCancelamentoPort = saveCancelamentoPort;
    }

    @Override
    public void processarCancelamento(CancelamentoMessage message) {
        logger.info("Processando cancelamento para requestID: {}", message.requestID());

        try {
            // Conversão da mensagem para entidade de persistência
            CancelamentoPersistence persistence = new CancelamentoPersistence(
                    message.requestID(),
                    message.userID(),
                    "PROCESSADO",
                    message.datetime()
            );

            // Salvar no DynamoDB de forma assíncrona
            saveCancelamentoPort.salvar(persistence)
                    .thenRun(() -> logger.info("Cancelamento salvo com sucesso para requestID: {}", message.requestID()))
                    .exceptionally(throwable -> {
                        logger.error("Erro ao salvar cancelamento para requestID: {}", message.requestID(), throwable);
                        return null;
                    });

        } catch (Exception e) {
            logger.error("Erro ao processar cancelamento para requestID: {}", message.requestID(), e);
            throw new RuntimeException("Falha no processamento do cancelamento", e);
        }
    }
}

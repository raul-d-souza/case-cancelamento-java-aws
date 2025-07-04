package com.rauldsouza.cancelamentooperacoesproducer.domain.usecase;

import com.rauldsouza.cancelamentooperacoesproducer.application.ports.in.IReceiveCancelamentoRequestPort;
import com.rauldsouza.cancelamentooperacoesproducer.application.ports.out.IPersistCancelamentoPort;
import com.rauldsouza.cancelamentooperacoesproducer.application.ports.out.ISendCancelamentoMessagePort;
import com.rauldsouza.cancelamentooperacoesproducer.domain.entities.CancelamentoMessage;
import com.rauldsouza.cancelamentooperacoesproducer.domain.entities.CancelamentoPersistence;
import com.rauldsouza.cancelamentooperacoesproducer.domain.entities.CancelamentoRequest;
import org.springframework.stereotype.Service;

@Service
public class ProcessarCancelamentoUseCase implements IReceiveCancelamentoRequestPort {

    private final ISendCancelamentoMessagePort sendCancelamentoMessagePort;
    private final IPersistCancelamentoPort persistCancelamentoPort;

    public ProcessarCancelamentoUseCase(
            ISendCancelamentoMessagePort sendCancelamentoMessagePort,
            IPersistCancelamentoPort persistCancelamentoPort
    ) {
        this.sendCancelamentoMessagePort = sendCancelamentoMessagePort;
        this.persistCancelamentoPort = persistCancelamentoPort;
    }

    @Override
    public void processarCancelamento(CancelamentoRequest request) {
        String status = "PENDENTE";
        CancelamentoMessage message = new CancelamentoMessage(
                request.requestID(),
                request.userID(),
                status,
                request.datetime()
        );
        sendCancelamentoMessagePort.send(message);

        CancelamentoPersistence persist = new CancelamentoPersistence(
                request.requestID(),
                request.userID(),
                status,
                request.datetime()
        );
        persistCancelamentoPort.persist(persist);
    }
}

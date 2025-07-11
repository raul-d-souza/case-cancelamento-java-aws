package com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.application.ports.out;

import com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.domain.entities.CancelamentoMessage;

public interface IReceiveMessagePort {
    void startListening();

    void stopListening();
}

package com.rauldsouza.cancelamentooperacoesproducer.application.ports.out;

import com.rauldsouza.cancelamentooperacoesproducer.domain.entities.CancelamentoMessage;

public interface ISendCancelamentoMessagePort {
    void send(CancelamentoMessage message);
}

package com.rauldsouza.cancelamentooperacoesproducer.application.ports.in;

import com.rauldsouza.cancelamentooperacoesproducer.domain.entities.CancelamentoRequest;

public interface IReceiveCancelamentoRequestPort {
    void processarCancelamento(CancelamentoRequest request);
}

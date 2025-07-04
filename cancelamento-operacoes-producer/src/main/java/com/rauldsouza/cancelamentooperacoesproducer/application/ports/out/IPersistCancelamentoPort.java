package com.rauldsouza.cancelamentooperacoesproducer.application.ports.out;

import com.rauldsouza.cancelamentooperacoesproducer.domain.entities.CancelamentoPersistence;

public interface IPersistCancelamentoPort {
    void persist(CancelamentoPersistence persistence);
}

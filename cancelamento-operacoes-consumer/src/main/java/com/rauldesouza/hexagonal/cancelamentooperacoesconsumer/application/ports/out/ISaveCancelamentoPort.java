package com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.application.ports.out;

import com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.domain.entities.CancelamentoPersistence;

import java.util.concurrent.CompletableFuture;

public interface ISaveCancelamentoPort {
    CompletableFuture<Void> salvar(CancelamentoPersistence cancelamento);
}

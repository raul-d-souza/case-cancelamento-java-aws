package com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.application.ports.in;

import com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.domain.entities.CancelamentoMessage;
import org.springframework.context.annotation.Bean;

public interface IReceiveCancelamentoRequestPort {
    void processarCancelamento(CancelamentoMessage message);
}

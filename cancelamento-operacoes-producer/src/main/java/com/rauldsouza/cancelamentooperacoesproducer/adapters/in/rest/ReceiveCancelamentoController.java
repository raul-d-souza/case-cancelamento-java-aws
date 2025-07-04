package com.rauldsouza.cancelamentooperacoesproducer.adapters.in.rest;

import com.rauldsouza.cancelamentooperacoesproducer.application.ports.in.IReceiveCancelamentoRequestPort;
import com.rauldsouza.cancelamentooperacoesproducer.domain.entities.CancelamentoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cancelamento")
public class ReceiveCancelamentoController {

    private final IReceiveCancelamentoRequestPort receiveCancelamentoRequestPort;

    @Autowired
    public ReceiveCancelamentoController(IReceiveCancelamentoRequestPort receiveCancelamentoRequestPort) {
        this.receiveCancelamentoRequestPort = receiveCancelamentoRequestPort;
    }

    @PostMapping
    public void receberCancelamento(@RequestBody CancelamentoRequest request) {
        receiveCancelamentoRequestPort.processarCancelamento(request);
    }

}

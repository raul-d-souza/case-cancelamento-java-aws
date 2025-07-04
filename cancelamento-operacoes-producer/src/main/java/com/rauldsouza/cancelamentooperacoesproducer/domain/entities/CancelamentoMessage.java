package com.rauldsouza.cancelamentooperacoesproducer.domain.entities;

public record CancelamentoMessage(String requestID, String userID, String status, String datetime) {
}

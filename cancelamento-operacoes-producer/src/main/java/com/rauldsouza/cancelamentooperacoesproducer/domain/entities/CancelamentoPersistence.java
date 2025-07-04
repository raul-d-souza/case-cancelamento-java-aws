package com.rauldsouza.cancelamentooperacoesproducer.domain.entities;

public record CancelamentoPersistence(String requestID, String userID, String status, String datetime) {
}

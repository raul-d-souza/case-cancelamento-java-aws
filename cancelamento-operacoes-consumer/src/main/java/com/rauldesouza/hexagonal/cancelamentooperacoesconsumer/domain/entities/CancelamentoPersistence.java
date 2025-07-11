package com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.domain.entities;

public record CancelamentoPersistence(String requestID, String userID, String status, String datetime) {
}

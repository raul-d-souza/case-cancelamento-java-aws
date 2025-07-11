package com.rauldesouza.hexagonal.cancelamentooperacoesconsumer.domain.entities;

public record CancelamentoMessage(String requestID, String userID, String status, String datetime) {
}

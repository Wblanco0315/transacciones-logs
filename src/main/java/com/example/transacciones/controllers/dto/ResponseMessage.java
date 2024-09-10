package com.example.transacciones.controllers.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"message","status"})
public record ResponseMessage(
       String menssage,
       boolean status
) {
}

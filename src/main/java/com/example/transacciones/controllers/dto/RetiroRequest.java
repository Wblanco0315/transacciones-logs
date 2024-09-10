package com.example.transacciones.controllers.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;

@JsonPropertyOrder({"monto","cedula","password"})
public record RetiroRequest(@NotBlank Double monto, @NotBlank String cedula,@NotBlank String password){
}

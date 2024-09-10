package com.example.transacciones.controllers.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import org.hibernate.validator.constraints.NotBlank;

@Getter
public class DepositoRequest{
    @NotNull(message = "El monto no puede ser nulo")
    @Positive(message = "El monto debe ser mayor que cero")
    private Double monto;

    @NotBlank(message = "La cédula no puede estar en blanco")
    private String cedula;
}

package com.example.transacciones.controllers.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record AuthCreateUserRequest(@NotBlank @Valid String cedula,
                                    @NotBlank @Valid String password,
                                    @Valid AuthCreateRoleRequest roleRequest) {
}

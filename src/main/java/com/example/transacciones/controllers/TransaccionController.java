package com.example.transacciones.controllers;

import com.example.transacciones.controllers.dto.DepositoRequest;
import com.example.transacciones.controllers.dto.ResponseMessage;
import com.example.transacciones.controllers.dto.RetiroRequest;
import com.example.transacciones.models.TransaccionEntity;
import com.example.transacciones.models.UserEntity;
import com.example.transacciones.repositories.TransaccionRepository;
import com.example.transacciones.services.TransaccionService;
import com.example.transacciones.services.UserDetailServiceIMP;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;

@RestController
@RequestMapping("/transaccion")
@Slf4j
public class TransaccionController {
    static final Logger LOGGER = LogManager.getLogger(TransaccionController.class);
    @Autowired
    TransaccionService transaccionService;
    @Autowired
    UserDetailServiceIMP userDetailServiceIMP;
    @Autowired
    TransaccionRepository transaccionRepository;

    @GetMapping("/balance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseMessage> consultarSaldo(@RequestHeader("authorization") String token){
        LOGGER.info("Consultando saldo");
        ResponseMessage response=transaccionService.consultarSaldo(token);
        LOGGER.info("Saldo consultado con exito");
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PostMapping("/depositar")
    public ResponseEntity<ResponseMessage> depositar(@Valid @RequestBody DepositoRequest request){
        LOGGER.info("inicio de deposito saldo");

        ResponseEntity<ResponseMessage> deposito=transaccionService.depositarMonto(request);
        LOGGER.info("Despositado con exito");
        return deposito;

    }

    @PostMapping ("/retiro")
    public ResponseEntity<ResponseMessage> retiro(@RequestBody RetiroRequest request){

        LOGGER.info("inicio de retiro");
        ResponseEntity<ResponseMessage> retiro=transaccionService.retirarMonto(request);
        LOGGER.info("Retirado con exito");
        return retiro;
    }

    @PostMapping("/transferencia")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseMessage> transferencia(@RequestHeader("authorization") String token,@RequestBody DepositoRequest request){
        LOGGER.info("inicio de retiro");
        ResponseEntity<ResponseMessage>transferencia=transaccionService.transferir(token,request);
        LOGGER.info("Transferido con exito");
        return transferencia;
    }

    @GetMapping("auditoria")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Iterable<TransaccionEntity>> auditoria(){
        LOGGER.info("inicio, auditoria");
         Iterable<TransaccionEntity> transacciones = transaccionRepository.findAll();
        LOGGER.info("Auditoria exitosa!");
         return new ResponseEntity<>(transacciones,HttpStatus.OK);
    }

    @GetMapping("historial")
    @PreAuthorize("hasRole('USER')")
    public  ResponseEntity<Iterable<TransaccionEntity>> historial(@RequestHeader("authorization") String token){
        LOGGER.info("inicio, historial");
        Optional<UserEntity> usuario=transaccionService.buscarUsuarioLoggeado(token);
        String cedula=usuario.get().getCedula();
        Iterable<TransaccionEntity> transacciones = transaccionRepository.findTransaccionesByCedula(cedula);
        LOGGER.info("fin, historial");
        return new ResponseEntity<>(transacciones,HttpStatus.OK);
    }

}

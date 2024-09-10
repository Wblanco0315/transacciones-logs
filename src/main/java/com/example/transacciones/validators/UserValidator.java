package com.example.transacciones.validators;

import com.example.transacciones.controllers.dto.ResponseMessage;
import com.example.transacciones.errors.BadCredentialsException;
import com.example.transacciones.models.UserEntity;
import com.example.transacciones.repositories.UserRepository;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Getter
@Component
public class UserValidator {

    @Autowired
    private static final Logger LOGGER = LogManager.getLogger(UserValidator.class);
    @Autowired
    private UserRepository userRepository;

    private String message;

    //Valida que los campos esten correctamente diligenciados
    public ResponseMessage logginValidator(String cedula, String password){
        if (cedula.length()<5){
            message="La cedula debe tener minimo 5 caracteres";
            LOGGER.warn("warn {}",message);
            return new ResponseMessage(message,false);
        }
        if (password.length()<8){
            message="La contraseña debe tener minimo 8 caracteres";
            LOGGER.warn("warn {}",message);
            return new ResponseMessage(message,false);
        }
        return new ResponseMessage("User Loggin Sussesfully",true);
    }

    //Valida que el usuario registrado no exista
    public ResponseMessage signUpValidator(String cedula,String password){
        ResponseMessage response=logginValidator(cedula,password);
        Optional<UserEntity> user=userRepository.findUserEntityByCedula(cedula);
        if (user.isPresent()){
            message="El usuario ya existe";
            LOGGER.warn("warn {}",message);
            response=new ResponseMessage(message,false);
        }

        if (response.status()){
            LOGGER.info("Usuario {} creado exitosamente.",cedula);
            return new ResponseMessage("Registrado con exito",true);
        }
        return response;
    }

}

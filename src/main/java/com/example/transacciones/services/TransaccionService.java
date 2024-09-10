package com.example.transacciones.services;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.transacciones.controllers.dto.DepositoRequest;
import com.example.transacciones.controllers.dto.ResponseMessage;
import com.example.transacciones.controllers.dto.RetiroRequest;
import com.example.transacciones.models.TransaccionEntity;
import com.example.transacciones.models.UserEntity;
import com.example.transacciones.repositories.TransaccionRepository;
import com.example.transacciones.repositories.UserRepository;
import com.example.transacciones.util.JWTUtils;
import com.example.transacciones.validators.TransaccionValidator;
import com.example.transacciones.validators.UserValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class TransaccionService{

    @Autowired UserRepository userRepository;

    @Autowired TransaccionRepository transaccionRepository;

    @Autowired TransaccionValidator transaccionValidator;

    @Autowired UserValidator userValidator;

    @Autowired JWTUtils jwtUtils;

    @Autowired private PasswordEncoder passwordEncoder;

    static final Logger LOGGER = LogManager.getLogger(TransaccionService.class);

    //Buscar Usuario logeado atravez del token en la base de datos
    public  Optional<UserEntity> buscarUsuarioLoggeado(String bearerToken){
        Optional<UserEntity> user;
        String cedula="";
        String token = bearerToken.substring(7); //obtemenos una subcadena de texto para eliminar "BEARER"
        DecodedJWT decodedJWT;

        try { //Prueba si el token es valido
            decodedJWT = jwtUtils.validateToken(token);
        }
        catch (JWTDecodeException e){ //Toma la excepcion al no ser un token valido
            LOGGER.error("JWT DecodeException {}",e.getLocalizedMessage());
            throw new JWTDecodeException("Token invalido");
        }

        //Extrae la Cedula del token, el cual esta asigando como el subject.
        cedula = decodedJWT.getSubject();

        //Se realiza una busqueda en la base de datos por medio de la cedula
        user = userRepository.findUserEntityByCedula(cedula);
        return user;
    }

    //CONSULTA DE SALDO
    public ResponseMessage consultarSaldo(String token){
        Optional<UserEntity> usuario=buscarUsuarioLoggeado(token);//Busca al usuario loggeado a traves del JWT
        if (!usuario.isPresent()){//No encontro al usuario, podria deberse a un error con el token o este se encuentra vencido
            LOGGER.warn("Token Invalido");
            return new ResponseMessage("Token invalido",false);
        }

        BigDecimal balance=usuario.get().getBalance();
        Set<UserEntity> listaUsuarios = new HashSet<>();
        listaUsuarios.add(usuario.get());
        TransaccionEntity transaccion;
        //Crea un TransaccionEntity
        transaccion=TransaccionEntity.builder().tipo_transaccion("CONSULTA_SALDO").monto(new BigDecimal(0)).ListaUsuarios(listaUsuarios).build();
        transaccionRepository.save(transaccion);//Guarda la transaccion
        return new ResponseMessage("Saldo total= $"+balance,true);

    }
    
    //DEPOSITAR MONTO
    public ResponseEntity<ResponseMessage> depositarMonto(DepositoRequest request){
        Optional <UserEntity> usuarioBuscado=userRepository.findUserEntityByCedula(request.getCedula());//Busca al usuario
        ResponseMessage response;

        if (!usuarioBuscado.isPresent()){//Si el usuario no exite
            String message= "Usuario con cedula "+request.getCedula()+" no encontrado!";
            LOGGER.warn(message);
            response=new ResponseMessage(message,false);
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        }

        UserEntity usuario=usuarioBuscado.get();//Se Obtiene la entidad UserEntity
        ResponseMessage montoResponse=transaccionValidator.montoValidation(request.getMonto());//Valida que este correctamente diligenciado
        if (!montoResponse.status()){//El monto no paso las validaciones
            response=montoResponse;
            LOGGER.warn(response.menssage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }

        BigDecimal monto= BigDecimal.valueOf(request.getMonto());//Convierte a BigDecimal

        BigDecimal balance= usuario.getBalance();//Obtenemos el saldo del usuario al que se le deposita
        BigDecimal total=balance.add(monto);//suma los valores
        usuario.setBalance(total);//Se guarda el nuevo saldo
        userRepository.save(usuario);//Se guardan los cambios en la Base de datos

        Set<UserEntity> listaUsuarios = new HashSet<>();
        listaUsuarios.add(usuario);
        TransaccionEntity transaccion;
        transaccion=TransaccionEntity.builder().tipo_transaccion("DEPOSITO").monto(monto).ListaUsuarios(listaUsuarios).build();
        transaccionRepository.save(transaccion);//se guarda la trasaccion en base de datos

        response=new ResponseMessage("DEPOSITADO CON EXITO",true);
        return new  ResponseEntity (response,HttpStatus.OK);
    }

    // RETIRAR MONTO
    public ResponseEntity<ResponseMessage> retirarMonto(RetiroRequest request){

        ResponseMessage userResponse=userValidator.logginValidator(request.cedula(),request.password());
        if (!userResponse.status()){//No paso las validaciones
            LOGGER.warn("Usuario no encontrado");
            return new ResponseEntity(userResponse,HttpStatus.BAD_REQUEST);
        }

        ResponseMessage transaccionResponse=transaccionValidator.montoValidation(request.monto());//Validacion del monto
        if (!transaccionResponse.status()){//No paso la validacion
            LOGGER.warn(transaccionResponse.menssage());
            return new ResponseEntity(transaccionResponse,HttpStatus.BAD_REQUEST);
        }

        Optional <UserEntity> usuarioEncontrado=userRepository.findUserEntityByCedula(request.cedula());//Busca al usuario

        if (!usuarioEncontrado.isPresent()){//No encontro al usuario
            LOGGER.warn("Usuario{}",request.cedula()+" no encontrado");
            ResponseMessage responseMessage=new ResponseMessage("USUARIO"+request.cedula()+" NO ENCONTRADO",false);
            return new ResponseEntity<>(responseMessage,HttpStatus.BAD_REQUEST);
        }

        UserEntity usuario=usuarioEncontrado.get();//Obtenemos los datos del usuario
        if (!passwordEncoder.matches(request.password(),usuario.getPassword())){//Las contraseñas no coinciden
            LOGGER.warn("Usuario{}",request.cedula()+" contraseña incorrecta");
            ResponseMessage responseMessage=new ResponseMessage("CREDENCIALES INVALIDAS",false);
            return new ResponseEntity<>(responseMessage,HttpStatus.BAD_REQUEST);
        }

        BigDecimal balance= usuario.getBalance();//Se obtiene saldo del usuario
        Double monto= request.monto(); //Se obtiene valor del monto a retirar

        if (balance.compareTo(BigDecimal.valueOf(monto))<0){//Valida que el usuario tenga el saldo suficiente
            LOGGER.warn("saldo insuficiente, user:{}",request.cedula());
            ResponseMessage response=new ResponseMessage("Saldo Insuficiente",false);
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }

        BigDecimal total=balance.subtract(new BigDecimal(monto));//Resta el valor del monto a restirar
        usuario.setBalance(total);//Guarda el nuevo saldo
        userRepository.save(usuario);//Guarda los cambios en la base de datos

        Set<UserEntity> listaUsuarios = new HashSet<>();
        listaUsuarios.add(usuario);
        TransaccionEntity transaccion;
        transaccion=TransaccionEntity.builder().tipo_transaccion("RETIRO").monto(new BigDecimal(monto)).ListaUsuarios(listaUsuarios).build();
        transaccionRepository.save(transaccion);//Guarda la transaccion

        return new  ResponseEntity("RETIRADO CON EXITO, nuevo saldo disponible: $"+usuario.getBalance(),HttpStatus.OK);
    }

    //TRANSFERENCIA
    public ResponseEntity transferir(String token,DepositoRequest request) {
        ResponseMessage response;
        Optional<UserEntity> userRemitente=buscarUsuarioLoggeado(token);//Busca al usuario logeado
        if (userRemitente.get().getCedula().equals(request.getCedula())){//Valida que no se hagan depositos a si mismo
            LOGGER.error("No te puedes transferir a ti mismo");
            response=new ResponseMessage("No te puedes transferir a ti mismo",false);
            return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
        }
        if (!userRemitente.isPresent()){//Error en la autentificacion del Usuario logeado
            LOGGER.error("Autentificacion fallida user{}",request.getCedula());
            response=new ResponseMessage("Autentificacion Fallida!",false);
            return new ResponseEntity(response,HttpStatus.NETWORK_AUTHENTICATION_REQUIRED);
        }

        Optional<UserEntity> userDestino=userRepository.findUserEntityByCedula(request.getCedula());//Busca al quien se le va a transferir
        if(!userDestino.isPresent()){//No se encontro la cedula
            String message="Usuario con cedula "+request.getCedula()+" no existe";
            LOGGER.error(message);
            response=new ResponseMessage(message,false);
            return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
        }

        ResponseMessage transaccionResponse=transaccionValidator.montoValidation(request.getMonto());//Valida el monto a transferir
        if (!transaccionResponse.status()){//No aprobo la validacion
            LOGGER.error(transaccionResponse.menssage());
            return new ResponseEntity(transaccionResponse,HttpStatus.BAD_REQUEST);
        }
        BigDecimal monto= BigDecimal.valueOf(request.getMonto());
        BigDecimal balanceRemitente=userRemitente.get().getBalance();//Saldo del que envia el dinero
        BigDecimal balanceDestino=userDestino.get().getBalance();//Saldo del que recibe el dinero
        if(balanceRemitente.compareTo(monto)<0){//Valida que el Remitente tenga los fondos suficientes
            LOGGER.warn("saldo insuficiente, user:{}",request.getCedula());
            response=new ResponseMessage("Saldo insuficiente",false);
            return new  ResponseEntity(response,HttpStatus.BAD_REQUEST);
        }

        balanceRemitente=balanceRemitente.subtract(monto);//Extrae el monto a transferir del Remitente
        userRemitente.get().setBalance(balanceRemitente);//Guarda el nuevo saldo
        userRepository.save(userRemitente.get());//Guarda los cambios en la base de datos

        balanceDestino=balanceDestino.add(monto);//suma el monto a transferir
        userDestino.get().setBalance(balanceDestino);//Guarda el nuevo saldo del Destinatario
        userRepository.save(userDestino.get());

        Set<UserEntity> listaUsuarios = new HashSet<>();
        //Guarda a las cuentas involucradas en la transferencia en una lista
        listaUsuarios.add(userRemitente.get());
        listaUsuarios.add(userDestino.get());
        TransaccionEntity transaccion;
        transaccion=TransaccionEntity.builder().tipo_transaccion("TRANSFERENCIA").monto(monto).ListaUsuarios(listaUsuarios).build();
        transaccionRepository.save(transaccion);//Guarda la transaccion hecha

        response=new ResponseMessage("Depositado con exito",true);
        return new  ResponseEntity(response,HttpStatus.OK);
    }

}

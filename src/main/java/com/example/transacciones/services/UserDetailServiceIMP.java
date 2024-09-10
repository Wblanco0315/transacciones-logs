package com.example.transacciones.services;

import com.example.transacciones.controllers.dto.AuthCreateUserRequest;
import com.example.transacciones.controllers.dto.AuthLoginRequest;
import com.example.transacciones.controllers.dto.AuthResponse;
import com.example.transacciones.controllers.dto.ResponseMessage;

import com.example.transacciones.errors.GlobalExceptionHandler;
import com.example.transacciones.errors.IllegalArgumentException;
import com.example.transacciones.errors.UsernameNotFoundException;
import com.example.transacciones.models.RoleEntity;
import com.example.transacciones.models.UserEntity;
import com.example.transacciones.repositories.RoleRepository;
import com.example.transacciones.repositories.UserRepository;
import com.example.transacciones.util.JWTUtils;
import com.example.transacciones.validators.UserValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.transacciones.errors.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailServiceIMP implements UserDetailsService {
    @Autowired private UserRepository userRepository;

    @Autowired private RoleRepository roleRepository;

    @Autowired private JWTUtils jwtUtils;

    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private UserValidator validacion;

    static final Logger LOGGER = LogManager.getLogger(UserDetailServiceIMP.class);

    @Override
    //BUSQUEDA DEL USUARIO EN LA BASE DE DATOS
    public UserDetails loadUserByUsername(String cedula){
        UserEntity userEntity= null;
        try {//Intenta buscar al usuario a travez de la llave primaria, la cedula.
            userEntity = userRepository.findUserEntityByCedula(cedula).orElseThrow(()-> new UsernameNotFoundException("Usuario No encontrado"));
        } catch (UsernameNotFoundException e) {//No encontro al usuario, arroja la exection
            throw new RuntimeException(e);
        }
        List<SimpleGrantedAuthority> authorityList=new ArrayList<>(); //Carga la lista de permisos
        try {
            // Busca los roles asignados en el registro
            userEntity.getRoles().forEach(role -> authorityList.add(new SimpleGrantedAuthority("ROLE_".concat(role.getRoleEnum().name()))));

            // Busca los permisos de cada rol
            userEntity.getRoles().stream().flatMap(role -> role.getPermissionList().stream()).forEach(permission -> authorityList.add(new SimpleGrantedAuthority(permission.getName())));

            //Envia como respuesta al usuario con sus atributos
            return new User(userEntity.getCedula(), userEntity.getPassword(), userEntity.isEnable(), userEntity.isAccountNoExpired(), userEntity.isCredentialNoExpired(), userEntity.isAccountNoLocked(), authorityList);
        } catch (IllegalArgumentException e) {// Atrapa la exception al no encontrar rol/roles asignados
            throw new RuntimeException("Rol/roles no existentes");
        }
    }

    //Inicio de sesion
    public AuthResponse loginUser(AuthLoginRequest authLoginUser){
        String cedula=authLoginUser.getCedula();
        String password=authLoginUser.getPassword();
        String accessToken="";
        LOGGER.info("Inicio de login para el usuario: {}",cedula);
        ResponseMessage validation=validacion.logginValidator(cedula,password);//Validacion de campos y del usuario
        if (validation.status()){// El usuario fue encontrado
            Authentication authentication=this.authentication(cedula,password);//Obtiene al usuario autentificado
            accessToken= jwtUtils.createToken(authentication);//Crea el token de autentificacion con los datos del usuario
            SecurityContextHolder.getContext().setAuthentication(authentication); //Crea la sesion autorizando al usuario.
        }
        return new AuthResponse(cedula, validation.menssage(),accessToken, validation.status());
    }

    //CREACION DEL USUARIO
    public ResponseMessage createUser(AuthCreateUserRequest createRoleRequest) throws IllegalArgumentException{
        String cedula = createRoleRequest.cedula();
        String password=createRoleRequest.password();
        LOGGER.info("Inicio de registro para el usuario: {}",cedula);

        ResponseMessage response=validacion.signUpValidator(cedula,password);// Valida si el usuario existe
        if (!response.status()){//si el usuario no existe retorna una respuesta
            return response;
        }

        List<String> rolesRequest = createRoleRequest.roleRequest().roleListName();//Obtiene la lista de roles asignados en el registro
        Set<RoleEntity> roleEntityList;//Inicializacion de lista de roles
        try {//Prueba si la lista de roles existe
            roleEntityList = roleRepository.findRoleEntitiesByRoleEnumIn(rolesRequest).stream().collect(Collectors.toSet());//guarda los roles del usuario en una lista
        } catch (IllegalArgumentException ex) {//Uno de los roles o rol asignado en el request no exite o no lo encontro
            LOGGER.info("Rol no existente {}",ex.getMessage());
            throw new RuntimeException("Rol/Roles invalidos ");
        }
        if (roleEntityList.isEmpty()) {throw new IllegalArgumentException("The roles specified does not exist."); } //Verifica si se asignaron roles

        //Crea una entidad usuario con sus datos y roles obtenidos anteriormente
        UserEntity userEntity = UserEntity.builder().cedula(cedula).password(passwordEncoder.encode(password)).balance(new BigDecimal(1000000)).roles(roleEntityList).isEnable(true).accountNoLocked(true).accountNoExpired(true).credentialNoExpired(true).build();
        UserEntity userSaved = userRepository.save(userEntity);//Guarda en la base de datos al usuario
        LOGGER.info("Usuario {} creado exitosamente.",cedula);
        return response;
    }

    //Autenticacion
    public Authentication authentication(String cedula,String password){
        UserDetails userDetails=this.loadUserByUsername(cedula);//Busca al usuario en la base de datos por cedula
        if(userDetails==null){//Si la cedula no esta registrada genera una exception
            throw new BadCredentialsException("El usuario "+cedula+ "no existe");
        }
        if (!passwordEncoder.matches(password,userDetails.getPassword())){//si las contraseñas no coincides arroja la exception
            throw new BadCredentialsException("Password incorrecto");
        };
        //Genera la autorizacion
        return new UsernamePasswordAuthenticationToken(cedula,userDetails.getPassword(),userDetails.getAuthorities());
    }

}

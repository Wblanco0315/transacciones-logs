package com.example.transacciones.repositories;

import com.example.transacciones.models.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<UserEntity,Long> {


    Optional<UserEntity> findUserEntityByCedula(String cedula);

}

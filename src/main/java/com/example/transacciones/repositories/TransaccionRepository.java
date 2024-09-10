package com.example.transacciones.repositories;

import com.example.transacciones.models.TransaccionEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransaccionRepository extends CrudRepository<TransaccionEntity,Long> {

    @Query(value = "SELECT t.id,t.monto_transaccion,t.tipo_transaccion,t.fecha_transaccion FROM public.transacciones as t INNER JOIN public.transaccion_user as u on id=transaccion_id  WHERE cedula= :cedula",nativeQuery = true)
    List<TransaccionEntity> findTransaccionesByCedula(@Param("cedula") String cedula);
}

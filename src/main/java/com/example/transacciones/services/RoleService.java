package com.example.transacciones.services;

import com.example.transacciones.models.PermissionEntity;
import com.example.transacciones.models.RoleEntity;
import com.example.transacciones.models.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Service
public class RoleService {
    private PermissionEntity creationPermission=PermissionEntity.builder()
            .name("CREATE")
            .build();

    private PermissionEntity readPermission=PermissionEntity.builder()
            .name("READ")
            .build();

    private PermissionEntity updatePermission=PermissionEntity.builder()
            .name("UPDATE")
            .build();

    private PermissionEntity deletePermission=PermissionEntity.builder()
            .name("DELETE")
            .build();

    //PERMISOS DE ROLES
    public RoleEntity admin() {
        return RoleEntity.builder()
                .roleEnum(RoleEnum.ADMIN)
                .permissionList(Set.of(creationPermission, readPermission, updatePermission, deletePermission))
                .build();
    }

    public RoleEntity user() {
        return RoleEntity.builder()
                .roleEnum(RoleEnum.USER)
                .permissionList(Set.of(creationPermission, readPermission, updatePermission))
                .build();
    }
}

package com.example.transacciones.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "users")
public class UserEntity {
    @Id @NotNull @Size(min = 5, max = 12) private String cedula;

    @Column(name = "password") private String password;

    @Column(name = "balance") private BigDecimal balance;

    @Column(name = "is_enable") private boolean isEnable;

    @Column(name = "account_no_expired") private boolean accountNoExpired;

    @Column(name = "account_no_locked") private boolean accountNoLocked;

    @Column(name = "credential_no_expired") private boolean credentialNoExpired;

    @ManyToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    @JoinTable(name = "user_role",joinColumns = @JoinColumn(name = "user_id"),inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles=new HashSet<>();
}

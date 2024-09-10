package com.example.transacciones.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "Transacciones")
public class TransaccionEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long  id;
    @Column(name = "tipo_transaccion")
    private String tipo_transaccion;
    @Column(name = "monto_transaccion")
    private BigDecimal monto;
    @Column(name = "fecha_transaccion")
    @Temporal(TemporalType.DATE)
    private Date fecha;

    @PrePersist
    protected void onCreate() {
        fecha = new Date();
    }

    @ManyToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinTable(
            name = "transaccion_user", // Nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "transaccion_id"), // Clave foránea hacia Estudiante
            inverseJoinColumns = @JoinColumn(name = "cedula") // Clave foránea hacia Curso
    )
    private Set<UserEntity> ListaUsuarios=new HashSet<>();
}

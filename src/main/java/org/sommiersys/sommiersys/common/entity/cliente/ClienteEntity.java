package org.sommiersys.sommiersys.common.entity.cliente;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.sommiersys.sommiersys.common.entity.facturaCabecera.FacturaCabeceraEntity;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "clientes")
public class ClienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "telefono")
    private String telefono;

    @Email
    @Column(name = "email")
    private String email;

    @Column(name = "cedula", unique = true, length = 13)
    private String cedula;


    private Boolean deleted = Boolean.FALSE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<FacturaCabeceraEntity> facturas;


}

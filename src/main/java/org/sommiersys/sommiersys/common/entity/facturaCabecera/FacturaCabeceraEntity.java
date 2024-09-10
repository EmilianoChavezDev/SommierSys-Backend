package org.sommiersys.sommiersys.common.entity.facturaCabecera;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.sommiersys.sommiersys.common.entity.cliente.ClienteEntity;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "Facturas_Cabecera")
public class FacturaCabeceraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "numero_factura", unique = true, nullable = false)
    private String numeroFactura;

    @Column(name = "total")
    private Double total;


    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", referencedColumnName = "id")
    private ClienteEntity cliente;


    @Column(name = "esCompra")
    private Boolean esCompra;


    @Column(name = "iva5")
    private Double iva5;

    @Column(name = "iva10")
    private Double iva10;


    private Boolean deleted = Boolean.FALSE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

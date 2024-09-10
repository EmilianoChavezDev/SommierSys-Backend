package org.sommiersys.sommiersys.common.entity.facturaDetalle;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.sommiersys.sommiersys.common.entity.producto.ProductoEntity;

import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "Facturas_Detalle")
public class FacturaDetalleEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "precio_unitario")
    private Double precioUnitario;

    @Column(name = "subtotal")
    private Double subtotal;

    @Column(name = "iva5")
    private Double iva5;

    @Column(name = "iva10")
    private Double iva10;


    @ManyToOne
    @JoinColumn(name = "producto_id", referencedColumnName = "id")
    private ProductoEntity producto;

    private Boolean deleted = Boolean.FALSE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}

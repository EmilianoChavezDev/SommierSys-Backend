package org.sommiersys.sommiersys.common.entity.producto;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.sommiersys.sommiersys.common.entity.facturaDetalle.FacturaDetalleEntity;
import org.sommiersys.sommiersys.common.entity.proveedor.ProveedorEntity;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "Productos")
public class ProductoEntity {


    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "marca")
    private String marca;

    @Column(name = "precio_compra")
    private Double precioCompra;

    @Column(name = "precio_venta")
    private Double precioVenta;

    @Column(name = "iva5")
    private Double iva5;

    @Column(name = "iva10")
    private Double iva10;


    @ManyToOne
    @JoinColumn(name = "proveedor_id", referencedColumnName = "id")
    private ProveedorEntity proveedor;


    @OneToMany(mappedBy = "producto")
    private List<FacturaDetalleEntity> detalle;

    private Boolean deleted = Boolean.FALSE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}

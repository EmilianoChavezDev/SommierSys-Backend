package org.sommiersys.sommiersys.dto.producto;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import org.sommiersys.sommiersys.common.entity.facturaDetalle.FacturaDetalleEntity;
import org.sommiersys.sommiersys.common.entity.proveedor.ProveedorEntity;

import java.io.Serializable;


@Data
public class ProductoDto implements Serializable {

    private Long id;
    private String nombre;
    private String marca;
    private Double precioCompra;
    private Double precioVenta;
    private Double iva5;
    private Double iva10;
    private Long proveedor;
}

package org.sommiersys.sommiersys.dto.facturaDetalle;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import org.sommiersys.sommiersys.common.entity.producto.ProductoEntity;

import java.io.Serializable;



@Data
public class FacturaDetalleDto implements Serializable {

    private Long id;
    private Integer cantidad;
    private Double precioUnitario;
    private Double iva5;
    private Double iva10;
    private Long producto;
}

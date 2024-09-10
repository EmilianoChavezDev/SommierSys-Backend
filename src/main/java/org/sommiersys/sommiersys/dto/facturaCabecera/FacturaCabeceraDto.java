package org.sommiersys.sommiersys.dto.facturaCabecera;


import lombok.Data;
import org.sommiersys.sommiersys.dto.facturaDetalle.FacturaDetalleDto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class FacturaCabeceraDto implements Serializable {

    private Long id;
    private String numeroFactura;
    private Double total;
    private LocalDate fecha;
    private Double iva5;
    private Double iva10;
    private Boolean esCompra;
    private Long clienteId;
    private List<FacturaDetalleDto> facturasDetalles = new ArrayList<>();

}

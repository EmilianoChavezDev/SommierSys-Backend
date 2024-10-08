package org.sommiersys.sommiersys.configuration;

import org.pack.sommierJar.dto.facturaCabecera.FacturaCabeceraDto;
import org.pack.sommierJar.dto.facturaDetalle.FacturaDetalleDto;
import org.pack.sommierJar.entity.facturaCabecera.FacturaCabeceraEntity;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;
import java.util.stream.Collectors;

public class MapperFactura {


        public static FacturaCabeceraDto toDto(FacturaCabeceraEntity entity, CacheManager cacheManager) {
            FacturaCabeceraDto dto = new FacturaCabeceraDto();
            dto.setId(entity.getId());
            dto.setFecha(entity.getFecha());
            dto.setTotal(entity.getTotal());
            dto.setIva5(entity.getIva5());
            dto.setIva10(entity.getIva10());
            dto.setEsCompra(entity.getEsCompra());
            dto.setClienteId(entity.getCliente() != null ? entity.getCliente().getId() : null);
            dto.setNumeroFactura(entity.getNumeroFactura());

            List<FacturaDetalleDto> detalles = entity.getFacturaDetalles()
                    .stream()
                    .map(det -> {
                        FacturaDetalleDto detDto = new FacturaDetalleDto();
                        detDto.setId(det.getId());
                        detDto.setSubtotal(det.getSubtotal());
                        detDto.setProducto(det.getProducto().getId());
                        detDto.setCantidad(det.getCantidad());
                        detDto.setIva5(det.getIva5());
                        detDto.setIva10(det.getIva10());
                        detDto.setPrecioUnitario(det.getPrecioUnitario());
                        return detDto;
                    })
                    .collect(Collectors.toList());

            dto.setFacturaDetalles(detalles);

            // Cachear cada factura individualmente
            String key = "api_factura_" + dto.getId();
            Cache cache = cacheManager.getCache("facturasCache");
            if (cache != null) {
                FacturaCabeceraDto cachedFactura = cache.get(key, FacturaCabeceraDto.class);
                if (cachedFactura == null) {
                    cache.put(key, dto);
                }
            }

            return dto;
        }



    public static FacturaCabeceraDto toDtoDelete(FacturaCabeceraEntity entity) {
        FacturaCabeceraDto dto = new FacturaCabeceraDto();
        dto.setId(entity.getId());
        dto.setNumeroFactura(entity.getNumeroFactura());
        dto.setEsCompra(entity.getEsCompra());
        dto.setIva5(entity.getIva5());
        dto.setIva10(entity.getIva10());
        dto.setTotal(entity.getTotal());
        dto.setClienteId(entity.getCliente() != null ? entity.getCliente().getId() : null);

        // Mapeo de los detalles
        List<FacturaDetalleDto> detallesDto = entity.getFacturaDetalles()
                .stream()
                .map(detalle -> {
                    FacturaDetalleDto detalleDto = new FacturaDetalleDto();
                    detalleDto.setId(detalle.getId());
                    detalleDto.setCantidad(detalle.getCantidad());
                    detalleDto.setPrecioUnitario(detalle.getPrecioUnitario());
                    detalleDto.setIva5(detalle.getIva5());
                    detalleDto.setIva10(detalle.getIva10());
                    detalleDto.setSubtotal(detalle.getSubtotal());
                    detalleDto.setProducto(detalle.getProducto().getId());
                    return detalleDto;
                })
                .collect(Collectors.toList());

        dto.setFacturaDetalles(detallesDto);

        return dto;
    }

}

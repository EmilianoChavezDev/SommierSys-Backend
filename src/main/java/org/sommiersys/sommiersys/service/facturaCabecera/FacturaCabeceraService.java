package org.sommiersys.sommiersys.service.facturaCabecera;

import org.modelmapper.ModelMapper;

import org.pack.sommierJar.dto.facturaCabecera.FacturaCabeceraDto;
import org.pack.sommierJar.dto.facturaDetalle.FacturaDetalleDto;
import org.pack.sommierJar.entity.cliente.ClienteEntity;
import org.pack.sommierJar.entity.facturaCabecera.FacturaCabeceraEntity;
import org.pack.sommierJar.entity.facturaDetalle.FacturaDetalleEntity;
import org.pack.sommierJar.entity.producto.ProductoEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sommiersys.sommiersys.common.exception.ControllerRequestException;
import org.sommiersys.sommiersys.common.interfaces.IBaseService;
import org.sommiersys.sommiersys.repository.facturaCabecera.FacturaCabeceraRepository;
import org.sommiersys.sommiersys.repository.facturaDetalle.FacturaDetalleRepository;
import org.sommiersys.sommiersys.repository.producto.ProductoRepository;
import org.sommiersys.sommiersys.service.facturaDetalle.FacturaDetalleService;
import org.sommiersys.sommiersys.service.producto.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FacturaCabeceraService implements IBaseService<FacturaCabeceraDto> {

    private static final Logger logger = LoggerFactory.getLogger(FacturaCabeceraService.class);

    @Autowired
    private FacturaCabeceraRepository facturaCabeceraRepository;

    @Autowired
    private FacturaDetalleRepository facturaDetalleRepository;

    @Autowired
    private FacturaDetalleService facturaDetalleService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoRepository productoRepository;


    ModelMapper modelMapper = new ModelMapper();

    @Override
    public Page<FacturaCabeceraDto> findAll(Pageable pageable) {
        Page<FacturaCabeceraEntity> entities = facturaCabeceraRepository.findAll(pageable);
        return entities.map(entity -> modelMapper.map(entity, FacturaCabeceraDto.class));
    }

    @Override
    public Optional<FacturaCabeceraDto> findById(Long id) {
        Optional<FacturaCabeceraEntity> entity = facturaCabeceraRepository.findById(id);
        return entity.map(e -> modelMapper.map(e, FacturaCabeceraDto.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FacturaCabeceraDto save(FacturaCabeceraDto dto) {
        try {
            // Convertir DTO a entidad
            FacturaCabeceraEntity entity = modelMapper.map(dto, FacturaCabeceraEntity.class);

            // Generar número de factura automáticamente si es una venta
            if (!entity.getEsCompra()) {
                String numeroFactura = generateFacturaNumber();  // Método para generar el número
                entity.setNumeroFactura(numeroFactura);
            }

            // Procesar los detalles de la factura
            List<FacturaDetalleEntity> facturaDetalleEntities = dto.getFacturaDetalles()
                    .stream()
                    .map(this::convertToFacturaDetalleEntity)
                    .collect(Collectors.toList());

            // Calcular el total automáticamente
            Double total = facturaDetalleEntities.stream()
                    .mapToDouble(detalle -> detalle.getCantidad() * detalle.getPrecioUnitario())
                    .sum();

            Double totalIva5 = facturaDetalleEntities.stream()
                    .mapToDouble(detalle -> detalle.getCantidad() * detalle.getPrecioUnitario() * detalle.getIva5() / 100)
                    .sum();

            Double totalIva10 = facturaDetalleEntities.stream()
                    .mapToDouble(detalle -> detalle.getCantidad() * detalle.getPrecioUnitario() * detalle.getIva10() / 100)
                    .sum();

            entity.setTotal(total + totalIva5 + totalIva10);
            entity.setIva5(totalIva5);
            entity.setIva10(totalIva10);

            // Asignar los detalles a la entidad de la factura
            entity.setFacturaDetalles(facturaDetalleEntities);

            // Guardar la entidad de la factura
            FacturaCabeceraEntity savedEntity = facturaCabeceraRepository.save(entity);

            // Actualizar stock si es una compra
            if (savedEntity.getEsCompra()) {
                for (FacturaDetalleEntity detalle : savedEntity.getFacturaDetalles()) {
                    // Aquí deberías tener un servicio para actualizar el stock de los productos
                    productoService.updateStock(detalle.getProducto().getId(), detalle.getCantidad());
                }
            }

            // Lógica para imprimir si no es compra (es una venta)
            if (!savedEntity.getEsCompra()) {
                imprimirFactura(savedEntity);  // Método para manejar la lógica de impresión
            }

            // Convertir la entidad guardada de nuevo a DTO y retornarla
            return modelMapper.map(savedEntity, FacturaCabeceraDto.class);
        } catch (Exception e) {
            logger.error("Error al guardar la factura", e);
            throw new ControllerRequestException("Error al guardar la factura", e);
        }
    }

    private FacturaDetalleEntity convertToFacturaDetalleEntity(FacturaDetalleDto dto) {
        FacturaDetalleEntity entity = modelMapper.map(dto, FacturaDetalleEntity.class);
        ProductoEntity producto = productoRepository.findById(dto.getProducto()).orElseThrow(() -> new ControllerRequestException("Producto no encontrado"));
        entity.setProducto(producto);
        return entity;
    }



    private void imprimirFactura(FacturaCabeceraEntity entity) {
        // Implementar la lógica para imprimir la factura
        // Esto podría involucrar generar un PDF y enviarlo a una impresora o guardarlo en un archivo
    }




    private String generateFacturaNumber() {
        String prefix = "FAC-";
        long count = facturaCabeceraRepository.count() + 1; // Obtén el número total de facturas y suma 1
        return prefix + String.format("%06d", count); // Genera un número con ceros a la izquierda
    }


    @Override
    public void delete(Long id) {
        Optional<FacturaCabeceraEntity> entityOptional = facturaCabeceraRepository.findById(id);
        if (entityOptional.isPresent()) {
            FacturaCabeceraEntity entity = entityOptional.get();
            entity.setDeleted(true);  // Asumimos un borrado lógico
            facturaCabeceraRepository.save(entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FacturaCabeceraDto update(Long id, FacturaCabeceraDto dto) {
        Optional<FacturaCabeceraEntity> entityOptional = facturaCabeceraRepository.findById(id);
        if (entityOptional.isPresent()) {
            FacturaCabeceraEntity entity = entityOptional.get();
            // Actualizar los campos necesarios
            entity.setNumeroFactura(dto.getNumeroFactura());
            entity.setTotal(dto.getTotal());
            entity.setEsCompra(dto.getEsCompra());
            entity.setIva5(dto.getIva5());
            entity.setIva10(dto.getIva10());
            entity.setCliente(modelMapper.map(dto.getClienteId(), ClienteEntity.class));

            // Guardar los cambios
            FacturaCabeceraEntity updatedEntity = facturaCabeceraRepository.save(entity);

            // Lógica para imprimir si no es compra (es una venta)
            if (!updatedEntity.getEsCompra()) {
                imprimirFactura(updatedEntity);
            }

            // Convertir a DTO y retornar
            return modelMapper.map(updatedEntity, FacturaCabeceraDto.class);
        }
        return null;
    }




    @Transactional(readOnly = true)
    public Page<FacturaCabeceraDto> findByClienteNombreOrNumeroFactura(Pageable pageable, String nombre, String numeroFactura) {
        Page<FacturaCabeceraEntity> facturaEntities = facturaCabeceraRepository.findByClienteNombreOrNumeroFactura(pageable, nombre, numeroFactura);
        return facturaEntities.map(facturaEntity -> modelMapper.map(facturaEntity, FacturaCabeceraDto.class));
    }



    private FacturaCabeceraDto convertToFacturaDto(FacturaCabeceraEntity entity) {
        FacturaCabeceraDto dto = modelMapper.map(entity, FacturaCabeceraDto.class);
        List<FacturaDetalleDto> detalleDtos = entity.getFacturaDetalles()
                .stream()
                .map(this::convertToFacturaDetalleDto)
                .collect(Collectors.toList());

        dto.setFacturaDetalles(detalleDtos);
        return dto;
    }


    private FacturaDetalleDto convertToFacturaDetalleDto(FacturaDetalleEntity entity) {
        return modelMapper.map(entity, FacturaDetalleDto.class);
    }
}

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
import org.sommiersys.sommiersys.repository.cliente.ClienteRepository;
import org.sommiersys.sommiersys.repository.facturaCabecera.FacturaCabeceraRepository;
import org.sommiersys.sommiersys.repository.facturaDetalle.FacturaDetalleRepository;
import org.sommiersys.sommiersys.repository.producto.ProductoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private ProductoRepository productoRepository;

    @Autowired
    private ClienteRepository clienteRepository;


    ModelMapper modelMapper = new ModelMapper();

    @Override
    public Page<FacturaCabeceraDto> findAll(Pageable pageable) {
        Page<FacturaCabeceraEntity> entities = facturaCabeceraRepository.findAll(pageable);
        return entities.map(entity -> {
            FacturaCabeceraDto dto = modelMapper.map(entity, FacturaCabeceraDto.class);
            dto.setFacturaDetalles(entity.getFacturaDetalles()
                    .stream()
                    .map(det -> modelMapper.map(det, FacturaDetalleDto.class))
                    .collect(Collectors.toList()));
            return dto;
        });
    }

    @CachePut(cacheManager = "cacheManagerWithoutTTL", value = "sd", key = "'api_facturas_' + #id")
    @Transactional(readOnly = true)
    public Optional<FacturaCabeceraDto> findById(Long id) {
        FacturaCabeceraEntity entity = facturaCabeceraRepository.findById(id)
                .orElseThrow(() -> new ControllerRequestException("No existe la factura con ID " + id));
        FacturaCabeceraDto dto = modelMapper.map(entity, FacturaCabeceraDto.class);
        List<FacturaDetalleDto> detalles = entity.getFacturaDetalles().stream()
                .map(det -> modelMapper.map(det, FacturaDetalleDto.class))
                .collect(Collectors.toList());
        dto.setFacturaDetalles(detalles);
        return Optional.of(dto);
    }

        @Transactional(rollbackFor = Exception.class)
        public FacturaCabeceraDto save(FacturaCabeceraDto dto) {
            try {
                FacturaCabeceraEntity entity = new FacturaCabeceraEntity();
                entity.setEsCompra(dto.getEsCompra());
                entity.setIva5(dto.getIva5());
                entity.setIva10(dto.getIva10());
                entity.setFecha(LocalDate.from(LocalDateTime.now()));
                entity.setNumeroFactura(entity.getEsCompra() ? "0" : generateFacturaNumber());

                if (dto.getClienteId() != null) {
                    ClienteEntity cliente = clienteRepository.findById(dto.getClienteId())
                            .orElseThrow(() -> new ControllerRequestException("No existe el cliente con ID " + dto.getClienteId()));
                    entity.setCliente(cliente);
                }

                FacturaCabeceraEntity savedEntity = facturaCabeceraRepository.save(entity);
                List<FacturaDetalleEntity> det = new ArrayList<>();

                double total = 0.0;
                double totalIva5 = 0.0;
                double totalIva10 = 0.0;

                for (FacturaDetalleDto detalleDto : dto.getFacturaDetalles()) {
                    ProductoEntity producto = productoRepository.findById(detalleDto.getProducto())
                            .orElseThrow(() -> new ControllerRequestException("No existe el producto con ID " + detalleDto.getProducto()));

                    // Calcular subtotal, IVA
                    FacturaDetalleEntity detalleEntity = new FacturaDetalleEntity();
                    detalleEntity.setCantidad(detalleDto.getCantidad());
                    detalleEntity.setPrecioUnitario(entity.getEsCompra() ? producto.getPrecioCompra() : producto.getPrecioVenta());
                    detalleEntity.setIva5(producto.getIva5());
                    detalleEntity.setIva10(producto.getIva10());
                    detalleEntity.setProducto(producto);

                    // Calcular subtotal e IVA
                    double subtotal = detalleEntity.getCantidad() * detalleEntity.getPrecioUnitario();
                    double iva5 = (producto.getIva5() / 100) * subtotal;
                    double iva10 = (producto.getIva10() / 100) * subtotal;

                    detalleEntity.setSubtotal(subtotal);
                    detalleEntity.setIva5(iva5);
                    detalleEntity.setIva10(iva10);

                    // Actualizar los totales
                    total += subtotal;
                    totalIva5 += iva5;
                    totalIva10 += iva10;


                    // Asigno la cab al det
                    detalleEntity.setFactura(savedEntity);
                    det.add(detalleEntity);

                    producto.setCantidad(savedEntity.getEsCompra() ?  producto.getCantidad() + detalleEntity.getCantidad() : producto.getCantidad() - detalleEntity.getCantidad());


                    productoRepository.save(producto);
                    facturaDetalleRepository.save(detalleEntity);
                }

                savedEntity.setTotal(total + totalIva5 + totalIva10);
                savedEntity.setIva5(totalIva5);
                savedEntity.setIva10(totalIva10);


                savedEntity = facturaCabeceraRepository.save(savedEntity);

                List<FacturaDetalleDto> detalles = new ArrayList<>();
                for (FacturaDetalleEntity detalle : det) {
                    FacturaDetalleDto detalleDto = new FacturaDetalleDto();
                    detalleDto.setId(detalle.getId());
                    detalleDto.setCantidad(detalle.getCantidad());
                    detalleDto.setPrecioUnitario(detalle.getPrecioUnitario());
                    detalleDto.setIva5(detalle.getIva5());
                    detalleDto.setIva10(detalle.getIva10());
                    detalleDto.setProducto(detalle.getProducto().getId());
                    detalleDto.setSubtotal(detalle.getSubtotal());
                    detalles.add(detalleDto);
                }






                FacturaCabeceraDto cabeDto = modelMapper.map(savedEntity, FacturaCabeceraDto.class);
                cabeDto.setFacturaDetalles(detalles);
                return cabeDto;
            } catch (Exception e) {
                  logger.error("Error al guardar la factura", e);
                throw new ControllerRequestException("Error al guardar la factura", e);
            }
        }



    private void imprimirFactura(FacturaCabeceraEntity entity) {

    }




    private String generateFacturaNumber() {
        String prefix = "FAC-";
        long count = facturaCabeceraRepository.count() + 1; // Obtén el número total de facturas y suma 1
        return prefix + String.format("%06d", count); // Genera un número con ceros a la izquierda
    }


    @Override
    public void delete(Long id) {
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public FacturaCabeceraDto update(Long id, FacturaCabeceraDto dto) {
        FacturaCabeceraEntity entity = facturaCabeceraRepository.findById(id)
                .orElseThrow(() -> new ControllerRequestException("No existe la factura con ID " + id));

        // Actualizar campos de la cabecera
        entity.setEsCompra(dto.getEsCompra());
        entity.setIva5(dto.getIva5());
        entity.setIva10(dto.getIva10());
        if (dto.getClienteId() != null) {
            ClienteEntity cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new ControllerRequestException("No existe el cliente con ID " + dto.getClienteId()));
            entity.setCliente(cliente);
        }

        // Actualizar detalles
        List<FacturaDetalleEntity> existingDetails = entity.getFacturaDetalles();
        facturaDetalleRepository.deleteAll(existingDetails); // Eliminar los detalles existentes

        List<FacturaDetalleEntity> updatedDetails = new ArrayList<>();
        double total = 0.0;
        double totalIva5 = 0.0;
        double totalIva10 = 0.0;

        for (FacturaDetalleDto detalleDto : dto.getFacturaDetalles()) {
            ProductoEntity producto = productoRepository.findById(detalleDto.getProducto())
                    .orElseThrow(() -> new ControllerRequestException("No existe el producto con ID " + detalleDto.getProducto()));

            FacturaDetalleEntity detalleEntity = new FacturaDetalleEntity();
            detalleEntity.setCantidad(detalleDto.getCantidad());
            detalleEntity.setPrecioUnitario(entity.getEsCompra() ? producto.getPrecioCompra() : producto.getPrecioVenta());
            detalleEntity.setIva5(producto.getIva5());
            detalleEntity.setIva10(producto.getIva10());
            detalleEntity.setProducto(producto);

            double subtotal = detalleEntity.getCantidad() * detalleEntity.getPrecioUnitario();
            double iva5 = (producto.getIva5() / 100) * subtotal;
            double iva10 = (producto.getIva10() / 100) * subtotal;

            detalleEntity.setSubtotal(subtotal);
            detalleEntity.setIva5(iva5);
            detalleEntity.setIva10(iva10);

            total += subtotal;
            totalIva5 += iva5;
            totalIva10 += iva10;

            detalleEntity.setFactura(entity);
            updatedDetails.add(detalleEntity);

            producto.setCantidad(entity.getEsCompra() ? producto.getCantidad() + detalleEntity.getCantidad() : producto.getCantidad() - detalleEntity.getCantidad());
            productoRepository.save(producto);
        }

        entity.setFacturaDetalles(updatedDetails);
        entity.setTotal(total + totalIva5 + totalIva10);
        entity.setIva5(totalIva5);
        entity.setIva10(totalIva10);

        FacturaCabeceraEntity updatedEntity = facturaCabeceraRepository.save(entity);
        List<FacturaDetalleDto> detalles = updatedEntity.getFacturaDetalles().stream()
                .map(det -> modelMapper.map(det, FacturaDetalleDto.class))
                .collect(Collectors.toList());

        FacturaCabeceraDto updatedDto = modelMapper.map(updatedEntity, FacturaCabeceraDto.class);
        updatedDto.setFacturaDetalles(detalles);
        return updatedDto;
    }

    @Transactional(readOnly = true)
    public Page<FacturaCabeceraDto> findByClienteNombreOrNumeroFactura(Pageable pageable, String nombre, String numeroFactura) {
        Page<FacturaCabeceraEntity> facturaEntities = facturaCabeceraRepository.findByClienteNombreOrNumeroFactura(pageable, nombre, numeroFactura);
        return facturaEntities.map(facturaEntity -> modelMapper.map(facturaEntity, FacturaCabeceraDto.class));
    }


}

package org.sommiersys.sommiersys.service.facturaCabecera;


import jakarta.ws.rs.NotFoundException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.hibernate.service.spi.ServiceException;
import org.modelmapper.ModelMapper;
import org.pack.sommierJar.dto.facturaCabecera.FacturaCabeceraDto;
import org.pack.sommierJar.dto.facturaDetalle.FacturaDetalleDto;
import org.pack.sommierJar.entity.cliente.ClienteEntity;
import org.pack.sommierJar.entity.facturaCabecera.FacturaCabeceraEntity;
import org.pack.sommierJar.entity.facturaDetalle.FacturaDetalleEntity;
import org.pack.sommierJar.entity.producto.ProductoEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sommiersys.sommiersys.common.interfaces.IBaseService;
import org.sommiersys.sommiersys.configuration.MapperFactura;
import org.sommiersys.sommiersys.repository.cliente.ClienteRepository;
import org.sommiersys.sommiersys.repository.facturaCabecera.FacturaCabeceraRepository;
import org.sommiersys.sommiersys.repository.facturaDetalle.FacturaDetalleRepository;
import org.sommiersys.sommiersys.repository.producto.ProductoRepository;
import org.sommiersys.sommiersys.service.email.EmailService;
import org.sommiersys.sommiersys.utils.FacturaPDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
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


    @Autowired
    CacheManager cacheManager;


    @Autowired
    private EmailService emailService;


    @Override
    @Transactional
    public Page<FacturaCabeceraDto> findAll(Pageable pageable) {
        try {
            Page<FacturaCabeceraEntity> entities = facturaCabeceraRepository.findAllByDeleted(false, pageable);

            List<FacturaCabeceraDto> cabeceraDtos = entities
                    .stream()
                    .map(entity -> MapperFactura.toDto(entity, cacheManager))
                    .collect(Collectors.toList());

            return new PageImpl<>(cabeceraDtos, pageable, entities.getTotalElements());
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al buscar las facturas: {}", e.getMessage());
            throw new ServiceException("Error al buscar las facturas");
        }
    }


    @Override
    @CachePut(cacheManager = "cacheManagerWithoutTTL", value = "sd", key = "'api_facturas_' + #id")
    @Transactional(readOnly = true)
    public Optional<FacturaCabeceraDto> findById(Long id) {
        try {
            FacturaCabeceraEntity entity = facturaCabeceraRepository.findById(id)
                    .orElseThrow(() -> new ServiceException("No existe la factura con ID " + id));

            FacturaCabeceraDto dto = MapperFactura.toDto(entity, cacheManager);

            return Optional.of(dto);
        } catch (NotFoundException e) {
            logger.error("Rollback triggered - Factura no encontrada, id={}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al actualizar la factura: {}, Parameters: id={}", e.getMessage(), id);
            throw new ServiceException("Error al actualizar la factura");
        }
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
                        .orElseThrow(() -> new ServiceException("No existe el cliente con ID " + dto.getClienteId()));
                entity.setCliente(cliente);
            }

            FacturaCabeceraEntity savedEntity = facturaCabeceraRepository.save(entity);
            List<FacturaDetalleEntity> det = new ArrayList<>();

            double total = 0.0;
            double totalIva5 = 0.0;
            double totalIva10 = 0.0;

            for (FacturaDetalleDto detalleDto : dto.getFacturaDetalles()) {
                ProductoEntity producto = productoRepository.findById(detalleDto.getProducto())
                        .orElseThrow(() -> new ServiceException("No existe el producto con ID " + detalleDto.getProducto()));

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

                // Asigno la cabecera al detalle
                detalleEntity.setFactura(savedEntity);
                det.add(detalleEntity);

                producto.setCantidad(savedEntity.getEsCompra() ? producto.getCantidad() + detalleEntity.getCantidad() : producto.getCantidad() - detalleEntity.getCantidad());

                productoRepository.save(producto);
                facturaDetalleRepository.save(detalleEntity);
            }

            savedEntity.setTotal(total + totalIva5 + totalIva10);
            savedEntity.setIva5(totalIva5);
            savedEntity.setIva10(totalIva10);

            savedEntity = facturaCabeceraRepository.save(savedEntity);

            // Convertir los detalles a DTO
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


            // Generar el PDF de la factura
            FacturaPDF facturaPDF = new FacturaPDF(facturaDetalleRepository);
            PDDocument pdfDocument = facturaPDF.generarFactura(savedEntity);

            // Convertir el PDF a un array de bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            pdfDocument.save(outputStream);
            byte[] pdfBytes = outputStream.toByteArray();

            // Cerrar el documento PDF
            pdfDocument.close();

            // Enviar el correo con el PDF adjunto
            String destinatario = savedEntity.getCliente().getEmail();  // Asegúrate de que el cliente tiene un email válido
            String asunto = "Factura #" + savedEntity.getNumeroFactura();
            String cuerpo = "Adjunto encontrarás la factura correspondiente.";
            String nombreArchivoPDF = "Factura_" + savedEntity.getNumeroFactura() + ".pdf";


            emailService.sendEmailWithPDFAttachment(destinatario, asunto, cuerpo, pdfBytes, nombreArchivoPDF);

            // Devolver el DTO de la factura guardada
            FacturaCabeceraDto cabeDto = modelMapper.map(savedEntity, FacturaCabeceraDto.class);
            cabeDto.setFacturaDetalles(detalles);
            return cabeDto;
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al guardar la factura: {}", e.getMessage());
            throw new ServiceException("Error al guardar la factura", e);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        try {
            FacturaCabeceraEntity factura = facturaCabeceraRepository.findById(id)
                    .orElseThrow(() -> new ServiceException("No existe la factura con ID " + id));

            factura.setDeleted(true);

            factura.getFacturaDetalles().forEach(detalle -> {
                detalle.setDeleted(true);
                facturaDetalleRepository.save(detalle);
            });

            facturaCabeceraRepository.save(factura);

            FacturaCabeceraDto facturaDto = MapperFactura.toDtoDelete(factura);

        } catch (NotFoundException e) {
            logger.error("Rollback triggered - Cliente no encontrado, id={}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al actualizar el cliente: {}, Parameters: id={}", e.getMessage(), id);
            throw new ServiceException("Error al actualizar el cliente");
        }
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public FacturaCabeceraDto update(Long id, FacturaCabeceraDto dto) {
        try {
            // Obtener la factura existente
            FacturaCabeceraEntity entity = facturaCabeceraRepository.findById(id)
                    .orElseThrow(() -> new ServiceException("No existe la factura con ID " + id));

            // Actualizar campos de la cabecera
            entity.setEsCompra(dto.getEsCompra());
            entity.setIva5(dto.getIva5());
            entity.setIva10(dto.getIva10());
            if (dto.getClienteId() != null) {
                ClienteEntity cliente = clienteRepository.findById(dto.getClienteId())
                        .orElseThrow(() -> new ServiceException("No existe el cliente con ID " + dto.getClienteId()));
                entity.setCliente(cliente);
            }

            // Validar y preparar los detalles de la factura antes de actualizar
            List<FacturaDetalleEntity> updatedDetails = new ArrayList<>();
            double total = 0.0;
            double totalIva5 = 0.0;
            double totalIva10 = 0.0;

            for (FacturaDetalleDto detalleDto : dto.getFacturaDetalles()) {

                // Buscar el producto en la base de datos
                ProductoEntity producto = productoRepository.findById(detalleDto.getProducto())
                        .orElseThrow(() -> new ServiceException("No existe el producto con ID " + detalleDto.getProducto()));

                // Preparar el detalle con los datos actualizados
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

                // Sumar los totales
                total += subtotal;
                totalIva5 += iva5;
                totalIva10 += iva10;

                // Agregar el detalle a la lista de actualizaciones
                updatedDetails.add(detalleEntity);

                // Actualizar la cantidad del producto
                producto.setCantidad(entity.getEsCompra() ? producto.getCantidad() + detalleEntity.getCantidad() : producto.getCantidad() - detalleEntity.getCantidad());
                productoRepository.save(producto);
            }

            // Actualizar la factura cabecera
            entity.setFacturaDetalles(updatedDetails);
            entity.setTotal(total + totalIva5 + totalIva10);
            entity.setIva5(totalIva5);
            entity.setIva10(totalIva10);

            // Guardar los cambios en la base de datos
            FacturaCabeceraEntity updatedEntity = facturaCabeceraRepository.save(entity);

            // Mapear los detalles actualizados manualmente
            List<FacturaDetalleDto> detalles = new ArrayList<>();
            for (FacturaDetalleEntity detalle : updatedEntity.getFacturaDetalles()) {
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

            // Mapear manualmente la cabecera
            FacturaCabeceraDto updatedDto = new FacturaCabeceraDto();
            updatedDto.setId(updatedEntity.getId());
            updatedDto.setNumeroFactura(updatedEntity.getNumeroFactura());
            updatedDto.setTotal(updatedEntity.getTotal());
            updatedDto.setFecha(updatedEntity.getFecha());
            updatedDto.setIva5(updatedEntity.getIva5());
            updatedDto.setIva10(updatedEntity.getIva10());
            updatedDto.setEsCompra(updatedEntity.getEsCompra());
            updatedDto.setClienteId(updatedEntity.getCliente().getId());
            updatedDto.setFacturaDetalles(detalles);

            return updatedDto;
        } catch (NotFoundException e) {
            logger.error("Rollback triggered - Cliente no encontrado, id={}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al actualizar la factura: {}, Parameters: id={}", e.getMessage(), id);
            throw new ServiceException("Error al actualizar la factura", e);
        }
    }


    @Transactional(readOnly = true)
    public Page<FacturaCabeceraDto> findByClienteNombreOrNumeroFactura(Pageable pageable, String nombre, String numeroFactura) {
        try {
            Page<FacturaCabeceraEntity> facturaEntities = facturaCabeceraRepository.findByClienteNombreOrNumeroFactura(pageable, nombre, numeroFactura, false);

            List<FacturaCabeceraDto> cabeceraDtos = facturaEntities
                    .stream()
                    .map(entity -> MapperFactura.toDto(entity, cacheManager))
                    .collect(Collectors.toList());

            return new PageImpl<>(cabeceraDtos, pageable, facturaEntities.getTotalElements());
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al buscar la factura: {}, nombre={}, numero de factura={}", e.getMessage(), nombre, numeroFactura);
            throw new ServiceException("Error al buscar la factura");
        }
    }


    @Transactional(readOnly = true)
    public Page<FacturaCabeceraDto> findByNumeroFacturaAndFecha(Pageable pageable,
                                                                String numeroFactura,
                                                                LocalDateTime startDate,
                                                                LocalDateTime endDate) {
        // Llama al repositorio para buscar las facturas
        Page<FacturaCabeceraEntity> facturaEntities = facturaCabeceraRepository.findByNumeroFacturaAndFecha(
                pageable, numeroFactura, startDate, endDate, false); // Suponiendo que 'false' es para el estado no eliminado

        // Convierte las entidades en DTOs utilizando el MapperFactura
        List<FacturaCabeceraDto> cabeceraDtos = facturaEntities
                .stream()
                .map(entity -> MapperFactura.toDto(entity, cacheManager))
                .collect(Collectors.toList());

        // Retorna el resultado paginado
        return new PageImpl<>(cabeceraDtos, pageable, facturaEntities.getTotalElements());
    }



    public String generateFacturaNumber() {
        String prefix = "FAC-";
        long count = facturaCabeceraRepository.count() + 1; // Obtén el número total de facturas y suma 1
        return prefix + String.format("%06d", count); // Genera un número con ceros a la izquierda
    }

}

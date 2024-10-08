package org.sommiersys.sommiersys.service.transacciones;


import org.modelmapper.ModelMapper;
import org.pack.sommierJar.dto.cliente.ClienteDto;
import org.pack.sommierJar.dto.facturaCabecera.FacturaCabeceraDto;
import org.pack.sommierJar.dto.facturaDetalle.FacturaDetalleDto;
import org.pack.sommierJar.entity.cliente.ClienteEntity;
import org.pack.sommierJar.entity.facturaCabecera.FacturaCabeceraEntity;
import org.pack.sommierJar.entity.facturaDetalle.FacturaDetalleEntity;
import org.pack.sommierJar.entity.producto.ProductoEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sommiersys.sommiersys.common.exception.ControllerRequestException;
import org.sommiersys.sommiersys.repository.cliente.ClienteRepository;
import org.sommiersys.sommiersys.repository.facturaCabecera.FacturaCabeceraRepository;
import org.sommiersys.sommiersys.repository.facturaDetalle.FacturaDetalleRepository;
import org.sommiersys.sommiersys.repository.producto.ProductoRepository;
import org.sommiersys.sommiersys.service.email.EmailService;
import org.sommiersys.sommiersys.service.facturaCabecera.FacturaCabeceraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransaccionesService {

    final boolean error = false;
    private static final Logger logger = LoggerFactory.getLogger(TransaccionesService.class);

    @Autowired
    private FacturaCabeceraRepository facturaCabeceraRepository;

    @Autowired
    private  ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private FacturaDetalleRepository facturaDetalleRepository;

    @Autowired
    private FacturaCabeceraService facturaCabeceraService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CacheManager cacheManager;
    ModelMapper modelMapper = new ModelMapper();

    //REQUIRED
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ClienteDto updateCliente(Long id, ClienteDto dto) {
        try {
            // Busca el cliente existente
            ClienteEntity cliente = clienteRepository.findById(id)
                    .orElseThrow(() -> new ControllerRequestException("No se ha encontrado ese cliente"));

            // Actualiza solo los campos necesarios del cliente existente
            cliente.setNombre(dto.getNombre());
            cliente.setDireccion(dto.getDireccion());
            cliente.setTelefono(dto.getTelefono());
            cliente.setCedula(dto.getCedula());
            cliente.setEmail(dto.getEmail());

            // Guarda el cliente actualizado
            clienteRepository.save(cliente);
            return modelMapper.map(cliente, ClienteDto.class);
        } catch (Exception e) {
            logger.error("Error al actualizar el cliente", e);
            throw new ControllerRequestException("Error al actualizar el cliente", e);
        }
    }

    // llamando a save sin transaction
    public ClienteDto updateClienteNotTransaction(Long id, ClienteDto dto){
        try {
            if(error) throw new RuntimeException();
            return updateCliente(id, dto);
        } catch (Exception e) {
            logger.error("error al actualizar cliente", e);
            throw new ControllerRequestException("Error al actualizar cliente",e );
        }
    }

    //llamando a save con transaction
    @Transactional
    public ClienteDto updateClienteWithTransaction(Long id, ClienteDto dto) {
        try {
            clienteRepository.findById(id);
            if (error) throw new RuntimeException();
            return updateCliente(id, dto);
        } catch (Exception e) {
            logger.error("error al buscar/actualizar cliente", e);
            throw new ControllerRequestException("Error al buscar/actualizar cliente",e );
        }
    }





    // ReadOnly false, propagation Required_new
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW ,rollbackFor = Exception.class)
    public FacturaCabeceraDto saveFacturas(FacturaCabeceraDto dto) {
        try {
            FacturaCabeceraEntity entity = new FacturaCabeceraEntity();
            entity.setEsCompra(dto.getEsCompra());
            entity.setIva5(dto.getIva5());
            entity.setIva10(dto.getIva10());
            entity.setFecha(LocalDate.from(LocalDateTime.now()));
            entity.setNumeroFactura(entity.getEsCompra() ? "0" : facturaCabeceraService.generateFacturaNumber());

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


    // llamando a save sin transaction
    public FacturaCabeceraDto saveNotTransaction(FacturaCabeceraDto facturaDto){
        try {
            FacturaCabeceraDto factura = saveFacturas(facturaDto);
            if(error) throw new RuntimeException();
            return factura;
        } catch (Exception e) {
            logger.error("Error al guardar la factura sin transaccion", e);
            throw new ControllerRequestException("Error al guardar al la factura");
        }
    }

    //llamando a save con transaction
    @Transactional
    public FacturaCabeceraDto saveWithTransaction(FacturaCabeceraDto facturaDto) {
        try {
            FacturaCabeceraDto factura = saveFacturas(facturaDto);
            // otra transaccion a db
            FacturaCabeceraEntity facturaEntity = facturaCabeceraRepository.findById(factura.getId()).orElseThrow(null);
            facturaEntity.setEsCompra(false);
            facturaCabeceraRepository.save(facturaEntity);
            if (error) throw new RuntimeException();
            return factura;
        } catch (Exception e) {
            logger.error("error al guardar la factura");
            throw new ControllerRequestException("Error al guardar la factura");
        }
    }

        // SUPPORTS, metodos de solo lectura
        @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
        public Page<FacturaCabeceraDto> findAllFacturas(Pageable pageable) {
            try {
                Page<FacturaCabeceraEntity> entities = facturaCabeceraRepository.findAllByDeleted(false,pageable);

                List<FacturaCabeceraDto> cabeceraDtos = entities
                        .stream()
                        .map(entity -> {
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

                            // Cacheando cada factura individualmente
                            String key = "api_factura_" + dto.getId();
                            Cache cache = cacheManager.getCache("facturasCache");
                            if (cache != null) {
                                FacturaCabeceraDto cachedFactura = cache.get(key, FacturaCabeceraDto.class);
                                if (cachedFactura == null) {
                                    cache.put(key, dto);
                                }
                            }

                            return dto;
                        })
                        .collect(Collectors.toList());

                return new PageImpl<>(cabeceraDtos, pageable, entities.getTotalElements());
            } catch (Exception e) {
                logger.error("Error al listar las facturas", e);
                throw new ControllerRequestException("Error al listar las facturas", e);
            }
        }




    //sin transaccional
    public Page<FacturaCabeceraDto> findAllFacturasWithoutTransactional(Pageable pageable){
        try {
            if (error) throw new RuntimeException();
            return findAllFacturas(pageable);
        } catch (Exception e) {
            logger.error("Error al eliminar la factura detalle");
            throw new ControllerRequestException("Error al eliminar los detalles");
        }
    }

    @Transactional
    public Page<FacturaCabeceraDto> findAllFacturasWithTransactional(Pageable pageable){
        try {
            if (error) throw new RuntimeException();
            return findAllFacturas(pageable);
        } catch (Exception e) {
            logger.error("Error al eliminar la factura detalle");
            throw new ControllerRequestException("Error al eliminar los detalles");
        }
    }




        //NOT_SUPPORTED
        @Transactional(propagation = Propagation.NOT_SUPPORTED, rollbackFor = Exception.class)
        public void deleteFactura(Long id) {
            // Buscar la factura por su ID
            FacturaCabeceraEntity factura = facturaCabeceraRepository.findById(id)
                    .orElseThrow(() -> new ControllerRequestException("No existe la factura con ID " + id));

            // Marcar la factura como eliminada
            factura.setDeleted(true);

            // Eliminar (marcar como eliminados) también los detalles asociados
            for (FacturaDetalleEntity detalle : factura.getFacturaDetalles()) {
                detalle.setDeleted(true);
                facturaDetalleRepository.save(detalle); // Guardar los detalles actualizados
            }

            // Guardar la factura actualizada en la base de datos
            facturaCabeceraRepository.save(factura);

            // Convertir manualmente la factura a DTO
            FacturaCabeceraDto facturaDto = new FacturaCabeceraDto();
            facturaDto.setId(factura.getId());
            facturaDto.setNumeroFactura(factura.getNumeroFactura());
            facturaDto.setEsCompra(factura.getEsCompra());
            facturaDto.setIva5(factura.getIva5());
            facturaDto.setIva10(factura.getIva10());
            facturaDto.setTotal(factura.getTotal());

            // Convertir los detalles manualmente a DTO y asignar
            List<FacturaDetalleDto> detallesDto = new ArrayList<>();
            for (FacturaDetalleEntity detalle : factura.getFacturaDetalles()) {
                FacturaDetalleDto detalleDto = new FacturaDetalleDto();
                detalleDto.setId(detalle.getId());
                detalleDto.setCantidad(detalle.getCantidad());
                detalleDto.setPrecioUnitario(detalle.getPrecioUnitario());
                detalleDto.setIva5(detalle.getIva5());
                detalleDto.setIva10(detalle.getIva10());
                detalleDto.setSubtotal(detalle.getSubtotal());
                detalleDto.setProducto(detalle.getProducto().getId());

                detallesDto.add(detalleDto);
            }

            facturaDto.setFacturaDetalles(detallesDto);

        }

    // Llamar a NOT_SUPPORTED sin transaccion
    public void deleteClienteWithoutTransaction(Long facturaId){
        try {
            deleteFactura(facturaId);
            if (error) throw new RuntimeException();

        }catch (Exception e){
            logger.error("Error al eliminar el cliente");
            throw new ControllerRequestException("Error al eliminar el cliente");

        }
    }
    @Transactional
    public void deleteClienteWithTransaction(Long facturaId){
        try {
            clienteRepository.findById(1L);
            deleteFactura(facturaId);
            if (error) throw new RuntimeException();

        }catch (Exception e){
            logger.error("Error al eliminar el cliente");
            throw new ControllerRequestException("Error al eliminar el cliente");

        }
    }





    //Never
    @Transactional(propagation = Propagation.NEVER, rollbackFor = Exception.class)
    public Boolean sendEmail(String destinatario, String asunto, String cuerpo){
        try {
            emailService.enviarCorreo(destinatario, asunto, cuerpo);
            if (error) throw new RuntimeException();
            return Boolean.TRUE;
        }catch (Exception e){
            logger.error("Error al enviar correo", e);
            throw new ControllerRequestException("Error al enviar correo", e);
        }
    }


    public Boolean sendEmailWithoutTransactional(String destinatario, String asunto, String cuerpo){
        try {
            sendEmail(destinatario, asunto, cuerpo);
            if (error) throw new RuntimeException();
            return Boolean.TRUE;
        } catch (Exception e) {
            logger.error("Error al eliminar la factura detalle");
            throw new ControllerRequestException("Error al eliminar los detalles");
        }
    }

    @Transactional
    public void sendEmialWithTransactional(String destinatario, String asunto, String cuerpo){
        try {
            if (error) throw new RuntimeException();
            sendEmail(destinatario, asunto, cuerpo);
        } catch (Exception e) {
            logger.error("Error al eliminar la factura detalle");
            throw new ControllerRequestException("Error al eliminar los detalles");
        }
    }

}

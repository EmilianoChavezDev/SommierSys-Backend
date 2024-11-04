package org.sommiersys.sommiersys.service.facturaDetalle;

import org.hibernate.service.spi.ServiceException;
import org.pack.sommierJar.dto.facturaDetalle.FacturaDetalleDto;
import org.pack.sommierJar.entity.facturaDetalle.FacturaDetalleEntity;
import org.pack.sommierJar.entity.producto.ProductoEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sommiersys.sommiersys.common.interfaces.IBaseService;
import org.sommiersys.sommiersys.repository.facturaDetalle.FacturaDetalleRepository;
import org.sommiersys.sommiersys.repository.producto.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class FacturaDetalleService implements IBaseService<FacturaDetalleDto> {

    private static final Logger logger = LoggerFactory.getLogger(FacturaDetalleService.class);

    @Autowired
    private FacturaDetalleRepository facturaDetalleRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public Page<FacturaDetalleDto> findAll(Pageable pageable) {
        try {
            return facturaDetalleRepository.findAll(pageable)
                    .map(this::entityToDto);
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al obtener todos los detalles de factura", e);
            throw new ServiceException("Error al obtener todos los detalles de factura", e);
        }
    }

    @Override
    public Optional<FacturaDetalleDto> findById(Long id) {
        try {
            return facturaDetalleRepository.findById(id)
                    .map(this::entityToDto);
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al obtener el detalle de factura con ID: {}", id, e);
            throw new ServiceException("Error al obtener el detalle de factura", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FacturaDetalleDto save(FacturaDetalleDto dto) {
        try {
            FacturaDetalleEntity entity = dtoToEntity(dto);
            entity.setSubtotal(calcularSubtotal(entity));
            FacturaDetalleEntity savedEntity = facturaDetalleRepository.save(entity);
            return entityToDto(savedEntity);
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al guardar el detalle de factura", e);
            throw new ServiceException("Error al guardar el detalle de factura", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FacturaDetalleDto update(Long id, FacturaDetalleDto dto) {
        try {
            Optional<FacturaDetalleEntity> optionalEntity = facturaDetalleRepository.findById(id);
            if (optionalEntity.isPresent()) {
                FacturaDetalleEntity entity = optionalEntity.get();
                entity.setCantidad(dto.getCantidad());
                entity.setPrecioUnitario(dto.getPrecioUnitario());
                entity.setSubtotal(calcularSubtotal(entity));
                entity.setIva5(dto.getIva5());
                entity.setIva10(dto.getIva10());
                // Mapear otros campos según sea necesario
                FacturaDetalleEntity updatedEntity = facturaDetalleRepository.save(entity);
                return entityToDto(updatedEntity);
            } else {
                throw new ServiceException("Detalle de factura no encontrado con ID: " + id);
            }
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al actualizar el detalle de factura con ID: {}", id, e);
            throw new ServiceException("Error al actualizar el detalle de factura", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED, rollbackFor = Exception.class, timeout = 10)
    public void delete(Long id) {
        try {
            FacturaDetalleEntity car = facturaDetalleRepository.findById(id)
                    .orElseThrow(() -> new ServiceException("Detalle de factura no encontrado con ID: " + id));
            facturaDetalleRepository.delete(car);
        } catch (ServiceException e) {
            logger.error("Rollback triggered - Parameters: id={}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al eliminar el detalle de factura con ID: {}, Error: {}", id, e.getMessage());
            throw new ServiceException("Error al eliminar el detalle de factura");
        }
    }


    // Método para calcular el subtotal basado en cantidad y precio unitario
    private Double calcularSubtotal(FacturaDetalleEntity entity) {
        return entity.getCantidad() * entity.getPrecioUnitario();
    }

    // Métodos de mapeo entre DTO y Entity
    private FacturaDetalleDto entityToDto(FacturaDetalleEntity entity) {
        FacturaDetalleDto dto = new FacturaDetalleDto();
        dto.setId(entity.getId());
        dto.setCantidad(entity.getCantidad());
        dto.setPrecioUnitario(entity.getPrecioUnitario());
        dto.setSubtotal(entity.getSubtotal());
        dto.setIva5(entity.getIva5());
        dto.setIva10(entity.getIva10());
        // Mapear otros campos según sea necesario
        return dto;
    }

    private FacturaDetalleEntity dtoToEntity(FacturaDetalleDto dto) {
        FacturaDetalleEntity entity = new FacturaDetalleEntity();
        entity.setId(dto.getId());
        entity.setCantidad(dto.getCantidad());
        entity.setPrecioUnitario(dto.getPrecioUnitario());
        entity.setSubtotal(dto.getSubtotal());
        entity.setIva5(dto.getIva5());
        entity.setIva10(dto.getIva10());

        // Asignar el producto a la entidad
        ProductoEntity producto = productoRepository.findById(dto.getProducto())
                .orElseThrow(() -> new ServiceException("Producto no encontrado con ID: " + dto.getProducto()));
        entity.setProducto(producto);

        return entity;
    }
}

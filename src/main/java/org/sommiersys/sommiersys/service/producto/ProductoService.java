package org.sommiersys.sommiersys.service.producto;

import org.pack.sommierJar.dto.producto.ProductoDto;
import org.pack.sommierJar.entity.producto.ProductoEntity;
import org.pack.sommierJar.entity.proveedor.ProveedorEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sommiersys.sommiersys.common.exception.ControllerRequestException;
import org.sommiersys.sommiersys.repository.producto.ProductoRepository;
import org.sommiersys.sommiersys.repository.proveedor.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<ProductoDto> findAll(Pageable pageable) {
        try {
            Page<ProductoEntity> entityPage = productoRepository.findAllByDeleted(false, pageable);
            return entityPage.map(this::convertToDto);
        } catch (Exception e) {
            logger.error("Error al listar los productos", e);
            throw new ControllerRequestException("Error al listar los productos", e);
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<ProductoDto> findById(Long id) {
        try {
            return productoRepository.findById(id).map(this::convertToDto);
        } catch (Exception e) {
            logger.error("Error al buscar el producto con ID " + id, e);
            throw new ControllerRequestException("Error al buscar el producto", e);
        }
    }

    @Transactional
    public ProductoDto save(ProductoDto dto) {
        try {
            ProductoEntity entity = convertToEntity(dto);
            ProductoEntity savedEntity = productoRepository.save(entity);
            return convertToDto(savedEntity);
        } catch (Exception e) {
            logger.error("Error al guardar el producto", e);
            throw new ControllerRequestException("Error al guardar el producto", e);
        }
    }

    @Transactional
    public void delete(Long id) {
        try {
            productoRepository.deleteById(id);
        } catch (Exception e) {
            logger.error("Error al eliminar el producto con ID " + id, e);
            throw new ControllerRequestException("Error al eliminar el producto", e);
        }
    }

    @Transactional
    public ProductoDto update(Long id, ProductoDto dto) {
        try {
            ProductoEntity existingEntity = productoRepository.findById(id)
                    .orElseThrow(() -> new ControllerRequestException("No existe el producto con ID " + id));
            ProductoEntity updatedEntity = convertToEntity(dto);
            updatedEntity.setId(existingEntity.getId());
            productoRepository.save(updatedEntity);
            return convertToDto(updatedEntity);
        } catch (Exception e) {
            logger.error("Error al actualizar el producto con ID " + id, e);
            throw new ControllerRequestException("Error al actualizar el producto", e);
        }
    }

    @Transactional
    public void updateStock(Long productoId, Integer cantidad) {
        try {
            ProductoEntity producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new ControllerRequestException("No existe el producto con ID " + productoId));
            // Aquí asumimos que el stock se gestiona con la cantidad en el producto
            producto.setCantidad(producto.getCantidad() + cantidad); // Ajustar la lógica según cómo gestiones el stock
            productoRepository.save(producto);
        } catch (Exception e) {
            logger.error("Error al actualizar el stock del producto con ID " + productoId, e);
            throw new ControllerRequestException("Error al actualizar el stock del producto", e);
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<ProductoDto> findByNombre(String nombre, Pageable pageable) {
        try {
            Page<ProductoEntity> entityPage = productoRepository.findByNombre(pageable, nombre);
            return entityPage.map(this::convertToDto);
        } catch (Exception e) {
            logger.error("Error al buscar productos por nombre " + nombre, e);
            throw new ControllerRequestException("Error al buscar los productos", e);
        }
    }

    private ProductoDto convertToDto(ProductoEntity entity) {
        ProductoDto dto = new ProductoDto();
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setMarca(entity.getMarca());
        dto.setPrecioCompra(entity.getPrecioCompra());
        dto.setPrecioVenta(entity.getPrecioVenta());
        dto.setIva5(entity.getIva5());
        dto.setIva10(entity.getIva10());
        dto.setCantidad(entity.getCantidad());
        dto.setProveedor(entity.getProveedor() != null ? entity.getProveedor().getId() : null);
        return dto;
    }

    private ProductoEntity convertToEntity(ProductoDto dto) {
        ProductoEntity entity = new ProductoEntity();
        entity.setId(dto.getId());
        entity.setNombre(dto.getNombre());
        entity.setMarca(dto.getMarca());
        entity.setPrecioCompra(dto.getPrecioCompra());
        entity.setPrecioVenta(dto.getPrecioVenta());
        entity.setIva5(dto.getIva5());
        entity.setIva10(dto.getIva10());
        entity.setCantidad(dto.getCantidad());

        if (dto.getProveedor() != null) {
            ProveedorEntity proveedor = proveedorRepository.findById(dto.getProveedor())
                    .orElseThrow(() -> new ControllerRequestException("No existe el proveedor con ID " + dto.getProveedor()));
            entity.setProveedor(proveedor);
        } else {
            entity.setProveedor(null);
        }

        return entity;
    }
}

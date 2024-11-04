package org.sommiersys.sommiersys.service.proveedor;


import jakarta.ws.rs.NotFoundException;
import org.hibernate.service.spi.ServiceException;
import org.modelmapper.ModelMapper;
import org.pack.sommierJar.dto.proveedor.ProveedorDto;
import org.pack.sommierJar.entity.proveedor.ProveedorEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sommiersys.sommiersys.common.interfaces.IBaseService;
import org.sommiersys.sommiersys.repository.cliente.ClienteRepository;
import org.sommiersys.sommiersys.repository.proveedor.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class ProveedorService implements IBaseService<ProveedorDto> {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorService.class);
    ModelMapper modelMapper = new ModelMapper();

    @Autowired
    private ProveedorRepository proveedorRepository;
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CacheManager cacheManager;


    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<ProveedorDto> findAll(Pageable pageable) {
        try {
            Page<ProveedorEntity> entityPage = proveedorRepository.findAllByDeleted(false, pageable);
            Page<ProveedorDto> proveedorResult = entityPage.map(entity -> modelMapper.map(entity, ProveedorDto.class));
            proveedorResult.forEach(cliente -> {
                String key = "api_proveedor_" + cliente.getId();
                Cache cache = cacheManager.getCache(key);
                Object proveedorCacheado = cache.get(key, Object.class);

                if (proveedorCacheado == null) {
                    cache.put(key, (Object) cliente);
                }
            });

            return proveedorResult;
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al buscar el proveedor: {}", e.getMessage());
            throw new ServiceException("Error al listar los proveedores");
        }
    }

    @Override
    @Cacheable(cacheManager = "cacheManagerWithoutTTL", value = "sd", key = "'api_proveedor_' + #id")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<ProveedorDto> findById(Long id) {
        try {
            Optional<ProveedorEntity> entityOptional = proveedorRepository.findById(id);
            return entityOptional.map(entity -> modelMapper.map(entity, ProveedorDto.class));
        } catch (NotFoundException e) {
            logger.error("Rollback triggered - Proveedor no encontrado, id={}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al buscar el proveedor: {}, id={}", e.getMessage(), id);
            throw new ServiceException("Error al buscar el proveedor");
        }

    }

    @Override
    public ProveedorDto save(ProveedorDto dto) {
        try {
            ProveedorEntity proveedorEntity = modelMapper.map(dto, ProveedorEntity.class);
            ProveedorEntity savedEntity = proveedorRepository.save(proveedorEntity);
            ProveedorDto proveedorDto = modelMapper.map(savedEntity, ProveedorDto.class);
            return proveedorDto;
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al guardar el proveedor: {}", e.getMessage());
            throw new ServiceException("Error al guardar el proveedor");
        }

    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    @CacheEvict(value = "sd", key = "'api_proveedor_' + #id")
    public void delete(Long id) {
        try {
            proveedorRepository.delete(proveedorRepository.findById(id).orElse(null));
        } catch (NotFoundException e) {
            logger.error("Rollback triggered - Parameters: id={}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al borrar el proveedor: {}, Parameters: id={}", e.getMessage(), id);
            throw new ServiceException("Error al borrar el proveedor");
        }
    }

    @Override

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    @CachePut(cacheManager = "cacheManagerWithoutTTL", value = "sd", key = "'api_proveedor_' + #id")
    public ProveedorDto update(Long id, ProveedorDto dto) {
        try {
            ProveedorEntity updatedEntity = proveedorRepository.findById(id).orElseThrow(() -> new ServiceException("No se pudo encontrar al proveedor"));
            ProveedorEntity cliente = modelMapper.map(dto, ProveedorEntity.class);
            cliente.setId(updatedEntity.getId());
            proveedorRepository.save(cliente);
            return modelMapper.map(cliente, ProveedorDto.class);
        } catch (NotFoundException e) {
            logger.error("Rollback triggered - Cliente no encontrado, id={}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al actualizar el proveedor: {}, Parameters: id={}", e.getMessage(), id);
            throw new ServiceException("Error al actualizar el proveedor");
        }
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public Page<ProveedorDto> findByNombreOrRuc(Pageable pageable, String ruc, String nombre) {
        try {
            Page<ProveedorEntity> proveedorEntities = proveedorRepository.findByNombreOrRuc(pageable, ruc, nombre);
            Page<ProveedorDto> proveedorResult = proveedorEntities.map(entity -> modelMapper.map(entity, ProveedorDto.class));
            proveedorResult.forEach(cliente -> {
                String key = "api_proveedor_" + cliente.getId();
                Cache cache = cacheManager.getCache(key);
                Object proveedorCacheado = cache.get(key, Object.class);

                if (proveedorCacheado == null) {
                    cache.put(key, (Object) cliente);
                }
            });
            return proveedorResult;
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al buscar el proveedor: {}, nombre={}, ruc={}", e.getMessage(), nombre, ruc);
            throw new ServiceException("Error al buscar el proveedor");
        }
    }
}

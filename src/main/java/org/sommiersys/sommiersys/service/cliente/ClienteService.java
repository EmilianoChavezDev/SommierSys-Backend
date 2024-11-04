package org.sommiersys.sommiersys.service.cliente;


import jakarta.ws.rs.NotFoundException;
import org.hibernate.service.spi.ServiceException;
import org.modelmapper.ModelMapper;
import org.pack.sommierJar.dto.cliente.ClienteDto;
import org.pack.sommierJar.entity.cliente.ClienteEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sommiersys.sommiersys.common.interfaces.IBaseService;
import org.sommiersys.sommiersys.repository.cliente.ClienteRepository;
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
public class ClienteService implements IBaseService<ClienteDto> {


    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);

    ModelMapper modelMapper = new ModelMapper();

    @Autowired
    private ClienteRepository clienteRepository;


    @Autowired
    private CacheManager cacheManager;


    @Override
    public Page<ClienteDto> findAll(Pageable pageable) {
        try {
            Page<ClienteEntity> clienteEntities = clienteRepository.findAllByDeleted(false, pageable);
            Page<ClienteDto> result = clienteEntities.map(clienteEntity -> modelMapper.map(clienteEntity, ClienteDto.class));

            result.forEach(cliente -> {
                String key = "api_cliente_" + cliente.getId();
                Cache cache = cacheManager.getCache(key);
                Object clienteCacheado = cache.get(key, Object.class);

                if (clienteCacheado == null) {
                    cache.put(key, cliente);
                }
            });
            return result;
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al buscar los clientes: {}", e.getMessage());
            throw new ServiceException("Error al buscar los clientes");
        }
    }


    @Override
    @Cacheable(cacheManager = "cacheManagerWithoutTTL", value = "sd", key = "'api_cliente_' + #id")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public Optional<ClienteDto> findById(Long id) {
        try {
            Optional<ClienteEntity> clienteEntity = clienteRepository.findById(id);
            return clienteEntity.map(cliente -> modelMapper.map(cliente, ClienteDto.class));
        } catch (NotFoundException e) {
            logger.error("Rollback triggered - Cliente no encontrado, id={}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al buscar el cliente: {}, id={}", e.getMessage(), id);
            throw new ServiceException("Error al buscar el cliente");
        }
    }


    @Override
    public ClienteDto save(ClienteDto dto) {
        try {
            ClienteEntity cliente = modelMapper.map(dto, ClienteEntity.class);
            ClienteEntity savedCliente = clienteRepository.save(cliente);
            return modelMapper.map(savedCliente, ClienteDto.class);
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al guardar el cliente: {}", e.getMessage());
            throw new ServiceException("Error al guardar el cliente");
        }
    }


    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    @CacheEvict(value = "sd", key = "'api_cliente_' + #id")
    public void delete(Long id) {
        try {
            clienteRepository.delete(clienteRepository.findById(id).orElse(null));
        } catch (NotFoundException e) {
            logger.error("Rollback triggered - Parameters: id={}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al borrar el cliente: {}, Parameters: id={}", e.getMessage(), id);
            throw new ServiceException("Error al borrar el cliente");
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    @CachePut(cacheManager = "cacheManagerWithoutTTL", value = "sd", key = "'api_cliente_' + #id")
    public ClienteDto update(Long id, ClienteDto dto) {
        try {
            ClienteEntity cliente = clienteRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Cliente no encontrado con ID: " + id));

            cliente.setNombre(dto.getNombre());
            cliente.setDireccion(dto.getDireccion());
            cliente.setTelefono(dto.getTelefono());
            cliente.setCedula(dto.getCedula());
            cliente.setEmail(dto.getEmail());

            clienteRepository.save(cliente);
            return modelMapper.map(cliente, ClienteDto.class);
        } catch (NotFoundException e) {
            logger.error("Rollback triggered - Cliente no encontrado, id={}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al actualizar el cliente: {}, Parameters: id={}", e.getMessage(), id);
            throw new ServiceException("Error al actualizar el cliente");
        }
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public Page<ClienteDto> findByNombreOrCedula(Pageable pageable, String nombre, String cedula) {
        if ((nombre == null || nombre.trim().isEmpty()) && (cedula == null || cedula.trim().isEmpty())) {
            throw new IllegalArgumentException("Por favor, introduzca al menos un filtro de búsqueda: nombre o cédula.");
        }

        try {
            Page<ClienteEntity> clienteEntities = clienteRepository.findByNombreOrRuc(pageable, nombre, cedula);
            Page<ClienteDto> result = clienteEntities.map(clienteEntity -> modelMapper.map(clienteEntity, ClienteDto.class));

            result.forEach(cliente -> {
                String key = "api_cliente_" + cliente.getId();
                Cache cache = cacheManager.getCache(key);
                Object clienteCacheado = cache.get(key, Object.class);

                if(clienteCacheado == null){
                    cache.put(key, cliente);
                }
            });
            return result;
        } catch (Exception e) {
            logger.error("Rollback triggered - Error al buscar el cliente: {}, nombre={}, cedula={}", e.getMessage(), nombre, cedula);
            throw new ServiceException("Error al buscar el cliente");
        }
    }



}

package org.sommiersys.sommiersys.service.cliente;


import org.modelmapper.ModelMapper;
import org.pack.sommierJar.dto.cliente.ClienteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sommiersys.sommiersys.common.exception.ControllerRequestException;
import org.sommiersys.sommiersys.common.interfaces.IBaseService;
import org.pack.sommierJar.entity.cliente.ClienteEntity;
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

                if(clienteCacheado == null){
                    cache.put(key, (Object)cliente);
                }
            });
            return result;
        } catch (Exception e) {
            logger.error("Error al buscar los clientes", e);
            throw new ControllerRequestException("Error al buscar los clientes", e);
        }
    }

    @Override
    @Cacheable(cacheManager = "cacheManagerWithoutTTL", value = "sd", key = "'api_cliente_' + #id")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public Optional<ClienteDto> findById(Long id) {
        Optional<ClienteEntity> clienteEntity = clienteRepository.findById(id);
        return clienteEntity.map(cliente -> modelMapper.map(cliente, ClienteDto.class));
    }

    @Override
    public ClienteDto save(ClienteDto dto) {
        try {
            ClienteEntity cliente = modelMapper.map(dto, ClienteEntity.class);
            ClienteEntity savedCliente = clienteRepository.save(cliente);
            ClienteDto savedDto = modelMapper.map(savedCliente, ClienteDto.class);
            return savedDto;
        } catch (Exception e) {
            logger.error("Error al guardar un cliente", e);
            throw new ControllerRequestException("No se puedo guardar el cliente", e);
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    @CacheEvict(value = "sd", key = "'api_cliente_' + #id")
    public void delete(Long id) {
        try {
            clienteRepository.delete(clienteRepository.findById(id).orElse(null));
        } catch (Exception e) {
            logger.error("Error al eliminar el cliente", e);
            throw new ControllerRequestException("Error al eliminar al cliente", e);
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    @CachePut(cacheManager = "cacheManagerWithoutTTL", value = "sd", key = "'api_cliente_' + #id")
    public ClienteDto update(Long id, ClienteDto dto) {
        try {
            ClienteEntity cliente = clienteRepository.findById(id).orElseThrow(() -> new ControllerRequestException("No se ha encontrado ese cliente") );
            ClienteEntity clienteEntity = modelMapper.map(dto, ClienteEntity.class);
            clienteEntity.setId(cliente.getId());
            clienteRepository.save(clienteEntity);
            return modelMapper.map(clienteEntity, ClienteDto.class);
        } catch (Exception e) {
            logger.error("Error al actualizar el cliente", e);
            throw new ControllerRequestException("Error al actualizar el cliente", e);
        }
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public Page<ClienteDto> findByNombreOrCedula(Pageable pageable, String nombre, String cedula) {
        // Validación: Si ambos parámetros son nulos o vacíos, lanza una excepción
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
                    cache.put(key, (Object)cliente);
                }
            });

            return result;
        } catch (Exception e) {
            logger.error("Error al buscar el cliente", e);
            throw new ControllerRequestException("Cliente no encontrado", e);
        }
    }


}

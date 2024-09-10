package org.sommiersys.sommiersys.service.proveedor;


import org.modelmapper.ModelMapper;
import org.pack.sommierJar.dto.proveedor.ProveedorDto;
import org.pack.sommierJar.entity.proveedor.ProveedorEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sommiersys.sommiersys.common.exception.ControllerRequestException;
import org.sommiersys.sommiersys.common.interfaces.IBaseService;
import org.sommiersys.sommiersys.repository.cliente.ClienteRepository;
import org.sommiersys.sommiersys.repository.proveedor.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
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


    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<ProveedorDto> findAll(Pageable pageable) {
        try {
            Page<ProveedorEntity> entityPage = proveedorRepository.findAllByDeleted(false, pageable);
            Page<ProveedorDto> proveedorResult = entityPage.map(entity -> modelMapper.map(entity, ProveedorDto.class));
            return proveedorResult;
        } catch (Exception e) {
            logger.error("Error al listar los proveedores", e);
            throw new ControllerRequestException("Error al listar los proveedores",e);
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<ProveedorDto> findById(Long id) {
        Optional<ProveedorEntity> entityOptional = proveedorRepository.findById(id);
        return entityOptional.map(entity -> modelMapper.map(entity, ProveedorDto.class));
    }

    @Override
    public ProveedorDto save(ProveedorDto dto) {
        try {
            ProveedorEntity proveedorEntity = modelMapper.map(dto, ProveedorEntity.class);
            ProveedorEntity savedEntity = proveedorRepository.save(proveedorEntity);
            ProveedorDto proveedorDto =  modelMapper.map(savedEntity, ProveedorDto.class);
            return proveedorDto;
        } catch (Exception e) {
            logger.error("Error al guardar un proveedor", e);
            throw new ControllerRequestException("Error al guardar un proveedor" ,e);
        }

    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public void delete(Long id) {
        try {
            clienteRepository.delete(clienteRepository.findById(id).orElse(null));
        } catch (Exception e) {
            logger.error("Error al eliminar un proveedor", e);
            throw new ControllerRequestException("Error al eliminar un proveedor" ,e);
        }
    }

    @Override

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public ProveedorDto update(Long id, ProveedorDto dto) {
        try {
            ProveedorEntity updatedEntity = proveedorRepository.findById(id).orElseThrow(() -> new ControllerRequestException("No se pudo encontrar al proveedor"));
            ProveedorEntity cliente = modelMapper.map(dto, ProveedorEntity.class);
            cliente.setId(updatedEntity.getId());
            proveedorRepository.save(cliente);
            return modelMapper.map(cliente, ProveedorDto.class);
        } catch (Exception e) {
            logger.error("Error al actualizar un proveedor", e);
            throw new ControllerRequestException("Error al actualizar un proveedor" ,e);
        }
    }



    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public Page<ProveedorDto> findByNombreOrRuc(Pageable pageable, String ruc, String nombre) {
        try {
            Page<ProveedorEntity> proveedorEntities = proveedorRepository.findByNombreOrRuc(pageable, ruc, nombre);
            Page<ProveedorDto> proveedorResult = proveedorEntities.map(entity -> modelMapper.map(entity, ProveedorDto.class));
            return proveedorResult;
        } catch (Exception e) {
            logger.error("Error al buscar un proveedor", e);
            throw new ControllerRequestException("Error al buscar un proveedor" ,e);
        }
    }
}

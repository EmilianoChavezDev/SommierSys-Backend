package org.sommiersys.sommiersys.controller.cliente;


import org.pack.sommierJar.dto.cliente.ClienteDto;
import org.sommiersys.sommiersys.common.interfaces.IBaseController;
import org.sommiersys.sommiersys.service.cliente.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;


@RestController
@RequestMapping("/api/clientes")
public class ClienteController implements IBaseController<ClienteDto> {


    @Autowired
    private ClienteService clienteService;




    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/findAllClientes")
    public ResponseEntity<Page<ClienteDto>> findAllClientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {

        // Si no tienes un campo específico para ordenar, debes definir uno aquí
        Sort sortDirection = sort.equalsIgnoreCase("asc") ? Sort.by("id").ascending() : Sort.by("id").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sortDirection);

        Page<ClienteDto> clientes = clienteService.findAll(pageRequest);
        return ResponseEntity.ok(clientes);
    }


    @Override
    public ResponseEntity<Page<ClienteDto>> findAll(Pageable pageable) {
        return null;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Optional<ClienteDto>> findById(@Validated @PathVariable final Long id) {
        return new ResponseEntity<>(clienteService.findById(id), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ClienteDto> save(@Validated @RequestBody final ClienteDto dto) {
        return new ResponseEntity<>(clienteService.save(dto), HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ClienteDto> update(
            @PathVariable Long id,
            @Validated @RequestBody ClienteDto dto) {
        return new ResponseEntity<>(clienteService.update(id, dto), HttpStatus.OK);
    }


    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity deleted(@PathVariable final Long id) {
        clienteService.delete(id);
        return new ResponseEntity<>("Eliminado con exito", HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/findByNombreOrCedula")
    public ResponseEntity<Page<ClienteDto>> findNombreOrCedula(@Validated @RequestParam(required = false) final String nombre,
                                                               @Validated @RequestParam(required = false) final String cedula,
                                                               Pageable pageable
                                                           ){
        return new ResponseEntity<Page<ClienteDto>>(clienteService.findByNombreOrCedula(pageable, nombre, cedula), HttpStatus.OK);
    }
}

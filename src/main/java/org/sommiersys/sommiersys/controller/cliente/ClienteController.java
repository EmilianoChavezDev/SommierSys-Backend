package org.sommiersys.sommiersys.controller.cliente;


import org.pack.sommierJar.dto.cliente.ClienteDto;
import org.sommiersys.sommiersys.common.interfaces.IBaseController;
import org.sommiersys.sommiersys.service.cliente.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api/clientes")
public class ClienteController implements IBaseController<ClienteDto> {


    @Autowired
    private ClienteService clienteService;


    @Override
    public ResponseEntity<Page<ClienteDto>> findAll(Pageable pageable) {
        Page<ClienteDto> clientes = clienteService.findAll(pageable);
        return ResponseEntity.ok(clientes);
    }

    @Override
    public ResponseEntity<Optional<ClienteDto>> findById(@Validated @PathVariable final Long id) {
        return new ResponseEntity<>(clienteService.findById(id), HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public ResponseEntity<ClienteDto> save(@Validated @RequestBody final ClienteDto dto) {
        return new ResponseEntity<>(clienteService.save(dto), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ClienteDto> update(
            @PathVariable Long id,
            @Validated @RequestBody ClienteDto dto) {
        return new ResponseEntity<>(clienteService.update(id, dto), HttpStatus.OK);
    }


    @Override
    public ResponseEntity deleted(@PathVariable final Long id) {
        clienteService.delete(id);
        return new ResponseEntity<>("Eliminado con exito", HttpStatus.OK);
    }


    @GetMapping("/findByNombreOrCedula")
    public ResponseEntity<Page<ClienteDto>> findNombreOrCedula(@Validated @RequestParam(required = false) final String nombre,
                                                               @Validated @RequestParam(required = false) final String cedula,
                                                               Pageable pageable
                                                           ){
        return new ResponseEntity<Page<ClienteDto>>(clienteService.findByNombreOrCedula(pageable, nombre, cedula), HttpStatus.OK);
    }
}

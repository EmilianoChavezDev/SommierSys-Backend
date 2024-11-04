package org.sommiersys.sommiersys.controller.proveedor;

import org.pack.sommierJar.dto.cliente.ClienteDto;
import org.pack.sommierJar.dto.proveedor.ProveedorDto;
import org.sommiersys.sommiersys.common.interfaces.IBaseController;
import org.sommiersys.sommiersys.service.proveedor.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController implements IBaseController<ProveedorDto> {

    @Autowired
    private ProveedorService proveedorService;

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Page<ProveedorDto>> findAll(Pageable pageable) {
        return ResponseEntity.ok(proveedorService.findAll(pageable));
    }

    @GetMapping("/findAllProveedores")
    public ResponseEntity<Page<ProveedorDto>> findAllClientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {

        // Si no tienes un campo específico para ordenar, debes definir uno aquí
        Sort sortDirection = sort.equalsIgnoreCase("asc") ? Sort.by("id").ascending() : Sort.by("id").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sortDirection);

        Page<ProveedorDto> clientes = proveedorService.findAll(pageRequest);
        return ResponseEntity.ok(clientes);
    }



    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Optional<ProveedorDto>> findById(@Validated @PathVariable final Long id) {
        return ResponseEntity.ok(proveedorService.findById(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ProveedorDto> save(@Validated @RequestBody final ProveedorDto dto) {
        return ResponseEntity.ok(proveedorService.save(dto));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity update(@PathVariable Long id, @Validated @RequestBody ProveedorDto dto) {
        return ResponseEntity.ok(proveedorService.update(id, dto));
    }

    @GetMapping("/findByNombreOrRuc")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Page<ProveedorDto>> findNombreOrRuc(@Validated @RequestParam(required = false) final String nombre,
                                                              @Validated @RequestParam(required = false) final String ruc,
                                                              Pageable pageable) {
        return new ResponseEntity<>(proveedorService.findByNombreOrRuc(pageable, nombre, ruc), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity deleted(@PathVariable final Long id) {
        proveedorService.delete(id);
        return new ResponseEntity<>("Proveedor eliminado con exito", HttpStatus.OK);
    }
}


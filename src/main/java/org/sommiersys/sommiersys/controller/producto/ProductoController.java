package org.sommiersys.sommiersys.controller.producto;

import org.pack.sommierJar.dto.producto.ProductoDto;
import org.pack.sommierJar.dto.proveedor.ProveedorDto;
import org.sommiersys.sommiersys.common.interfaces.IBaseController;
import org.sommiersys.sommiersys.service.producto.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
public class ProductoController implements IBaseController<ProductoDto> {


    @Autowired
    private ProductoService productoService;

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Page<ProductoDto>> findAll(Pageable pageable) {
        Page<ProductoDto> productos = productoService.findAll(pageable);
        return new ResponseEntity<>(productos, HttpStatus.OK);
    }

    @GetMapping("/findAllProductos")
    public ResponseEntity<Page<ProductoDto>> findAllProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {

        // Si no tienes un campo específico para ordenar, debes definir uno aquí
        Sort sortDirection = sort.equalsIgnoreCase("asc") ? Sort.by("id").ascending() : Sort.by("id").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sortDirection);

        Page<ProductoDto> clientes = productoService.findAll(pageRequest);
        return ResponseEntity.ok(clientes);
    }


    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Optional<ProductoDto>> findById(@Validated @PathVariable Long id) {
        return ResponseEntity.ok(productoService.findById(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ProductoDto> save(@Validated @RequestBody final ProductoDto dto) {
        return new ResponseEntity<>(productoService.save(dto), HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity update(@Validated @PathVariable final Long id,@Validated @RequestBody ProductoDto dto) {
        return new ResponseEntity<>(productoService.update(id, dto), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity deleted(@PathVariable final Long id) {
        productoService.delete(id);
        return new ResponseEntity<>("Producto eliminado con exito", HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/findByNombre")
    public ResponseEntity<Page<ProductoDto>> findByNombre(Pageable pageable,@Validated @RequestParam String nombre) {
        return new ResponseEntity<Page<ProductoDto>>(productoService.findByNombre(nombre, pageable), HttpStatus.OK);
    }


}


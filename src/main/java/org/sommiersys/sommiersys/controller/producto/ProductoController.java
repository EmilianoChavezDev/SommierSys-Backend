package org.sommiersys.sommiersys.controller.producto;

import org.pack.sommierJar.dto.producto.ProductoDto;
import org.sommiersys.sommiersys.common.interfaces.IBaseController;
import org.sommiersys.sommiersys.service.producto.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
public class ProductoController implements IBaseController<ProductoDto> {


    @Autowired
    private ProductoService productoService;

    @Override
    public ResponseEntity<Page<ProductoDto>> findAll(Pageable pageable) {
        Page<ProductoDto> productos = productoService.findAll(pageable);
        return new ResponseEntity<>(productos, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Optional<ProductoDto>> findById(@Validated @PathVariable Long id) {
        return ResponseEntity.ok(productoService.findById(id));
    }

    @Override
    public ResponseEntity<ProductoDto> save(@Validated @RequestBody final ProductoDto dto) {
        return new ResponseEntity<>(productoService.save(dto), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity update(@Validated @PathVariable final Long id,@Validated @RequestBody ProductoDto dto) {
        return new ResponseEntity<>(productoService.update(id, dto), HttpStatus.OK);
    }

    @Override
    public ResponseEntity deleted(@PathVariable final Long id) {
        productoService.delete(id);
        return new ResponseEntity<>("Producto eliminado con exito", HttpStatus.OK);
    }
    @GetMapping("/findByNombre")
    public ResponseEntity<Page<ProductoDto>> findByNombre(Pageable pageable,@Validated @RequestParam String nombre) {
        return new ResponseEntity<Page<ProductoDto>>(productoService.findByNombre(nombre, pageable), HttpStatus.OK);
    }


}


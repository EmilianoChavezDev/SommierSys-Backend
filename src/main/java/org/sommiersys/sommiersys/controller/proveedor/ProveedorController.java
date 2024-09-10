package org.sommiersys.sommiersys.controller.proveedor;

import org.pack.sommierJar.dto.proveedor.ProveedorDto;
import org.sommiersys.sommiersys.common.interfaces.IBaseController;
import org.sommiersys.sommiersys.repository.proveedor.ProveedorRepository;
import org.sommiersys.sommiersys.service.proveedor.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController implements IBaseController<ProveedorDto> {



    @Autowired
    private ProveedorService proveedorService;
    @Autowired
    private ProveedorRepository proveedorRepository;


    @Override
    public ResponseEntity<Page<ProveedorDto>> findAll(Pageable pageable) {
        return ResponseEntity.ok(proveedorService.findAll(pageable));
    }

    @Override
    public ResponseEntity<Optional<ProveedorDto>> findById(@Validated @PathVariable final Long id) {
        return ResponseEntity.ok(proveedorService.findById(id));
    }

    @Override
    public ResponseEntity<ProveedorDto> save(@Validated @RequestBody final ProveedorDto dto) {
        return ResponseEntity.ok(proveedorService.save(dto));
    }

    @Override
    public ResponseEntity update(@PathVariable Long id,@Validated @RequestBody ProveedorDto dto) {
        return ResponseEntity.ok(proveedorService.update(id, dto));
    }


    @GetMapping("/findByNombreOrRuc")
    public ResponseEntity<Page<ProveedorDto>> findNombreOrRuc(@Validated @RequestParam(required = false) final String nombre,
                                                               @Validated @RequestParam(required = false) final String ruc,
                                                               Pageable pageable
    ){
        return new ResponseEntity<Page<ProveedorDto>>(proveedorService.findByNombreOrRuc(pageable, nombre, ruc), HttpStatus.OK);
    }

    @Override
    public ResponseEntity deleted(@PathVariable final Long id) {
        proveedorService.delete(id);
        return new ResponseEntity("Proveedor eliminado con exito",HttpStatus.OK);
    }
}

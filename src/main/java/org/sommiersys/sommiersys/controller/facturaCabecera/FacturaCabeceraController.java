package org.sommiersys.sommiersys.controller.facturaCabecera;

import org.pack.sommierJar.dto.facturaCabecera.FacturaCabeceraDto;
import org.sommiersys.sommiersys.common.interfaces.IBaseController;

import org.sommiersys.sommiersys.service.facturaCabecera.FacturaCabeceraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/facturas_cabecera")
public class FacturaCabeceraController implements IBaseController<FacturaCabeceraDto> {

    @Autowired
    private FacturaCabeceraService facturaCabeceraService;

    @Override
    @GetMapping
    public ResponseEntity<Page<FacturaCabeceraDto>> findAll(Pageable pageable) {
        Page<FacturaCabeceraDto> result = facturaCabeceraService.findAll(pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Optional<FacturaCabeceraDto>> findById(@PathVariable Long id) {
        Optional<FacturaCabeceraDto> factura = facturaCabeceraService.findById(id);
        if (factura.isPresent()) {
            return new ResponseEntity<>(factura, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @PostMapping
    public ResponseEntity<FacturaCabeceraDto> save(@RequestBody FacturaCabeceraDto dto) {
        FacturaCabeceraDto savedFactura = facturaCabeceraService.save(dto);
        return new ResponseEntity<>(savedFactura, HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<FacturaCabeceraDto> update(@PathVariable Long id, @RequestBody FacturaCabeceraDto dto) {
        FacturaCabeceraDto updatedFactura = facturaCabeceraService.update(id, dto);
        if (updatedFactura != null) {
            return new ResponseEntity<>(updatedFactura, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleted(@PathVariable Long id) {
        facturaCabeceraService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @GetMapping("/findByNombreOrNumero")
    public ResponseEntity<Page<FacturaCabeceraDto>> findByNombreOrNumero(
            Pageable pageable,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String numeroFactura) {

        Page<FacturaCabeceraDto> results = facturaCabeceraService.findByClienteNombreOrNumeroFactura(pageable, nombre, numeroFactura);
        return ResponseEntity.ok(results);
    }
}

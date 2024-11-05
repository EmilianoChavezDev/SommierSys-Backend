package org.sommiersys.sommiersys.controller.facturaCabecera;

import org.pack.sommierJar.dto.facturaCabecera.FacturaCabeceraDto;
import org.pack.sommierJar.dto.producto.ProductoDto;
import org.sommiersys.sommiersys.common.interfaces.IBaseController;

import org.sommiersys.sommiersys.service.facturaCabecera.FacturaCabeceraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/facturas_cabecera")
public class FacturaCabeceraController implements IBaseController<FacturaCabeceraDto> {

    @Autowired
    private FacturaCabeceraService facturaCabeceraService;

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Page<FacturaCabeceraDto>> findAll(Pageable pageable) {
        Page<FacturaCabeceraDto> result = facturaCabeceraService.findAll(pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @GetMapping("/findAllFacturas")
    public ResponseEntity<Page<FacturaCabeceraDto>> findAllFacturas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {

        // Si no tienes un campo específico para ordenar, debes definir uno aquí
        Sort sortDirection = sort.equalsIgnoreCase("asc") ? Sort.by("id").ascending() : Sort.by("id").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sortDirection);

        Page<FacturaCabeceraDto> clientes = facturaCabeceraService.findAll(pageRequest);
        return ResponseEntity.ok(clientes);
    }


    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Optional<FacturaCabeceraDto>> findById(@PathVariable Long id) {
        Optional<FacturaCabeceraDto> factura = facturaCabeceraService.findById(id);
        if (factura.isPresent()) {
            return new ResponseEntity<>(factura, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<FacturaCabeceraDto> save(@RequestBody FacturaCabeceraDto dto) {
        FacturaCabeceraDto savedFactura = facturaCabeceraService.save(dto);
        return new ResponseEntity<>(savedFactura, HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacturaCabeceraDto> update(@PathVariable Long id, @RequestBody FacturaCabeceraDto dto) {
        FacturaCabeceraDto updatedFactura = facturaCabeceraService.update(id, dto);
        if (updatedFactura != null) {
            return new ResponseEntity<>(updatedFactura, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleted(@PathVariable Long id) {
        facturaCabeceraService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/findByNombreOrNumero")
    public ResponseEntity<Page<FacturaCabeceraDto>> findByNombreOrNumero(
            Pageable pageable,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String numeroFactura) {

        Page<FacturaCabeceraDto> results = facturaCabeceraService.findByClienteNombreOrNumeroFactura(pageable, nombre, numeroFactura);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/findByFecha")
    public ResponseEntity<Page<FacturaCabeceraDto>> buscarPorNumeroFacturaYFecha(
            @RequestParam(required = false) String numeroFactura,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {

        // Define la dirección de ordenamiento
        Sort sortDirection = sort.equalsIgnoreCase("asc") ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sortDirection);

        // Llama al servicio para buscar las facturas
        Page<FacturaCabeceraDto> facturas = facturaCabeceraService.findByNumeroFacturaAndFecha(pageRequest, numeroFactura, startDate, endDate);

        return ResponseEntity.ok(facturas);
    }

}

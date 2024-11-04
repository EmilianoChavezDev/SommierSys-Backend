//package org.sommiersys.sommiersys.controller.transacciones;
//
//
//import org.pack.sommierJar.dto.cliente.ClienteDto;
//import org.pack.sommierJar.dto.facturaCabecera.FacturaCabeceraDto;
//import org.sommiersys.sommiersys.service.transacciones.TransaccionesService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/transacciones")
//public class TransaccionesController {
//
//    @Autowired
//    private TransaccionesService transaccionesService;
//
//
//
//    // required
//    @PutMapping("/clientes/{id}/sin-transaccion")
//    public ResponseEntity<ClienteDto> updateClienteSinTransaccion(@PathVariable Long id, @RequestBody ClienteDto clienteDto) {
//        ClienteDto updatedCliente = transaccionesService.updateClienteNotTransaction(id, clienteDto);
//        return ResponseEntity.ok(updatedCliente);
//    }
//
//
//    @PutMapping("/clientes/{id}/con-transaccion")
//    public ResponseEntity<ClienteDto> updateClienteConTransaccion(@PathVariable Long id, @RequestBody ClienteDto clienteDto) {
//        ClienteDto updatedCliente = transaccionesService.updateClienteWithTransaction(id, clienteDto);
//        return ResponseEntity.ok(updatedCliente);
//    }
//
//
//    // requires_new
//    @PostMapping("/facturas/sin-transaccion")
//    public ResponseEntity<FacturaCabeceraDto> saveFacturaSinTransaccion(@RequestBody FacturaCabeceraDto facturaDto) {
//        FacturaCabeceraDto savedFactura = transaccionesService.saveNotTransaction(facturaDto);
//        return ResponseEntity.ok(savedFactura);
//    }
//
//
//    @PostMapping("/facturas/con-transaccion")
//    public ResponseEntity<FacturaCabeceraDto> saveFacturaConTransaccion(@RequestBody FacturaCabeceraDto facturaDto) {
//        FacturaCabeceraDto savedFactura = transaccionesService.saveWithTransaction(facturaDto);
//        return ResponseEntity.ok(savedFactura);
//    }
//
//
//    // Supports
//    @GetMapping("/facturas/sin-transaccion")
//    public ResponseEntity<Page<FacturaCabeceraDto>> getAllFacturasSinTransaccion(Pageable pageable) {
//        Page<FacturaCabeceraDto> facturas = transaccionesService.findAllFacturasWithoutTransactional(pageable);
//        return ResponseEntity.ok(facturas);
//    }
//
//
//    @GetMapping("/facturas/con-transaccion")
//    public ResponseEntity<Page<FacturaCabeceraDto>> getAllFacturasConTransaccion(Pageable pageable) {
//        Page<FacturaCabeceraDto> facturas = transaccionesService.findAllFacturasWithTransactional(pageable);
//        return ResponseEntity.ok(facturas);
//    }
//
//
//
//    //Not_supported
//    @DeleteMapping("/facturas/{id}/sin-transaccion")
//    public ResponseEntity<Void> deleteFacturaSinTransaccion(@PathVariable Long id) {
//        transaccionesService.deleteClienteWithoutTransaction(id);
//        return ResponseEntity.noContent().build();
//    }
//
//
//    @DeleteMapping("/facturas/{id}/con-transaccion")
//    public ResponseEntity<Void> deleteFacturaConTransaccion(@PathVariable Long id) {
//        transaccionesService.deleteClienteWithTransaction(id);
//        return ResponseEntity.noContent().build();
//    }
//
//
//
//
//    //Never
//    @PostMapping("/enviar-email/sin-transaccion")
//    public ResponseEntity<Boolean> sendEmailSinTransaccion(@RequestParam String destinatario, @RequestParam String asunto, @RequestParam String cuerpo) {
//        return ResponseEntity.ok(transaccionesService.sendEmailWithoutTransactional(destinatario, asunto, cuerpo));
//    }
//
//
//    @PostMapping("/enviar-email/con-transaccion")
//    public ResponseEntity<?> sendEmailConTransaccion(@RequestParam String destinatario, @RequestParam String asunto, @RequestParam String cuerpo) {
//        transaccionesService.sendEmialWithTransactional(destinatario, asunto, cuerpo);
//        return new ResponseEntity<>("Enviado con exito", HttpStatus.OK);
//    }
//}

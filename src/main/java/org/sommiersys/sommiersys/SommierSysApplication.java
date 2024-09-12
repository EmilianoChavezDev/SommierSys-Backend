package org.sommiersys.sommiersys;

import org.pack.sommierJar.dto.cliente.ClienteDto;
import org.pack.sommierJar.dto.facturaCabecera.FacturaCabeceraDto;
import org.sommiersys.sommiersys.common.entity.facturaDetalle.FacturaDetalleEntity;
import org.sommiersys.sommiersys.service.facturaCabecera.FacturaCabeceraService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;


@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
@EntityScan(basePackages = "org.pack.sommierJar.*")
public class SommierSysApplication {

    public static void main(String[] args) {
        SpringApplication.run(SommierSysApplication.class, args);

    }

}

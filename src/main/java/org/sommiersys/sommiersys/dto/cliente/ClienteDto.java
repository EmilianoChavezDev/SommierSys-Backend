package org.sommiersys.sommiersys.dto.cliente;


import jakarta.validation.constraints.Email;
import lombok.Data;

import java.io.Serializable;

@Data
public class ClienteDto implements Serializable {
    private Long id;
    private String nombre;
    private String telefono;
    private String direccion;
    private String cedula;
    @Email
    private String email;
}

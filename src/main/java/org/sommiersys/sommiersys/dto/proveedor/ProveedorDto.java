package org.sommiersys.sommiersys.dto.proveedor;


import jakarta.validation.constraints.Email;
import lombok.Data;

import java.io.Serializable;

@Data
public class ProveedorDto implements Serializable {


    private Long id;
    private String nombre;
    private String ruc;
    private String direccion;
    private String telefono;
    @Email
    private String email;


}

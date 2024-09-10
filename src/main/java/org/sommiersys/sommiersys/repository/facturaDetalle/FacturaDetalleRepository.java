package org.sommiersys.sommiersys.repository.facturaDetalle;


import org.pack.sommierJar.entity.facturaDetalle.FacturaDetalleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacturaDetalleRepository extends JpaRepository<FacturaDetalleEntity, Long> {

//    List<FacturaDetalleEntity> findByFacturaId(Long facturaId);
}

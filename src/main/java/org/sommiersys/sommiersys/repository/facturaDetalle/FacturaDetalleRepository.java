package org.sommiersys.sommiersys.repository.facturaDetalle;


import org.pack.sommierJar.entity.facturaDetalle.FacturaDetalleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacturaDetalleRepository extends JpaRepository<FacturaDetalleEntity, Long> {

    @Query(value = "SELECT * FROM FACTURAS_DETALLE WHERE factura_id =?", nativeQuery = true)
    List<FacturaDetalleEntity> findAllbyFacturaId(final Long id);

//    List<FacturaDetalleEntity> findByFacturaId(Long facturaId);
}

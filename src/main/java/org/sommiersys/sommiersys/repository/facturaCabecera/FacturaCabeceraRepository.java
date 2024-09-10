package org.sommiersys.sommiersys.repository.facturaCabecera;


import org.pack.sommierJar.entity.facturaCabecera.FacturaCabeceraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

import java.util.Optional;


@Repository
public interface FacturaCabeceraRepository extends JpaRepository<FacturaCabeceraEntity, Long> {


    Optional<FacturaCabeceraEntity> findById(Long id);

    Page<FacturaCabeceraEntity> findAllByDeleted(boolean deleted, Pageable pageable);

    // Buscar facturas por nombre de cliente o número de factura
    @Query(value = "SELECT * FROM FACTURAS_CABECERA fc " +
            "JOIN CLIENTES c ON fc.CLIENTE_ID = c.ID " +
            "WHERE (:nombre IS NULL OR c.NOMBRE ILIKE %:nombre%) " +
            "AND (:numeroFactura IS NULL OR fc.NUMERO_FACTURA ILIKE %:numeroFactura%)",
            nativeQuery = true)
    Page<FacturaCabeceraEntity> findByClienteNombreOrNumeroFactura(Pageable pageable,
                                                                   String nombre,
                                                                   String numeroFactura);



    @Query(value = "SELECT * FROM FACTURAS_CABECERA WHERE CREATED_AT BETWEEN :startDate AND :endDate", nativeQuery = true)
    Page<FacturaCabeceraEntity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}

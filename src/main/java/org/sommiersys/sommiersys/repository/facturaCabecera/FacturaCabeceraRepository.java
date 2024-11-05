package org.sommiersys.sommiersys.repository.facturaCabecera;


import org.pack.sommierJar.entity.facturaCabecera.FacturaCabeceraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;


@Repository
public interface FacturaCabeceraRepository extends JpaRepository<FacturaCabeceraEntity, Long> {


    Optional<FacturaCabeceraEntity> findById(Long id);

    Page<FacturaCabeceraEntity> findAllByDeleted(boolean deleted, Pageable pageable);

    @Query(value = "SELECT fc.*, c.NOMBRE AS clienteNombre FROM FACTURAS_CABECERA fc " +
            "JOIN CLIENTES c ON fc.CLIENTE_ID = c.ID " +
            "WHERE (:nombre IS NULL OR c.NOMBRE ILIKE %:nombre%) " +
            "AND (:numeroFactura IS NULL OR fc.NUMERO_FACTURA ILIKE %:numeroFactura%) " +
            "AND fc.deleted = :deleted",
            nativeQuery = true)
    Page<FacturaCabeceraEntity> findByClienteNombreOrNumeroFactura(Pageable pageable,
                                                                   @Param("nombre") String nombre,
                                                                   @Param("numeroFactura") String numeroFactura,
                                                                   @Param("deleted") boolean deleted);



    @Query(value = "SELECT * FROM FACTURAS_CABECERA fc where fc.cliente_id = ?", nativeQuery = true)
    List<FacturaCabeceraEntity> findByClienteId(Long id);




    @Query(value = "SELECT * FROM FACTURAS_CABECERA WHERE CREATED_AT BETWEEN :startDate AND :endDate", nativeQuery = true)
    Page<FacturaCabeceraEntity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);


    @Query(value = "SELECT fc.* FROM FACTURAS_CABECERA fc " +
            "WHERE (:numeroFactura IS NULL OR fc.NUMERO_FACTURA ILIKE %:numeroFactura%) " +
            "AND (:startDate IS NULL OR fc.CREATED_AT >= :startDate) " +
            "AND (:endDate IS NULL OR fc.CREATED_AT <= :endDate) " +
            "AND fc.deleted = :deleted",
            nativeQuery = true)
    Page<FacturaCabeceraEntity> findByNumeroFacturaAndFecha(
            Pageable pageable,
            @Param("numeroFactura") String numeroFactura,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("deleted") boolean deleted);

}

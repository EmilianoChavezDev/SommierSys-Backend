package org.sommiersys.sommiersys.repository.cliente;


import org.pack.sommierJar.entity.cliente.ClienteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {

    Optional<ClienteEntity> findById(Long id);

    Page<ClienteEntity> findAllByDeleted(boolean deleted, Pageable pageable);

    @Query(value = "SELECT * FROM CLIENTES WHERE CLIENTES.ID = ?1 AND CLIENTES.DELETED = ?2", nativeQuery = true)
    Optional<ClienteEntity> findByIdAndDelete(Long id, boolean estado);


    @Query(value = "SELECT * FROM CLIENTES WHERE (:nombre IS NULL OR CLIENTES.NOMBRE ILIKE %:nombre%) AND (:cedula IS NULL OR CLIENTES.CEDULA ILIKE %:cedula%)", nativeQuery = true)
    Page<ClienteEntity> findByNombreOrRuc(Pageable pageable,
                                                    final String nombre,
                                                    final String cedula);
    @Query(value = "SELECT email FROM CLIENTES", nativeQuery = true)
    List<String> correos();
}

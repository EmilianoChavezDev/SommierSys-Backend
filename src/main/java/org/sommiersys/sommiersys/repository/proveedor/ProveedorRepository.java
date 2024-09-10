package org.sommiersys.sommiersys.repository.proveedor;



import org.pack.sommierJar.entity.proveedor.ProveedorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<ProveedorEntity, Long> {

    Optional<ProveedorEntity> findById(Long id);

    Page<ProveedorEntity> findAllByDeleted(boolean deleted, Pageable pageable);

    @Query(value = "SELECT * FROM PROVEEDORES WHERE PROVEEDORES.ID = ?1 AND PROVEEDORES.DELETED = ?2", nativeQuery = true)
    Optional<ProveedorEntity> findByIdAndDelete(Long id, boolean estado);


    Page<ProveedorEntity> findAllBy(Pageable pageable);


    @Query(value = "SELECT * FROM PROVEEDORES WHERE (:nombre IS NULL OR PROVEEDORES.NOMBRE ILIKE %:nombre%) AND (:ruc IS NULL OR PROVEEDORES.RUC ILIKE %:ruc%)", nativeQuery = true)
    Page<ProveedorEntity> findByNombreOrRuc(Pageable pageable,
                                                    final String nombre,
                                                    final String ruc);



    @Query(value = "SELECT email FROM PROVEEDORES", nativeQuery = true)
    List<String> correos();

}

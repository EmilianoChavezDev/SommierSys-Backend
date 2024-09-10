package org.sommiersys.sommiersys.repository.producto;


import org.pack.sommierJar.entity.producto.ProductoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {


    Optional<ProductoEntity> findById(Long id);

    Page<ProductoEntity> findAllByDeleted(boolean deleted, Pageable pageable);

    @Query(value = "SELECT * FROM PRODUCTOS WHERE PRODUCTOS.ID = ?1 AND PRODUCTOS.DELETED = ?2", nativeQuery = true)
    Optional<ProductoEntity> findByIdAndDelete(Long id, boolean estado);

    @Query(value = "SELECT * FROM PRODUCTOS WHERE (:nombre IS NULL OR PRODUCTOS.NOMBRE ILIKE %:nombre%)", nativeQuery = true)
    Page<ProductoEntity> findByNombre(Pageable pageable,
                                          final String nombre
                                        );
}

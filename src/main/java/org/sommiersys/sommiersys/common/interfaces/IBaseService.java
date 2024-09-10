package org.sommiersys.sommiersys.common.interfaces;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IBaseService<E> {

    public Page<E> findAll(Pageable pageable);

    Optional<E> findById(final Long id);

    public E save(final E dto);

    public void delete(final Long id);

    public E update(final Long id, final E dto);


}
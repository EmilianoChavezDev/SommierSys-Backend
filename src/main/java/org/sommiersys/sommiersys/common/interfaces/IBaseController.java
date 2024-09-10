package org.sommiersys.sommiersys.common.interfaces;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.Optional;

public interface IBaseController<E> {

    @GetMapping("/")
    ResponseEntity<Page<E>> findAll(final Pageable pageable);

    @GetMapping("/{id}")
    ResponseEntity<Optional<E>> findById(@Validated Long id);

    @PostMapping("/")
    ResponseEntity<E> save(@Validated final E dto);

    @PutMapping("/{id}")
    ResponseEntity update(@Validated final Long id, @Validated final E dto);

    @DeleteMapping("/{id}")
    ResponseEntity deleted(@Validated final Long id);
}

package org.sommiersys.sommiersys.service.facturaDetalle;



import org.pack.sommierJar.dto.facturaDetalle.FacturaDetalleDto;
import org.pack.sommierJar.entity.facturaDetalle.FacturaDetalleEntity;
import org.sommiersys.sommiersys.common.interfaces.IBaseService;
import org.sommiersys.sommiersys.repository.facturaDetalle.FacturaDetalleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FacturaDetalleService implements IBaseService<FacturaDetalleDto> {

    @Autowired
    private FacturaDetalleRepository facturaDetalleRepository;

    @Override
    public Page<FacturaDetalleDto> findAll(Pageable pageable) {
       return null;}

    @Override
    public Optional<FacturaDetalleDto> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public FacturaDetalleDto save(FacturaDetalleDto dto) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public FacturaDetalleDto update(Long id, FacturaDetalleDto dto) {
        return null;
    }

//    public Double calcularTotalPorFactura(Long facturaId) {
//        List<FacturaDetalleEntity> detalles = facturaDetalleRepository.findByFacturaId(facturaId);
//        return detalles.stream()
//                .mapToDouble(FacturaDetalleEntity::getSubtotal)
//                .sum();
//    }
}

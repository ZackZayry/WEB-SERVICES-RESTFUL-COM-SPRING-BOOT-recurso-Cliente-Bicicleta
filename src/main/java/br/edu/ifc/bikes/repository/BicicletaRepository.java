package br.edu.ifc.bikes.repository;

import br.edu.ifc.bikes.entity.Bicicleta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BicicletaRepository extends JpaRepository<Bicicleta, Long> {
    boolean existsByNumeroSerie(String numeroSerie);
    boolean existsByNumeroSerieAndIdNot(String numeroSerie, Long id);
    List<Bicicleta> findByClienteId(Long clienteId);
}

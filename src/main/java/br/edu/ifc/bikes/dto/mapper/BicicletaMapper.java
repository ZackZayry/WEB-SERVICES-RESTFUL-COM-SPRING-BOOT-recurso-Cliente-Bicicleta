package br.edu.ifc.bikes.dto.mapper;

import br.edu.ifc.bikes.dto.BicicletaRequestDTO;
import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.entity.Bicicleta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BicicletaMapper {

    // o cliente é associado pelo service (busca por clienteId)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    Bicicleta toEntity(BicicletaRequestDTO dto);

    @Mapping(target = "clienteId", source = "cliente.id")
    BicicletaResponseDTO toResponse(Bicicleta bicicleta);

    List<BicicletaResponseDTO> toResponse(List<Bicicleta> bicicletas);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    void updateEntity(BicicletaRequestDTO dto, @MappingTarget Bicicleta bicicleta);
}

package br.edu.ifc.bikes.dto.mapper;

import br.edu.ifc.bikes.dto.ClienteRequestDTO;
import br.edu.ifc.bikes.dto.ClienteResponseDTO;
import br.edu.ifc.bikes.dto.ClienteUpdateRequestDTO;
import br.edu.ifc.bikes.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    // a senha vem em texto puro aqui; o service é quem criptografa antes de salvar
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bicicletas", ignore = true)
    Cliente toEntity(ClienteRequestDTO dto);

    ClienteResponseDTO toResponse(Cliente cliente);

    List<ClienteResponseDTO> toResponse(List<Cliente> clientes);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senha", ignore = true)
    @Mapping(target = "bicicletas", ignore = true)
    void updateEntity(ClienteUpdateRequestDTO dto, @MappingTarget Cliente cliente);
}

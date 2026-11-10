package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.BicicletaRequestDTO;
import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.dto.mapper.BicicletaMapper;
import br.edu.ifc.bikes.entity.Bicicleta;
import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.exception.RecursoDuplicadoException;
import br.edu.ifc.bikes.repository.BicicletaRepository;
import br.edu.ifc.bikes.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BicicletaService {

    private final BicicletaRepository bicicletaRepository;
    private final ClienteRepository clienteRepository;
    private final BicicletaMapper bicicletaMapper;

    @Transactional
    public BicicletaResponseDTO criar(BicicletaRequestDTO dto) {
        Cliente cliente = buscarCliente(dto.clienteId());
        if (bicicletaRepository.existsByNumeroSerie(dto.numeroSerie())) {
            throw new RecursoDuplicadoException(
                    String.format("O número de série '%s' já está cadastrado", dto.numeroSerie()));
        }
        Bicicleta bicicleta = bicicletaMapper.toEntity(dto);
        bicicleta.setCliente(cliente);
        return bicicletaMapper.toResponse(bicicletaRepository.save(bicicleta));
    }

    /** Lista todas as bicicletas ou, se clienteId for informado, apenas as do cliente. */
    @Transactional(readOnly = true)
    public List<BicicletaResponseDTO> listar(Long clienteId) {
        List<Bicicleta> bicicletas = (clienteId == null)
                ? bicicletaRepository.findAll()
                : bicicletaRepository.findByClienteId(clienteId);
        return bicicletaMapper.toResponse(bicicletas);
    }

    @Transactional(readOnly = true)
    public BicicletaResponseDTO buscarPorId(Long id) {
        return bicicletaMapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public BicicletaResponseDTO atualizar(Long id, BicicletaRequestDTO dto) {
        Bicicleta bicicleta = buscarEntidade(id);
        Cliente cliente = buscarCliente(dto.clienteId());
        if (bicicletaRepository.existsByNumeroSerieAndIdNot(dto.numeroSerie(), id)) {
            throw new RecursoDuplicadoException(
                    String.format("O número de série '%s' já está cadastrado", dto.numeroSerie()));
        }
        bicicletaMapper.updateEntity(dto, bicicleta);
        bicicleta.setCliente(cliente);
        return bicicletaMapper.toResponse(bicicletaRepository.save(bicicleta));
    }

    @Transactional
    public void deletar(Long id) {
        bicicletaRepository.delete(buscarEntidade(id));
    }

    private Bicicleta buscarEntidade(Long id) {
        return bicicletaRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Bicicleta com id=%d não encontrada", id)));
    }

    private Cliente buscarCliente(Long clienteId) {
        return clienteRepository.findById(clienteId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Cliente com id=%d não encontrado", clienteId)));
    }
}

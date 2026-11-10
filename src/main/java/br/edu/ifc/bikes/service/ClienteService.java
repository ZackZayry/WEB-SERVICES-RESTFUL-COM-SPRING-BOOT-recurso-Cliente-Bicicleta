package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.ClienteRequestDTO;
import br.edu.ifc.bikes.dto.ClienteResponseDTO;
import br.edu.ifc.bikes.dto.ClienteSenhaRequestDTO;
import br.edu.ifc.bikes.dto.ClienteUpdateRequestDTO;
import br.edu.ifc.bikes.dto.mapper.ClienteMapper;
import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.exception.PasswordInvalidException;
import br.edu.ifc.bikes.exception.RecursoDuplicadoException;
import br.edu.ifc.bikes.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ClienteResponseDTO criar(ClienteRequestDTO dto) {
        if (clienteRepository.existsByEmail(dto.email())) {
            throw new RecursoDuplicadoException(String.format("O email '%s' já está cadastrado", dto.email()));
        }
        if (clienteRepository.existsByCpf(dto.cpf())) {
            throw new RecursoDuplicadoException(String.format("O CPF '%s' já está cadastrado", dto.cpf()));
        }
        Cliente cliente = clienteMapper.toEntity(dto);
        cliente.setSenha(passwordEncoder.encode(dto.senha()));
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listar() {
        return clienteMapper.toResponse(clienteRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id) {
        return clienteMapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public ClienteResponseDTO atualizar(Long id, ClienteUpdateRequestDTO dto) {
        Cliente cliente = buscarEntidade(id);
        if (clienteRepository.existsByEmailAndIdNot(dto.email(), id)) {
            throw new RecursoDuplicadoException(String.format("O email '%s' já está cadastrado", dto.email()));
        }
        if (clienteRepository.existsByCpfAndIdNot(dto.cpf(), id)) {
            throw new RecursoDuplicadoException(String.format("O CPF '%s' já está cadastrado", dto.cpf()));
        }
        clienteMapper.updateEntity(dto, cliente);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public void deletar(Long id) {
        // as bicicletas do cliente são removidas junto (cascade + orphanRemoval)
        clienteRepository.delete(buscarEntidade(id));
    }

    /**
     * Regras de alteração de senha:
     * 1. nova senha e confirmação devem ser iguais;
     * 2. nova senha deve ser diferente da atual;
     * 3. o cliente deve existir;
     * 4. a senha atual informada deve conferir com a armazenada.
     */
    @Transactional
    public void atualizarSenha(Long id, ClienteSenhaRequestDTO dto) {
        if (!dto.novaSenha().equals(dto.confirmaSenha())) {
            throw new PasswordInvalidException("A nova senha e a confirmação não coincidem");
        }
        if (dto.novaSenha().equals(dto.senhaAtual())) {
            throw new PasswordInvalidException("A nova senha deve ser diferente da senha atual");
        }
        Cliente cliente = buscarEntidade(id);
        if (!passwordEncoder.matches(dto.senhaAtual(), cliente.getSenha())) {
            throw new PasswordInvalidException("Senha atual incorreta");
        }
        cliente.setSenha(passwordEncoder.encode(dto.novaSenha()));
        clienteRepository.save(cliente);
    }

    private Cliente buscarEntidade(Long id) {
        return clienteRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Cliente com id=%d não encontrado", id)));
    }
}

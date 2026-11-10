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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    // mapper real (gerado pelo MapStruct): testa o service sem simular a conversão
    @Spy
    private ClienteMapper clienteMapper = Mappers.getMapper(ClienteMapper.class);

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteExistente() {
        Cliente c = new Cliente();
        c.setId(1L);
        c.setNome("Maria Silva");
        c.setEmail("maria@email.com");
        c.setCpf("12345678901");
        c.setSenha("hashAtual");
        return c;
    }

    // ---------------------------------------------------------------- criar

    @Test
    @DisplayName("criar: dados válidos salva o cliente com a senha criptografada")
    void criar_dadosValidos_salvaComSenhaCriptografada() {
        var dto = new ClienteRequestDTO("Maria Silva", "maria@email.com", "12345678901", "senha123");
        when(clienteRepository.existsByEmail("maria@email.com")).thenReturn(false);
        when(clienteRepository.existsByCpf("12345678901")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hashSenha");
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        ClienteResponseDTO resposta = clienteService.criar(dto);

        assertEquals(1L, resposta.id());
        assertEquals("Maria Silva", resposta.nome());
        assertEquals("maria@email.com", resposta.email());
        assertEquals("12345678901", resposta.cpf());

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertEquals("hashSenha", captor.getValue().getSenha());
        assertNotEquals("senha123", captor.getValue().getSenha());
    }

    @Test
    @DisplayName("criar: email duplicado lança RecursoDuplicadoException e não salva")
    void criar_emailDuplicado_lancaExcecaoENaoSalva() {
        var dto = new ClienteRequestDTO("Maria Silva", "maria@email.com", "12345678901", "senha123");
        when(clienteRepository.existsByEmail("maria@email.com")).thenReturn(true);

        RecursoDuplicadoException ex = assertThrows(RecursoDuplicadoException.class,
                () -> clienteService.criar(dto));

        assertTrue(ex.getMessage().contains("maria@email.com"));
        verify(clienteRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("criar: CPF duplicado lança RecursoDuplicadoException e não salva")
    void criar_cpfDuplicado_lancaExcecaoENaoSalva() {
        var dto = new ClienteRequestDTO("Maria Silva", "maria@email.com", "12345678901", "senha123");
        when(clienteRepository.existsByEmail("maria@email.com")).thenReturn(false);
        when(clienteRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> clienteService.criar(dto));

        verify(clienteRepository, never()).save(any());
    }

    // ---------------------------------------------------------------- listar / buscar

    @Test
    @DisplayName("listar: retorna todos os clientes como DTOs")
    void listar_retornaTodos() {
        Cliente outro = new Cliente();
        outro.setId(2L);
        outro.setNome("João Souza");
        outro.setEmail("joao@email.com");
        outro.setCpf("98765432100");
        outro.setSenha("hash");
        when(clienteRepository.findAll()).thenReturn(List.of(clienteExistente(), outro));

        List<ClienteResponseDTO> resposta = clienteService.listar();

        assertEquals(2, resposta.size());
        assertEquals("maria@email.com", resposta.get(0).email());
        assertEquals("joao@email.com", resposta.get(1).email());
    }

    @Test
    @DisplayName("buscarPorId: id existente retorna o cliente")
    void buscarPorId_existente_retornaCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente()));

        ClienteResponseDTO resposta = clienteService.buscarPorId(1L);

        assertEquals(1L, resposta.id());
        assertEquals("Maria Silva", resposta.nome());
    }

    @Test
    @DisplayName("buscarPorId: id inexistente lança EntityNotFoundException")
    void buscarPorId_inexistente_lancaNotFound() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> clienteService.buscarPorId(99L));

        assertTrue(ex.getMessage().contains("99"));
    }

    // ---------------------------------------------------------------- atualizar

    @Test
    @DisplayName("atualizar: dados válidos alteram o cadastro e preservam a senha")
    void atualizar_dadosValidos_atualizaSemMexerNaSenha() {
        var dto = new ClienteUpdateRequestDTO("Maria Souza", "maria.souza@email.com", "11122233344");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente()));
        when(clienteRepository.existsByEmailAndIdNot("maria.souza@email.com", 1L)).thenReturn(false);
        when(clienteRepository.existsByCpfAndIdNot("11122233344", 1L)).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponseDTO resposta = clienteService.atualizar(1L, dto);

        assertEquals("Maria Souza", resposta.nome());
        assertEquals("maria.souza@email.com", resposta.email());
        assertEquals("11122233344", resposta.cpf());

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertEquals("hashAtual", captor.getValue().getSenha());
    }

    @Test
    @DisplayName("atualizar: email que pertence a outro cliente lança RecursoDuplicadoException")
    void atualizar_emailDeOutroCliente_lancaExcecao() {
        var dto = new ClienteUpdateRequestDTO("Maria Silva", "joao@email.com", "12345678901");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente()));
        when(clienteRepository.existsByEmailAndIdNot("joao@email.com", 1L)).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> clienteService.atualizar(1L, dto));

        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizar: id inexistente lança EntityNotFoundException")
    void atualizar_inexistente_lancaNotFound() {
        var dto = new ClienteUpdateRequestDTO("Maria Silva", "maria@email.com", "12345678901");
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clienteService.atualizar(99L, dto));

        verify(clienteRepository, never()).save(any());
    }

    // ---------------------------------------------------------------- deletar

    @Test
    @DisplayName("deletar: id existente remove o cliente")
    void deletar_existente_removeCliente() {
        Cliente cliente = clienteExistente();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        clienteService.deletar(1L);

        verify(clienteRepository).delete(cliente);
    }

    @Test
    @DisplayName("deletar: id inexistente lança EntityNotFoundException e não remove")
    void deletar_inexistente_lancaNotFound() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clienteService.deletar(99L));

        verify(clienteRepository, never()).delete(any());
    }

    // ---------------------------------------------------------------- alteração de senha

    @Test
    @DisplayName("atualizarSenha: senha atual correta grava o hash da nova senha")
    void atualizarSenha_dadosValidos_gravaNovoHash() {
        Cliente cliente = clienteExistente();
        var dto = new ClienteSenhaRequestDTO("atual123", "nova12345", "nova12345");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches("atual123", "hashAtual")).thenReturn(true);
        when(passwordEncoder.encode("nova12345")).thenReturn("hashNova");

        clienteService.atualizarSenha(1L, dto);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertEquals("hashNova", captor.getValue().getSenha());
    }

    @Test
    @DisplayName("atualizarSenha: confirmação diferente da nova senha lança PasswordInvalidException")
    void atualizarSenha_confirmacaoDivergente_lancaExcecao() {
        var dto = new ClienteSenhaRequestDTO("atual123", "nova12345", "outraSenha");

        PasswordInvalidException ex = assertThrows(PasswordInvalidException.class,
                () -> clienteService.atualizarSenha(1L, dto));

        assertEquals("A nova senha e a confirmação não coincidem", ex.getMessage());
        verifyNoInteractions(clienteRepository, passwordEncoder);
    }

    @Test
    @DisplayName("atualizarSenha: nova senha igual à atual lança PasswordInvalidException")
    void atualizarSenha_novaIgualAtual_lancaExcecao() {
        var dto = new ClienteSenhaRequestDTO("mesma1234", "mesma1234", "mesma1234");

        PasswordInvalidException ex = assertThrows(PasswordInvalidException.class,
                () -> clienteService.atualizarSenha(1L, dto));

        assertEquals("A nova senha deve ser diferente da senha atual", ex.getMessage());
        verifyNoInteractions(clienteRepository, passwordEncoder);
    }

    @Test
    @DisplayName("atualizarSenha: senha atual incorreta lança PasswordInvalidException e não salva")
    void atualizarSenha_senhaAtualIncorreta_lancaExcecao() {
        var dto = new ClienteSenhaRequestDTO("errada123", "nova12345", "nova12345");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente()));
        when(passwordEncoder.matches("errada123", "hashAtual")).thenReturn(false);

        PasswordInvalidException ex = assertThrows(PasswordInvalidException.class,
                () -> clienteService.atualizarSenha(1L, dto));

        assertEquals("Senha atual incorreta", ex.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizarSenha: cliente inexistente lança EntityNotFoundException")
    void atualizarSenha_clienteInexistente_lancaNotFound() {
        var dto = new ClienteSenhaRequestDTO("atual123", "nova12345", "nova12345");
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clienteService.atualizarSenha(99L, dto));

        verifyNoInteractions(passwordEncoder);
        verify(clienteRepository, never()).save(any());
    }
}

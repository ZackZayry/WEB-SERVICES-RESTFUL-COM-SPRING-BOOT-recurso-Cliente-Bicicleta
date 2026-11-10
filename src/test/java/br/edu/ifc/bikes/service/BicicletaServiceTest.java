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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BicicletaServiceTest {

    @Mock
    private BicicletaRepository bicicletaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Spy
    private BicicletaMapper bicicletaMapper = Mappers.getMapper(BicicletaMapper.class);

    @InjectMocks
    private BicicletaService bicicletaService;

    private Cliente cliente(Long id) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setNome("Cliente " + id);
        c.setEmail("cliente" + id + "@email.com");
        c.setCpf("1234567890" + id);
        c.setSenha("hash");
        return c;
    }

    private Bicicleta bicicleta(Long id, String numeroSerie, Cliente dono) {
        Bicicleta b = new Bicicleta();
        b.setId(id);
        b.setMarca("Caloi");
        b.setModelo("Explorer");
        b.setNumeroSerie(numeroSerie);
        b.setCliente(dono);
        return b;
    }

    // ---------------------------------------------------------------- criar

    @Test
    @DisplayName("criar: dados válidos salva a bicicleta associada ao cliente")
    void criar_dadosValidos_salvaAssociadaAoCliente() {
        var dto = new BicicletaRequestDTO("Caloi", "Explorer", "SN001", 1L);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente(1L)));
        when(bicicletaRepository.existsByNumeroSerie("SN001")).thenReturn(false);
        when(bicicletaRepository.save(any(Bicicleta.class))).thenAnswer(inv -> {
            Bicicleta b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        BicicletaResponseDTO resposta = bicicletaService.criar(dto);

        assertEquals(10L, resposta.id());
        assertEquals("Caloi", resposta.marca());
        assertEquals("Explorer", resposta.modelo());
        assertEquals("SN001", resposta.numeroSerie());
        assertEquals(1L, resposta.clienteId());
    }

    @Test
    @DisplayName("criar: cliente inexistente lança EntityNotFoundException e não salva")
    void criar_clienteInexistente_lancaNotFound() {
        var dto = new BicicletaRequestDTO("Caloi", "Explorer", "SN001", 99L);
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bicicletaService.criar(dto));

        verify(bicicletaRepository, never()).save(any());
    }

    @Test
    @DisplayName("criar: número de série duplicado lança RecursoDuplicadoException")
    void criar_numeroSerieDuplicado_lancaExcecao() {
        var dto = new BicicletaRequestDTO("Caloi", "Explorer", "SN001", 1L);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente(1L)));
        when(bicicletaRepository.existsByNumeroSerie("SN001")).thenReturn(true);

        RecursoDuplicadoException ex = assertThrows(RecursoDuplicadoException.class,
                () -> bicicletaService.criar(dto));

        assertTrue(ex.getMessage().contains("SN001"));
        verify(bicicletaRepository, never()).save(any());
    }

    // ---------------------------------------------------------------- listar / buscar

    @Test
    @DisplayName("listar: sem filtro retorna todas as bicicletas")
    void listar_semFiltro_retornaTodas() {
        Cliente c1 = cliente(1L);
        Cliente c2 = cliente(2L);
        when(bicicletaRepository.findAll())
                .thenReturn(List.of(bicicleta(10L, "SN001", c1), bicicleta(11L, "SN002", c2)));

        List<BicicletaResponseDTO> resposta = bicicletaService.listar(null);

        assertEquals(2, resposta.size());
        verify(bicicletaRepository, never()).findByClienteId(any());
    }

    @Test
    @DisplayName("listar: com clienteId retorna apenas as bicicletas do cliente")
    void listar_comClienteId_filtraPorCliente() {
        when(bicicletaRepository.findByClienteId(1L))
                .thenReturn(List.of(bicicleta(10L, "SN001", cliente(1L))));

        List<BicicletaResponseDTO> resposta = bicicletaService.listar(1L);

        assertEquals(1, resposta.size());
        assertEquals(1L, resposta.get(0).clienteId());
        verify(bicicletaRepository, never()).findAll();
    }

    @Test
    @DisplayName("buscarPorId: id existente retorna a bicicleta")
    void buscarPorId_existente_retornaBicicleta() {
        when(bicicletaRepository.findById(10L)).thenReturn(Optional.of(bicicleta(10L, "SN001", cliente(1L))));

        BicicletaResponseDTO resposta = bicicletaService.buscarPorId(10L);

        assertEquals(10L, resposta.id());
        assertEquals("SN001", resposta.numeroSerie());
    }

    @Test
    @DisplayName("buscarPorId: id inexistente lança EntityNotFoundException")
    void buscarPorId_inexistente_lancaNotFound() {
        when(bicicletaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bicicletaService.buscarPorId(99L));
    }

    // ---------------------------------------------------------------- atualizar

    @Test
    @DisplayName("atualizar: dados válidos alteram a bicicleta e o cliente dono")
    void atualizar_dadosValidos_atualizaBicicletaECliente() {
        Bicicleta existente = bicicleta(10L, "SN001", cliente(1L));
        var dto = new BicicletaRequestDTO("Monark", "Barra Circular", "SN002", 2L);
        when(bicicletaRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente(2L)));
        when(bicicletaRepository.existsByNumeroSerieAndIdNot("SN002", 10L)).thenReturn(false);
        when(bicicletaRepository.save(any(Bicicleta.class))).thenAnswer(inv -> inv.getArgument(0));

        BicicletaResponseDTO resposta = bicicletaService.atualizar(10L, dto);

        assertEquals(10L, resposta.id());
        assertEquals("Monark", resposta.marca());
        assertEquals("Barra Circular", resposta.modelo());
        assertEquals("SN002", resposta.numeroSerie());
        assertEquals(2L, resposta.clienteId());
    }

    @Test
    @DisplayName("atualizar: número de série de outra bicicleta lança RecursoDuplicadoException")
    void atualizar_numeroSerieDeOutraBicicleta_lancaExcecao() {
        var dto = new BicicletaRequestDTO("Caloi", "Explorer", "SN999", 1L);
        when(bicicletaRepository.findById(10L)).thenReturn(Optional.of(bicicleta(10L, "SN001", cliente(1L))));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente(1L)));
        when(bicicletaRepository.existsByNumeroSerieAndIdNot("SN999", 10L)).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> bicicletaService.atualizar(10L, dto));

        verify(bicicletaRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizar: bicicleta inexistente lança EntityNotFoundException")
    void atualizar_bicicletaInexistente_lancaNotFound() {
        var dto = new BicicletaRequestDTO("Caloi", "Explorer", "SN001", 1L);
        when(bicicletaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bicicletaService.atualizar(99L, dto));

        verifyNoInteractions(clienteRepository);
        verify(bicicletaRepository, never()).save(any());
    }

    // ---------------------------------------------------------------- deletar

    @Test
    @DisplayName("deletar: id existente remove a bicicleta")
    void deletar_existente_removeBicicleta() {
        Bicicleta existente = bicicleta(10L, "SN001", cliente(1L));
        when(bicicletaRepository.findById(10L)).thenReturn(Optional.of(existente));

        bicicletaService.deletar(10L);

        verify(bicicletaRepository).delete(existente);
    }

    @Test
    @DisplayName("deletar: id inexistente lança EntityNotFoundException e não remove")
    void deletar_inexistente_lancaNotFound() {
        when(bicicletaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bicicletaService.deletar(99L));

        verify(bicicletaRepository, never()).delete(any());
    }
}

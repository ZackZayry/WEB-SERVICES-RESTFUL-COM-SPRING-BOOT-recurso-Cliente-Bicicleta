package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.dto.BicicletaRequestDTO;
import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.service.BicicletaService;
import br.edu.ifc.bikes.web.exception.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Bicicletas", description = "Operações de cadastro, consulta, edição e remoção de bicicletas.")
@RestController
@RequestMapping("api/v1/bicicletas")
@RequiredArgsConstructor
public class BicicletaController {

    private final BicicletaService bicicletaService;

    @Operation(summary = "Criar uma nova bicicleta",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Bicicleta criada com sucesso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BicicletaResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "Número de série já cadastrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PostMapping
    public ResponseEntity<BicicletaResponseDTO> criar(@Valid @RequestBody BicicletaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bicicletaService.criar(dto));
    }

    @Operation(summary = "Listar bicicletas", description = "Lista todas ou, com o parâmetro clienteId, apenas as de um cliente")
    @GetMapping
    public ResponseEntity<List<BicicletaResponseDTO>> listar(@RequestParam(required = false) Long clienteId) {
        return ResponseEntity.ok(bicicletaService.listar(clienteId));
    }

    @Operation(summary = "Buscar uma bicicleta pelo id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Bicicleta encontrada",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BicicletaResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Bicicleta não encontrada",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/{id}")
    public ResponseEntity<BicicletaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(bicicletaService.buscarPorId(id));
    }

    @Operation(summary = "Atualizar uma bicicleta",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Bicicleta atualizada",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BicicletaResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Bicicleta ou cliente não encontrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "Número de série já cadastrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PutMapping("/{id}")
    public ResponseEntity<BicicletaResponseDTO> atualizar(@PathVariable Long id,
                                                          @Valid @RequestBody BicicletaRequestDTO dto) {
        return ResponseEntity.ok(bicicletaService.atualizar(id, dto));
    }

    @Operation(summary = "Remover uma bicicleta",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Bicicleta removida"),
                    @ApiResponse(responseCode = "404", description = "Bicicleta não encontrada",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        bicicletaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

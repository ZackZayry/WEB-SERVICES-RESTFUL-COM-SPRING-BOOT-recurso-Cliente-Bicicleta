package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Usado na criação (POST) e na atualização (PUT) de bicicletas
public record BicicletaRequestDTO(
        @NotBlank(message = "A marca é obrigatória")
        @Size(max = 100, message = "A marca deve ter no máximo 100 caracteres")
        String marca,

        @NotBlank(message = "O modelo é obrigatório")
        @Size(max = 100, message = "O modelo deve ter no máximo 100 caracteres")
        String modelo,

        @NotBlank(message = "O número de série é obrigatório")
        @Size(max = 50, message = "O número de série deve ter no máximo 50 caracteres")
        String numeroSerie,

        @NotNull(message = "O cliente é obrigatório")
        Long clienteId
) { }

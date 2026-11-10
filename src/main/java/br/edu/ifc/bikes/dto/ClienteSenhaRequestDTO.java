package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteSenhaRequestDTO(
        @NotBlank(message = "A senha atual é obrigatória")
        String senhaAtual,

        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 6, max = 20, message = "A nova senha deve ter entre 6 e 20 caracteres")
        String novaSenha,

        @NotBlank(message = "A confirmação da senha é obrigatória")
        String confirmaSenha
) { }

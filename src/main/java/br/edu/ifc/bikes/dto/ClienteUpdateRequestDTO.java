package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Atualização de dados cadastrais. A senha tem endpoint próprio (ClienteSenhaRequestDTO).
public record ClienteUpdateRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres")
        String nome,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email deve ser válido")
        @Size(max = 150, message = "O email deve ter no máximo 150 caracteres")
        String email,

        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 dígitos numéricos")
        String cpf
) { }

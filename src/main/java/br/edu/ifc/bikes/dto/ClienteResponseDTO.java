package br.edu.ifc.bikes.dto;

// Nunca expõe a senha
public record ClienteResponseDTO(
        Long id,
        String nome,
        String email,
        String cpf
) { }

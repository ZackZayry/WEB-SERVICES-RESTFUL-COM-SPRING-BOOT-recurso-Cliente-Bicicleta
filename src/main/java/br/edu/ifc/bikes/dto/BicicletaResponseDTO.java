package br.edu.ifc.bikes.dto;

public record BicicletaResponseDTO(
        Long id,
        String marca,
        String modelo,
        String numeroSerie,
        Long clienteId
) { }

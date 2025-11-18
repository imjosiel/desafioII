package br.com.erp.desafioII.dto;

import jakarta.validation.constraints.Min;

public record AtualizaItemDTO(
        @Min(1) int quantidade
) {}
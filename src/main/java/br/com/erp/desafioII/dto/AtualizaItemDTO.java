package br.com.erp.desafioII.dto;

import jakarta.validation.constraints.Min;

/**
 * DTO para atualização da quantidade de item do pedido.
 */
public record AtualizaItemDTO(
        @Min(1) int quantidade
) {}
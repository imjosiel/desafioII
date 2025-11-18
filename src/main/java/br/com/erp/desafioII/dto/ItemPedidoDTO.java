package br.com.erp.desafioII.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO de entrada para adicionar item ao pedido.
 */
public record ItemPedidoDTO(
        @NotNull UUID produtoServicoId,
        @Min(1) int quantidade
) {}
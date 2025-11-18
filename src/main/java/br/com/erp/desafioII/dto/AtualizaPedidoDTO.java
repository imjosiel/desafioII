package br.com.erp.desafioII.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO para atualização de pedido.
 * Permite definir o descontoPercentual (>= 0).
 */
public record AtualizaPedidoDTO(
        @NotNull @DecimalMin("0.0") BigDecimal descontoPercentual
) {}
package br.com.erp.desafioII.dto;

import br.com.erp.desafioII.domain.SituacaoPedido;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO de saída com visão agregada do Pedido.
 */
public record PedidoView(
        UUID id,
        SituacaoPedido situacao,
        BigDecimal descontoPercentual,
        BigDecimal totalProdutos,
        BigDecimal totalServicos,
        BigDecimal totalComDesconto,
        List<ItemView> itens
) {
    /**
     * Visão de item contida em PedidoView.
     */
    public record ItemView(UUID id, UUID produtoServicoId, String nome, BigDecimal precoUnitario, int quantidade) {}
}
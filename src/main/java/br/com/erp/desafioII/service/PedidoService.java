package br.com.erp.desafioII.service;

import br.com.erp.desafioII.domain.*;
import br.com.erp.desafioII.repository.ItemPedidoRepository;
import br.com.erp.desafioII.repository.PedidoRepository;
import br.com.erp.desafioII.repository.ProdutoServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Regras de negócio de Pedido:
 * - Itens: adicionar, remover, atualizar quantidade
 * - Desconto: aplicado apenas a produtos e somente com pedido ABERTO
 * - Totais: produtos, serviços e total com desconto
 * - Fechamento: bloqueia alterações
 */
@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final ProdutoServicoRepository produtoServicoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoServicoRepository produtoServicoRepository, ItemPedidoRepository itemPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoServicoRepository = produtoServicoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    /**
     * Cria um novo pedido em situação ABERTO com desconto 0.
     */
    @Transactional
    public Pedido criar() {
        return pedidoRepository.save(new Pedido());
    }

    /**
     * Adiciona um item ao pedido.
     * Bloqueia se o pedido estiver FECHADO ou se o produto estiver desativado.
     * @param pedidoId UUID do pedido
     * @param produtoServicoId UUID de produto/serviço
     * @param quantidade quantidade (>=1)
     * @return pedido atualizado
     */
    @Transactional
    public Pedido adicionarItem(UUID pedidoId, UUID produtoServicoId, int quantidade) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        if (pedido.getSituacao() == SituacaoPedido.FECHADO) {
            throw new IllegalStateException("Pedido fechado");
        }
        ProdutoServico ps = produtoServicoRepository.findById(produtoServicoId).orElseThrow();
        if (Boolean.FALSE.equals(ps.getAtivo())) {
            throw new IllegalStateException("Produto desativado");
        }
        ItemPedido ip = new ItemPedido();
        ip.setPedido(pedido);
        ip.setProdutoServico(ps);
        ip.setQuantidade(quantidade);
        ip.setPrecoUnitario(ps.getPreco());
        pedido.getItens().add(ip);
        return pedidoRepository.save(pedido);
    }

    /**
     * Remove um item do pedido por ID.
     * @param pedidoId UUID do pedido
     * @param itemId UUID do item
     * @return pedido atualizado
     */
    @Transactional
    public Pedido removerItem(UUID pedidoId, UUID itemId) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        pedido.getItens().removeIf(i -> i.getId().equals(itemId));
        return pedidoRepository.save(pedido);
    }

    /**
     * Atualiza a quantidade de um item.
     * Bloqueia se o pedido estiver FECHADO.
     * @param pedidoId UUID do pedido
     * @param itemId UUID do item
     * @param quantidade nova quantidade (>=1)
     * @return pedido atualizado
     */
    @Transactional
    public Pedido atualizarQuantidadeItem(UUID pedidoId, UUID itemId, int quantidade) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        if (pedido.getSituacao() == SituacaoPedido.FECHADO) {
            throw new IllegalStateException("Pedido fechado");
        }
        for (ItemPedido i : pedido.getItens()) {
            if (i.getId().equals(itemId)) {
                i.setQuantidade(quantidade);
                break;
            }
        }
        return pedidoRepository.save(pedido);
    }

    /**
     * Define o percentual de desconto do pedido.
     * Apenas permitido se o pedido estiver ABERTO e percentual >= 0.
     * O desconto incide somente sobre produtos no cálculo final.
     */
    @Transactional
    public Pedido aplicarDesconto(UUID pedidoId, BigDecimal percentual) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        if (pedido.getSituacao() == SituacaoPedido.FECHADO) {
            throw new IllegalStateException("Pedido fechado");
        }
        if (percentual.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Percentual inválido");
        }
        pedido.setDescontoPercentual(percentual);
        return pedidoRepository.save(pedido);
    }

    /**
     * Soma o total de itens do tipo PRODUTO.
     */
    public BigDecimal totalProdutos(Pedido pedido) {
        return pedido.getItens().stream()
                .filter(i -> i.getProdutoServico().getTipo() == TipoItem.PRODUTO)
                .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Soma o total de itens do tipo SERVICO.
     */
    public BigDecimal totalServicos(Pedido pedido) {
        return pedido.getItens().stream()
                .filter(i -> i.getProdutoServico().getTipo() == TipoItem.SERVICO)
                .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Total combinado: serviços (sem desconto) + produtos com desconto aplicado.
     */
    public BigDecimal totalComDesconto(Pedido pedido) {
        BigDecimal produtos = totalProdutos(pedido);
        BigDecimal servicos = totalServicos(pedido);
        BigDecimal desconto = pedido.getDescontoPercentual().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal produtosComDesconto = produtos.subtract(produtos.multiply(desconto));
        return produtosComDesconto.add(servicos);
    }

    /**
     * Fecha o pedido (situação FECHADO), bloqueando alterações futuras.
     */
    @Transactional
    public Pedido fechar(UUID pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        pedido.setSituacao(SituacaoPedido.FECHADO);
        return pedidoRepository.save(pedido);
    }

    /**
     * Exclui o pedido por ID.
     * Itens são removidos devido ao cascade/orphanRemoval.
     */
    @Transactional
    public void deletar(UUID pedidoId) {
        pedidoRepository.deleteById(pedidoId);
    }
}
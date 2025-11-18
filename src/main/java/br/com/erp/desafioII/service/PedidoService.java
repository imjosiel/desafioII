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

    @Transactional
    public Pedido criar() {
        return pedidoRepository.save(new Pedido());
    }

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

    @Transactional
    public Pedido removerItem(UUID pedidoId, UUID itemId) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        pedido.getItens().removeIf(i -> i.getId().equals(itemId));
        return pedidoRepository.save(pedido);
    }

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

    public BigDecimal totalProdutos(Pedido pedido) {
        return pedido.getItens().stream()
                .filter(i -> i.getProdutoServico().getTipo() == TipoItem.PRODUTO)
                .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalServicos(Pedido pedido) {
        return pedido.getItens().stream()
                .filter(i -> i.getProdutoServico().getTipo() == TipoItem.SERVICO)
                .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalComDesconto(Pedido pedido) {
        BigDecimal produtos = totalProdutos(pedido);
        BigDecimal servicos = totalServicos(pedido);
        BigDecimal desconto = pedido.getDescontoPercentual().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal produtosComDesconto = produtos.subtract(produtos.multiply(desconto));
        return produtosComDesconto.add(servicos);
    }

    @Transactional
    public Pedido fechar(UUID pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        pedido.setSituacao(SituacaoPedido.FECHADO);
        return pedidoRepository.save(pedido);
    }
}
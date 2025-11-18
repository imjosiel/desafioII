package br.com.erp.desafioII.controller;

import br.com.erp.desafioII.domain.ItemPedido;
import br.com.erp.desafioII.domain.Pedido;
import br.com.erp.desafioII.dto.ItemPedidoDTO;
import br.com.erp.desafioII.dto.AtualizaItemDTO;
import br.com.erp.desafioII.dto.PedidoView;
import br.com.erp.desafioII.dto.AtualizaPedidoDTO;
import br.com.erp.desafioII.repository.ItemPedidoRepository;
import br.com.erp.desafioII.repository.PedidoRepository;
import br.com.erp.desafioII.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints de pedido: criação, listagem, detalhe com totais/itens,
 * gerenciamento de itens, aplicação de desconto e fechamento.
 * Regras:
 * - Desconto apenas em produtos e somente com pedido ABERTO
 * - Fechamento bloqueia alterações e aplicação de desconto
 */
@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final PedidoService pedidoService;

    public PedidoController(PedidoRepository pedidoRepository, ItemPedidoRepository itemPedidoRepository, PedidoService pedidoService) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.pedidoService = pedidoService;
    }

    /**
     * Cria um novo pedido na situação ABERTO com desconto 0.
     * @return pedido criado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido create() {
        return pedidoService.criar();
    }

    /**
     * Lista pedidos com paginação.
     * @param pageable parâmetros de paginação
     * @return página de pedidos
     */
    @GetMapping
    public Page<Pedido> list(Pageable pageable) {
        return pedidoRepository.findAll(pageable);
    }

    /**
     * Detalha o pedido com visão agregada (totais, desconto e itens).
     * @param id UUID do pedido
     * @return visão de pedido com totais e itens
     */
    @GetMapping("/{id}")
    public PedidoView get(@PathVariable UUID id) {
        Pedido p = pedidoRepository.findById(id).orElseThrow();
        List<PedidoView.ItemView> itens = p.getItens().stream().map(i -> new PedidoView.ItemView(
                i.getId(), i.getProdutoServico().getId(), i.getProdutoServico().getNome(), i.getPrecoUnitario(), i.getQuantidade()
        )).toList();
        return new PedidoView(
                p.getId(),
                p.getSituacao(),
                p.getDescontoPercentual(),
                pedidoService.totalProdutos(p),
                pedidoService.totalServicos(p),
                pedidoService.totalComDesconto(p),
                itens
        );
    }

    /**
     * Adiciona um item ao pedido.
     * Bloqueia se o pedido estiver FECHADO ou se o produto estiver desativado.
     * @param id UUID do pedido
     * @param dto dados do item (produtoServicoId, quantidade)
     * @return pedido atualizado
     */
    @PostMapping("/{id}/itens")
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido addItem(@PathVariable UUID id, @RequestBody @Valid ItemPedidoDTO dto) {
        return pedidoService.adicionarItem(id, dto.produtoServicoId(), dto.quantidade());
    }

    /**
     * Remove um item do pedido.
     * @param id UUID do pedido
     * @param itemId UUID do item
     */
    @DeleteMapping("/{id}/itens/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable UUID id, @PathVariable UUID itemId) {
        pedidoService.removerItem(id, itemId);
    }

    /**
     * Atualiza a quantidade de um item do pedido.
     * Bloqueia se o pedido estiver FECHADO.
     * @param id UUID do pedido
     * @param itemId UUID do item
     * @param dto nova quantidade
     * @return pedido atualizado
     */
    @PutMapping("/{id}/itens/{itemId}")
    public Pedido atualizarItem(@PathVariable UUID id, @PathVariable UUID itemId, @RequestBody @Valid AtualizaItemDTO dto) {
        return pedidoService.atualizarQuantidadeItem(id, itemId, dto.quantidade());
    }

    /**
     * Aplica percentual de desconto apenas sobre produtos.
     * Permitido somente se o pedido estiver ABERTO.
     * @param id UUID do pedido
     * @param percentual valor percentual (0+)
     * @return pedido atualizado
     */
    @PostMapping("/{id}/desconto")
    public Pedido aplicarDesconto(@PathVariable UUID id, @RequestParam BigDecimal percentual) {
        return pedidoService.aplicarDesconto(id, percentual);
    }

    /**
     * Fecha o pedido, alterando a situação para FECHADO e bloqueando alterações.
     * @param id UUID do pedido
     * @return pedido atualizado
     */
    @PostMapping("/{id}/fechar")
    public Pedido fechar(@PathVariable UUID id) {
        return pedidoService.fechar(id);
    }

    /**
     * Atualiza dados do pedido (descontoPercentual), mantendo validações de regra.
     * Equivalente funcional a aplicar desconto via endpoint dedicado.
     * @param id UUID do pedido
     * @param dto dados de atualização
     * @return pedido atualizado
     */
    @PutMapping("/{id}")
    public Pedido update(@PathVariable UUID id, @RequestBody @Valid AtualizaPedidoDTO dto) {
        return pedidoService.aplicarDesconto(id, dto.descontoPercentual());
    }

    /**
     * Exclui um pedido por ID.
     * Cascade configurado remove itens associados.
     * @param id UUID do pedido
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        pedidoService.deletar(id);
    }
}
package br.com.erp.desafioII.controller;

import br.com.erp.desafioII.domain.ItemPedido;
import br.com.erp.desafioII.domain.Pedido;
import br.com.erp.desafioII.dto.ItemPedidoDTO;
import br.com.erp.desafioII.dto.AtualizaItemDTO;
import br.com.erp.desafioII.dto.PedidoView;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido create() {
        return pedidoService.criar();
    }

    @GetMapping
    public Page<Pedido> list(Pageable pageable) {
        return pedidoRepository.findAll(pageable);
    }

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

    @PostMapping("/{id}/itens")
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido addItem(@PathVariable UUID id, @RequestBody @Valid ItemPedidoDTO dto) {
        return pedidoService.adicionarItem(id, dto.produtoServicoId(), dto.quantidade());
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable UUID id, @PathVariable UUID itemId) {
        pedidoService.removerItem(id, itemId);
    }

    @PutMapping("/{id}/itens/{itemId}")
    public Pedido atualizarItem(@PathVariable UUID id, @PathVariable UUID itemId, @RequestBody @Valid AtualizaItemDTO dto) {
        return pedidoService.atualizarQuantidadeItem(id, itemId, dto.quantidade());
    }

    @PostMapping("/{id}/desconto")
    public Pedido aplicarDesconto(@PathVariable UUID id, @RequestParam BigDecimal percentual) {
        return pedidoService.aplicarDesconto(id, percentual);
    }

    @PostMapping("/{id}/fechar")
    public Pedido fechar(@PathVariable UUID id) {
        return pedidoService.fechar(id);
    }
}
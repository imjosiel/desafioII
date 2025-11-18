package br.com.erp.desafioII.controller;

import br.com.erp.desafioII.domain.ItemPedido;
import br.com.erp.desafioII.repository.ItemPedidoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoints de itens de pedido: listagem paginada e detalhamento por ID.
 * Útil para auditoria e inspeção independente dos itens.
 */
@RestController
@RequestMapping("/itens-pedido")
public class ItemPedidoController {
    private final ItemPedidoRepository repository;

    public ItemPedidoController(ItemPedidoRepository repository) {
        this.repository = repository;
    }

    /**
     * Lista itens de pedido com paginação.
     * @param pageable parâmetros de paginação
     * @return página de itens
     */
    @GetMapping
    public Page<ItemPedido> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /**
     * Detalha um item de pedido por ID.
     * @param id UUID do item
     * @return item encontrado
     */
    @GetMapping("/{id}")
    public ItemPedido get(@PathVariable UUID id) {
        return repository.findById(id).orElseThrow();
    }
}
package br.com.erp.desafioII.controller;

import br.com.erp.desafioII.domain.ItemPedido;
import br.com.erp.desafioII.repository.ItemPedidoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/itens-pedido")
public class ItemPedidoController {
    private final ItemPedidoRepository repository;

    public ItemPedidoController(ItemPedidoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Page<ItemPedido> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ItemPedido get(@PathVariable UUID id) {
        return repository.findById(id).orElseThrow();
    }
}
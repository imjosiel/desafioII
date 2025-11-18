package br.com.erp.desafioII.service;

import br.com.erp.desafioII.repository.ItemPedidoRepository;
import br.com.erp.desafioII.repository.ProdutoServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProdutoServicoService {
    private final ProdutoServicoRepository produtoServicoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public ProdutoServicoService(ProdutoServicoRepository produtoServicoRepository, ItemPedidoRepository itemPedidoRepository) {
        this.produtoServicoRepository = produtoServicoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    @Transactional
    public void delete(UUID id) {
        if (itemPedidoRepository.existsByProdutoServico_Id(id)) {
            throw new IllegalStateException("Produto/serviço associado a pedido");
        }
        produtoServicoRepository.deleteById(id);
    }
}
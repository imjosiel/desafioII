package br.com.erp.desafioII.repository;

import br.com.erp.desafioII.domain.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositório JPA para ItemPedido.
 */
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, UUID> {
    boolean existsByProdutoServico_Id(UUID produtoServicoId);
}
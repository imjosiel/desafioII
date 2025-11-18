package br.com.erp.desafioII.repository;

import br.com.erp.desafioII.domain.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositório JPA para Pedido.
 */
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {
}
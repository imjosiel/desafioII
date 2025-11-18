package br.com.erp.desafioII.repository;

import br.com.erp.desafioII.domain.ProdutoServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProdutoServicoRepository extends JpaRepository<ProdutoServico, UUID> {
}
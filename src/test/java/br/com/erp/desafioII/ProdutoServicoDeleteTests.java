package br.com.erp.desafioII;

import br.com.erp.desafioII.domain.Pedido;
import br.com.erp.desafioII.domain.ProdutoServico;
import br.com.erp.desafioII.domain.TipoItem;
import br.com.erp.desafioII.repository.ProdutoServicoRepository;
import br.com.erp.desafioII.service.PedidoService;
import br.com.erp.desafioII.service.ProdutoServicoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class ProdutoServicoDeleteTests extends TestContainersConfig {
    @Autowired
    ProdutoServicoRepository produtoServicoRepository;
    @Autowired
    PedidoService pedidoService;
    @Autowired
    ProdutoServicoService produtoServicoService;

    @Test
    void naoExcluiAssociadoAPedido() {
        ProdutoServico prod = new ProdutoServico();
        prod.setNome("Teclado");
        prod.setPreco(new BigDecimal("150"));
        prod.setTipo(TipoItem.PRODUTO);
        prod.setAtivo(true);
        produtoServicoRepository.save(prod);

        Pedido p = pedidoService.criar();
        pedidoService.adicionarItem(p.getId(), prod.getId(), 1);

        Assertions.assertThrows(IllegalStateException.class, () -> produtoServicoService.delete(prod.getId()));
    }
}
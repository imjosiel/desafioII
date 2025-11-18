package br.com.erp.desafioII;

import br.com.erp.desafioII.domain.Pedido;
import br.com.erp.desafioII.domain.ProdutoServico;
import br.com.erp.desafioII.domain.SituacaoPedido;
import br.com.erp.desafioII.domain.TipoItem;
import br.com.erp.desafioII.repository.PedidoRepository;
import br.com.erp.desafioII.repository.ProdutoServicoRepository;
import br.com.erp.desafioII.service.PedidoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class PedidoServiceTests extends TestContainersConfig {
    @Autowired
    PedidoService pedidoService;
    @Autowired
    ProdutoServicoRepository produtoServicoRepository;
    @Autowired
    PedidoRepository pedidoRepository;

    @Test
    void aplicaDescontoSomenteEmProdutos() {
        ProdutoServico prod = new ProdutoServico();
        prod.setNome("Notebook");
        prod.setPreco(new BigDecimal("3000"));
        prod.setTipo(TipoItem.PRODUTO);
        prod.setAtivo(true);
        produtoServicoRepository.save(prod);

        ProdutoServico serv = new ProdutoServico();
        serv.setNome("Suporte");
        serv.setPreco(new BigDecimal("200"));
        serv.setTipo(TipoItem.SERVICO);
        serv.setAtivo(true);
        produtoServicoRepository.save(serv);

        Pedido p = pedidoService.criar();
        p = pedidoService.adicionarItem(p.getId(), prod.getId(), 1);
        p = pedidoService.adicionarItem(p.getId(), serv.getId(), 2);
        p = pedidoService.aplicarDesconto(p.getId(), new BigDecimal("10"));

        BigDecimal total = pedidoService.totalComDesconto(p);
        Assertions.assertEquals(new BigDecimal("360"), pedidoService.totalServicos(p));
        Assertions.assertEquals(new BigDecimal("2700.00"), pedidoService.totalProdutos(p).subtract(pedidoService.totalProdutos(p).multiply(new BigDecimal("0.10"))).setScale(2));
        Assertions.assertEquals(new BigDecimal("3060.00"), total.setScale(2));
    }

    @Test
    void bloqueiaDescontoQuandoFechado() {
        Pedido p = pedidoService.criar();
        pedidoService.fechar(p.getId());
        Assertions.assertThrows(IllegalStateException.class, () -> pedidoService.aplicarDesconto(p.getId(), new BigDecimal("5")));
    }

    @Test
    void bloqueiaProdutoDesativado() {
        ProdutoServico prod = new ProdutoServico();
        prod.setNome("Mouse");
        prod.setPreco(new BigDecimal("100"));
        prod.setTipo(TipoItem.PRODUTO);
        prod.setAtivo(false);
        produtoServicoRepository.save(prod);

        Pedido p = pedidoService.criar();
        Assertions.assertThrows(IllegalStateException.class, () -> pedidoService.adicionarItem(p.getId(), prod.getId(), 1));
    }
}
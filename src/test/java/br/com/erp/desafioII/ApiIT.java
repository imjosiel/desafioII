package br.com.erp.desafioII;

import br.com.erp.desafioII.domain.TipoItem;
import br.com.erp.desafioII.dto.ItemPedidoDTO;
import br.com.erp.desafioII.dto.ProdutoServicoDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIT extends TestContainersConfig {
    @Autowired
    TestRestTemplate rest;

    @Test
    void fluxoCompletoPedidoComDesconto() {
        ProdutoServicoDTO prodDto = new ProdutoServicoDTO("Notebook", new BigDecimal("3000"), TipoItem.PRODUTO, true);
        ResponseEntity<Map> prodRes = rest.postForEntity("/produtos-servicos", prodDto, Map.class);
        Assertions.assertEquals(HttpStatus.CREATED, prodRes.getStatusCode());
        UUID prodId = UUID.fromString(prodRes.getBody().get("id").toString());

        ProdutoServicoDTO servDto = new ProdutoServicoDTO("Suporte", new BigDecimal("200"), TipoItem.SERVICO, true);
        ResponseEntity<Map> servRes = rest.postForEntity("/produtos-servicos", servDto, Map.class);
        Assertions.assertEquals(HttpStatus.CREATED, servRes.getStatusCode());
        UUID servId = UUID.fromString(servRes.getBody().get("id").toString());

        ResponseEntity<Map> pedidoRes = rest.postForEntity("/pedidos", null, Map.class);
        Assertions.assertEquals(HttpStatus.CREATED, pedidoRes.getStatusCode());
        UUID pedidoId = UUID.fromString(pedidoRes.getBody().get("id").toString());

        ResponseEntity<Map> addProd = rest.postForEntity("/pedidos/" + pedidoId + "/itens", new ItemPedidoDTO(prodId, 1), Map.class);
        Assertions.assertEquals(HttpStatus.CREATED, addProd.getStatusCode());
        ResponseEntity<Map> addServ = rest.postForEntity("/pedidos/" + pedidoId + "/itens", new ItemPedidoDTO(servId, 2), Map.class);
        Assertions.assertEquals(HttpStatus.CREATED, addServ.getStatusCode());

        ResponseEntity<Map> descRes = rest.postForEntity("/pedidos/" + pedidoId + "/desconto?percentual=10", null, Map.class);
        Assertions.assertEquals(HttpStatus.OK, descRes.getStatusCode());

        ResponseEntity<Map> getRes = rest.getForEntity("/pedidos/" + pedidoId, Map.class);
        Assertions.assertEquals(HttpStatus.OK, getRes.getStatusCode());
        Assertions.assertEquals("10", getRes.getBody().get("descontoPercentual").toString());
        Assertions.assertEquals("3060.00", new BigDecimal(getRes.getBody().get("totalComDesconto").toString()).setScale(2).toString());
    }

    @Test
    void bloqueiosDeRegraDeNegocio() {
        ProdutoServicoDTO prodDto = new ProdutoServicoDTO("Mouse", new BigDecimal("100"), TipoItem.PRODUTO, false);
        ResponseEntity<Map> prodRes = rest.postForEntity("/produtos-servicos", prodDto, Map.class);
        UUID prodId = UUID.fromString(prodRes.getBody().get("id").toString());

        ResponseEntity<Map> pedidoRes = rest.postForEntity("/pedidos", null, Map.class);
        UUID pedidoId = UUID.fromString(pedidoRes.getBody().get("id").toString());

        ResponseEntity<Map> addDesativado = rest.postForEntity("/pedidos/" + pedidoId + "/itens", new ItemPedidoDTO(prodId, 1), Map.class);
        Assertions.assertEquals(HttpStatus.CONFLICT, addDesativado.getStatusCode());

        ResponseEntity<Map> fechar = rest.postForEntity("/pedidos/" + pedidoId + "/fechar", null, Map.class);
        Assertions.assertEquals(HttpStatus.OK, fechar.getStatusCode());

        ResponseEntity<Map> aplicarDescFechado = rest.postForEntity("/pedidos/" + pedidoId + "/desconto?percentual=10", null, Map.class);
        Assertions.assertEquals(HttpStatus.CONFLICT, aplicarDescFechado.getStatusCode());
    }
}
package br.com.erp.desafioII;

import br.com.erp.desafioII.domain.TipoItem;
import br.com.erp.desafioII.dto.ItemPedidoDTO;
import br.com.erp.desafioII.dto.ProdutoServicoDTO;
import br.com.erp.desafioII.dto.AtualizaPedidoDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Testes de integração dos principais fluxos da API.
 * Exercita criação/atualização de catálogo, ciclo de pedido, regras de desconto
 * e bloqueios de negócio utilizando um ambiente de aplicação real.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIT extends TestContainersConfig {
    @Autowired
    TestRestTemplate rest;

    /**
     * Fluxo completo de pedido com desconto:
     * 1) Cria produto e serviço
     * 2) Cria pedido e adiciona itens (1x produto, 2x serviço)
     * 3) Aplica 10% de desconto (somente sobre produtos)
     * 4) Valida totais agregados e desconto informado
     */
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

    /**
     * Valida bloqueios de regra de negócio:
     * - Produto desativado não pode ser adicionado ao pedido
     * - Pedido fechado bloqueia aplicação de desconto
     */
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

    /**
     * Atualização de pedido via PUT:
     * - Após adicionar itens, envia AtualizaPedidoDTO com descontoPercentual
     * - Valida que o endpoint PUT reflete corretamente o desconto aplicado
     */
    @Test
    void atualizarPedidoViaPut() {
        ProdutoServicoDTO prodDto = new ProdutoServicoDTO("Notebook", new BigDecimal("3000"), TipoItem.PRODUTO, true);
        ResponseEntity<Map> prodRes = rest.postForEntity("/produtos-servicos", prodDto, Map.class);
        UUID prodId = UUID.fromString(prodRes.getBody().get("id").toString());

        ProdutoServicoDTO servDto = new ProdutoServicoDTO("Suporte", new BigDecimal("200"), TipoItem.SERVICO, true);
        ResponseEntity<Map> servRes = rest.postForEntity("/produtos-servicos", servDto, Map.class);
        UUID servId = UUID.fromString(servRes.getBody().get("id").toString());

        ResponseEntity<Map> pedidoRes = rest.postForEntity("/pedidos", null, Map.class);
        UUID pedidoId = UUID.fromString(pedidoRes.getBody().get("id").toString());

        rest.postForEntity("/pedidos/" + pedidoId + "/itens", new ItemPedidoDTO(prodId, 1), Map.class);
        rest.postForEntity("/pedidos/" + pedidoId + "/itens", new ItemPedidoDTO(servId, 2), Map.class);

        AtualizaPedidoDTO dto = new AtualizaPedidoDTO(new BigDecimal("10"));
        ResponseEntity<Map> putRes = rest.exchange("/pedidos/" + pedidoId, HttpMethod.PUT, new HttpEntity<>(dto), Map.class);
        Assertions.assertEquals(HttpStatus.OK, putRes.getStatusCode());

        ResponseEntity<Map> getRes = rest.getForEntity("/pedidos/" + pedidoId, Map.class);
        Assertions.assertEquals("10", getRes.getBody().get("descontoPercentual").toString());
        Assertions.assertEquals("3060.00", new BigDecimal(getRes.getBody().get("totalComDesconto").toString()).setScale(2).toString());
    }

    /**
     * Exclusão de pedido e verificação da remoção:
     * - Cria pedido, exclui via DELETE
     * - Garante que a busca pelo recurso retorna 404
     */
    @Test
    void excluirPedidoViaDelete() {
        ResponseEntity<Map> pedidoRes = rest.postForEntity("/pedidos", null, Map.class);
        UUID pedidoId = UUID.fromString(pedidoRes.getBody().get("id").toString());

        ResponseEntity<Void> delRes = rest.exchange("/pedidos/" + pedidoId, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, delRes.getStatusCode());

        ResponseEntity<Map> getRes = rest.getForEntity("/pedidos/" + pedidoId, Map.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, getRes.getStatusCode());
    }
}
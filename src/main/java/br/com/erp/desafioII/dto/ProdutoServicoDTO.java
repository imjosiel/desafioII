package br.com.erp.desafioII.dto;

import br.com.erp.desafioII.domain.TipoItem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProdutoServicoDTO(
        @NotBlank String nome,
        @NotNull @DecimalMin("0.0") BigDecimal preco,
        @NotNull TipoItem tipo,
        @NotNull Boolean ativo
) {}
package br.com.erp.desafioII.controller;

import br.com.erp.desafioII.domain.ProdutoServico;
import br.com.erp.desafioII.dto.ProdutoServicoDTO;
import br.com.erp.desafioII.repository.ProdutoServicoRepository;
import br.com.erp.desafioII.service.ProdutoServicoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/produtos-servicos")
public class ProdutoServicoController {
    private final ProdutoServicoRepository repository;
    private final ProdutoServicoService service;

    public ProdutoServicoController(ProdutoServicoRepository repository, ProdutoServicoService service) {
        this.repository = repository;
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoServico create(@RequestBody @Valid ProdutoServicoDTO dto) {
        ProdutoServico ps = new ProdutoServico();
        ps.setNome(dto.nome());
        ps.setPreco(dto.preco());
        ps.setTipo(dto.tipo());
        ps.setAtivo(dto.ativo());
        return repository.save(ps);
    }

    @GetMapping
    public Page<ProdutoServico> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ProdutoServico get(@PathVariable UUID id) {
        return repository.findById(id).orElseThrow();
    }

    @PutMapping("/{id}")
    public ProdutoServico update(@PathVariable UUID id, @RequestBody @Valid ProdutoServicoDTO dto) {
        ProdutoServico ps = repository.findById(id).orElseThrow();
        ps.setNome(dto.nome());
        ps.setPreco(dto.preco());
        ps.setTipo(dto.tipo());
        ps.setAtivo(dto.ativo());
        return repository.save(ps);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
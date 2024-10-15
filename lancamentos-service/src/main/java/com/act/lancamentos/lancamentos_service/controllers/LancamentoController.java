package com.act.lancamentos.lancamentos_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.act.lancamentos.lancamentos_service.model.Lancamento;
import com.act.lancamentos.lancamentos_service.service.LancamentoService;

@RestController
@RequestMapping("/lancamentos")
public class LancamentoController {

	@Autowired
	private LancamentoService lancamentoService;

	@PostMapping
	public Lancamento criarLancamento(@RequestBody Lancamento lancamento) {
		return lancamentoService.salvarLancamento(lancamento);
	}

	@GetMapping
	public List<Lancamento> buscarTodosLancamentos() {
		return lancamentoService.buscarTodosLancamentos();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Lancamento> buscarLancamentoPorId(@PathVariable Long id) {
		Optional<Lancamento> lancamento = lancamentoService.buscarLancamentoPorId(id);
		return lancamento.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PutMapping("/{id}")
	public ResponseEntity<Lancamento> atualizarLancamento(@PathVariable Long id, @RequestBody Lancamento lancamento) {
		Optional<Lancamento> lancamentoExistente = lancamentoService.buscarLancamentoPorId(id);
		if (lancamentoExistente.isPresent()) {
			lancamento.setId(id); // Atualiza o ID com o do objeto existente
			Lancamento lancamentoAtualizado = lancamentoService.atualizarLancamento(lancamento);
			return ResponseEntity.ok(lancamentoAtualizado);
		}
		return ResponseEntity.notFound().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletarLancamento(@PathVariable Long id) {
		lancamentoService.deletarLancamento(id);
		return ResponseEntity.noContent().build();
	}
}

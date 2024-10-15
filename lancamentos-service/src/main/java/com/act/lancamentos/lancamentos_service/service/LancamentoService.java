package com.act.lancamentos.lancamentos_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LancamentoService {

	@Autowired
	private LancamentoRepository lancamentoRepository;

	public Lancamento salvarLancamento(Lancamento lancamento) {
		return lancamentoRepository.save(lancamento);
	}

	public List<Lancamento> buscarTodosLancamentos() {
		return lancamentoRepository.findAll();
	}

	public Optional<Lancamento> buscarLancamentoPorId(Long id) {
		return lancamentoRepository.findById(id);
	}

	public Lancamento atualizarLancamento(Lancamento lancamento) {
		return lancamentoRepository.save(lancamento);
	}

	public void deletarLancamento(Long id) {
		lancamentoRepository.deleteById(id);
	}
}

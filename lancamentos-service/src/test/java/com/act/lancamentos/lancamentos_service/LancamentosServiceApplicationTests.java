package com.act.lancamentos.lancamentos_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.act.lancamentos.lancamentos_service.repository.LancamentoRepository;
import com.act.lancamentos.lancamentos_service.service.LancamentoService;



@SpringBootTest
class LancamentoServiceTest {

 @MockBean
 private LancamentoRepository lancamentoRepository;

 @Autowired
 private LancamentoService lancamentoService;

 @Test
 void testCriarLancamento() {
 Lancamento lancamento = new Lancamento();
 lancamento.setDescricao("Teste");
 when(lancamentoRepository.save(any(Lancamento.class))).thenReturn(lancamento);

 Lancamento resultado = lancamentoService.criarLancamento(lancamento);
 assertNotNull(resultado);
 }
}


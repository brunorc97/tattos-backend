package com.tattos.service;

import com.tattos.dto.request.AgendamentoRequestDTO;
import com.tattos.dto.response.AgendamentoResponseDTO;
import com.tattos.exception.BusinessException;
import com.tattos.exception.ConflictException;
import com.tattos.model.Cliente;
import com.tattos.model.Tatuador;
import com.tattos.model.enums.StatusAgendamento;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.TransacaoFinanceiraRepository;
import com.tattos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AgendamentoServiceTest {

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TransacaoFinanceiraRepository transacaoRepository;

    private Cliente cliente;
    private Tatuador tatuador;

    @BeforeEach
    void setUp() {
        agendamentoRepository.deleteAll();
        transacaoRepository.deleteAll();
        usuarioRepository.deleteAll();

        cliente = Cliente.builder()
                .nome("Carlos Eduardo")
                .email("carlos@teste.com")
                .senha("hash123")
                .telefone("(11) 98888-7777")
                .ativo(true)
                .build();
        cliente = (Cliente) usuarioRepository.save(cliente);

        tatuador = Tatuador.builder()
                .nome("Rodrigo Black")
                .email("rodrigo@teste.com")
                .senha("hash123")
                .especialidade("Blackwork")
                .comissaoPercentual(new BigDecimal("70.00"))
                .ativo(true)
                .build();
        tatuador = (Tatuador) usuarioRepository.save(tatuador);
    }

    @Test
    @DisplayName("Deve criar um agendamento com sucesso quando não há conflito")
    void deveCriarAgendamentoComSucesso() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);

        AgendamentoRequestDTO dto = AgendamentoRequestDTO.builder()
                .clienteId(cliente.getId())
                .tatuadorId(tatuador.getId())
                .dataHora(dataHora)
                .duracaoMinutos(120)
                .cabine("Cabine 01")
                .valor(new BigDecimal("800.00"))
                .valorSinal(new BigDecimal("150.00"))
                .observacoes("Tatuagem no braço")
                .build();

        AgendamentoResponseDTO response = agendamentoService.criar(dto);

        assertNotNull(response.getId());
        assertEquals("Cabine 01", response.getCabine());
        assertEquals(StatusAgendamento.AGENDADO, response.getStatus());
        assertEquals(cliente.getNome(), response.getClienteNome());
        assertEquals(tatuador.getNome(), response.getTatuadorNome());
        assertEquals(dataHora.plusMinutes(120), response.getDataHoraFim());
    }

    @Test
    @DisplayName("Deve impedir criação de agendamento se o tatuador tiver conflito de horário")
    void deveLancarExcecaoQuandoHouverConflitoTatuador() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0);

        AgendamentoRequestDTO agendamento1 = AgendamentoRequestDTO.builder()
                .clienteId(cliente.getId())
                .tatuadorId(tatuador.getId())
                .dataHora(dataHora)
                .duracaoMinutos(120) // 10:00 até 12:00
                .cabine("Cabine 01")
                .valor(new BigDecimal("500.00"))
                .build();

        agendamentoService.criar(agendamento1);

        // Tentativa de agendar o mesmo tatuador das 11:00 às 13:00 (sobreposição) em outra cabine
        AgendamentoRequestDTO agendamentoConflitante = AgendamentoRequestDTO.builder()
                .clienteId(cliente.getId())
                .tatuadorId(tatuador.getId())
                .dataHora(dataHora.plusMinutes(60)) // 11:00
                .duracaoMinutos(120)
                .cabine("Cabine 02")
                .valor(new BigDecimal("600.00"))
                .build();

        ConflictException ex = assertThrows(ConflictException.class, () -> agendamentoService.criar(agendamentoConflitante));
        assertTrue(ex.getMessage().contains("tatuador"));
    }

    @Test
    @DisplayName("Deve impedir criação de agendamento se a cabine já estiver ocupada no mesmo horário")
    void deveLancarExcecaoQuandoHouverConflitoCabine() {
        Tatuador outroTatuador = Tatuador.builder()
                .nome("Luna Ink")
                .email("luna@teste.com")
                .senha("hash123")
                .ativo(true)
                .build();
        outroTatuador = (Tatuador) usuarioRepository.save(outroTatuador);

        LocalDateTime dataHora = LocalDateTime.now().plusDays(4).withHour(15).withMinute(0);

        AgendamentoRequestDTO agendamento1 = AgendamentoRequestDTO.builder()
                .clienteId(cliente.getId())
                .tatuadorId(tatuador.getId())
                .dataHora(dataHora)
                .duracaoMinutos(120) // 15:00 até 17:00
                .cabine("Cabine Especial")
                .valor(new BigDecimal("700.00"))
                .build();

        agendamentoService.criar(agendamento1);

        // Tentativa de outro tatuador usar a mesma cabine das 16:00 às 18:00
        AgendamentoRequestDTO agendamentoConflitoCabine = AgendamentoRequestDTO.builder()
                .clienteId(cliente.getId())
                .tatuadorId(outroTatuador.getId())
                .dataHora(dataHora.plusMinutes(60)) // 16:00
                .duracaoMinutos(120)
                .cabine("Cabine Especial")
                .valor(new BigDecimal("800.00"))
                .build();

        ConflictException ex = assertThrows(ConflictException.class, () -> agendamentoService.criar(agendamentoConflitoCabine));
        assertTrue(ex.getMessage().contains("cabine"));
    }

    @Test
    @DisplayName("Deve registrar receita e repasse financeiro automaticamente quando status muda para REALIZADO")
    void deveGerarTransacaoFinanceiraAoConcluirAgendamento() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);

        AgendamentoRequestDTO dto = AgendamentoRequestDTO.builder()
                .clienteId(cliente.getId())
                .tatuadorId(tatuador.getId())
                .dataHora(dataHora)
                .duracaoMinutos(60)
                .cabine("Cabine 03")
                .valor(new BigDecimal("1000.00"))
                .build();

        AgendamentoResponseDTO criado = agendamentoService.criar(dto);

        agendamentoService.atualizarStatus(criado.getId(), StatusAgendamento.REALIZADO, null);

        var transacoes = transacaoRepository.findAll();
        assertEquals(1, transacoes.size());
        assertEquals(new BigDecimal("1000.00"), transacoes.get(0).getValor());
        assertEquals(new BigDecimal("700.00"), transacoes.get(0).getValorRepasse()); // 70% de 1000.00
    }

    @Test
    @DisplayName("Deve aplicar 10% de desconto quando código de indicação válido for fornecido")
    void deveAplicarDezPorCentoDeDescontoComCodigoValido() {
        Cliente amigo = Cliente.builder()
                .nome("Amigo Que Indicou")
                .email("amigo@teste.com")
                .senha("hash123")
                .codigoIndicacao("TATTO-AMIGO1")
                .ativo(true)
                .build();
        usuarioRepository.save(amigo);

        LocalDateTime dataHora = LocalDateTime.now().plusDays(2).withHour(15).withMinute(0);

        AgendamentoRequestDTO dto = AgendamentoRequestDTO.builder()
                .clienteId(cliente.getId())
                .tatuadorId(tatuador.getId())
                .dataHora(dataHora)
                .duracaoMinutos(60)
                .cabine("Cabine 02")
                .valor(new BigDecimal("500.00"))
                .codigoIndicacao("TATTO-AMIGO1")
                .build();

        AgendamentoResponseDTO response = agendamentoService.criar(dto);

        assertNotNull(response.getId());
        assertEquals(new BigDecimal("500.00"), response.getValor());
        assertEquals(new BigDecimal("50.00"), response.getDescontoAplicado());
        assertEquals(new BigDecimal("450.00"), response.getValorFinal());

        // Concluir agendamento e verificar que a receita reflete o valor líquido com desconto
        agendamentoService.atualizarStatus(response.getId(), StatusAgendamento.REALIZADO, null);
        var transacoes = transacaoRepository.findAll();
        assertEquals(1, transacoes.size());
        assertEquals(new BigDecimal("450.00"), transacoes.get(0).getValor());
        assertEquals(new BigDecimal("315.00"), transacoes.get(0).getValorRepasse()); // 70% de 450.00
    }

    @Test
    @DisplayName("Deve rejeitar agendamento quando cliente tentar usar seu próprio código de indicação")
    void deveRejeitarAutoIndicacao() {
        cliente.setCodigoIndicacao("TATTO-PROPRIO");
        usuarioRepository.save(cliente);

        LocalDateTime dataHora = LocalDateTime.now().plusDays(2).withHour(17).withMinute(0);

        AgendamentoRequestDTO dto = AgendamentoRequestDTO.builder()
                .clienteId(cliente.getId())
                .tatuadorId(tatuador.getId())
                .dataHora(dataHora)
                .duracaoMinutos(60)
                .cabine("Cabine 02")
                .valor(new BigDecimal("400.00"))
                .codigoIndicacao("TATTO-PROPRIO")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> agendamentoService.criar(dto));
        assertEquals("Não é permitido utilizar o próprio código de indicação.", ex.getMessage());
    }

    @Test
    @DisplayName("Deve rejeitar agendamento quando código de indicação for inexistente")
    void deveRejeitarCodigoIndicacaoInexistente() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(2).withHour(18).withMinute(0);

        AgendamentoRequestDTO dto = AgendamentoRequestDTO.builder()
                .clienteId(cliente.getId())
                .tatuadorId(tatuador.getId())
                .dataHora(dataHora)
                .duracaoMinutos(60)
                .cabine("Cabine 02")
                .valor(new BigDecimal("300.00"))
                .codigoIndicacao("TATTO-INEXISTENTE")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> agendamentoService.criar(dto));
        assertEquals("Código de indicação inválido.", ex.getMessage());
    }

    @Test
    @DisplayName("Deve criar agendamento com desconto zero quando nenhum código for informado")
    void deveCriarAgendamentoSemDescontoQuandoNenhumCodigoInformado() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(2).withHour(19).withMinute(0);

        AgendamentoRequestDTO dto = AgendamentoRequestDTO.builder()
                .clienteId(cliente.getId())
                .tatuadorId(tatuador.getId())
                .dataHora(dataHora)
                .duracaoMinutos(60)
                .cabine("Cabine 02")
                .valor(new BigDecimal("600.00"))
                .codigoIndicacao(null)
                .build();

        AgendamentoResponseDTO response = agendamentoService.criar(dto);

        assertEquals(new BigDecimal("600.00"), response.getValor());
        assertEquals(BigDecimal.ZERO, response.getDescontoAplicado());
        assertEquals(new BigDecimal("600.00"), response.getValorFinal());
    }
}

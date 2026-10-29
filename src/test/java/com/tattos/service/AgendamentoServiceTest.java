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
class AgendamentoServiceTest {

    @Autowired
    private com.tattos.repository.CupomRepository cupomRepository;

    private AgendamentoRequestDTO agendamentoBase(BigDecimal valor) {
        return AgendamentoRequestDTO.builder()
                .clienteId(cliente.getId())
                .tatuadorId(tatuador.getId())
                .dataHora(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0))
                .duracaoMinutos(120)
                .cabine("Cabine 01")
                .valor(valor)
                .build();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        cupomRepository.deleteAll();
        transacaoRepository.deleteAll();
        agendamentoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }


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
    @DisplayName("Cupom de 10% em valor 1000 reduz total líquido para 900")
    void cupomPorcentagem() {
        cupomRepository.save(com.tattos.model.Cupom.builder()
                .codigo("DEZ10").tipo(com.tattos.model.Cupom.Tipo.PORCENTAGEM)
                .valor(new BigDecimal("10")).build());

        AgendamentoRequestDTO dto = agendamentoBase(new BigDecimal("1000.00"));
        dto.setCodigoCupom("DEZ10");

        AgendamentoResponseDTO response = agendamentoService.criar(dto);

        assertEquals(new BigDecimal("100.00"), response.getDescontoAplicado());
    }

    @Test
    @DisplayName("Cupom fixo maior que o valor trunca no total (nunca negativo)")
    void cupomFixoTruncado() {
        cupomRepository.save(com.tattos.model.Cupom.builder()
                .codigo("FIXO200").tipo(com.tattos.model.Cupom.Tipo.VALOR_FIXO)
                .valor(new BigDecimal("200.00")).build());

        AgendamentoRequestDTO dto = agendamentoBase(new BigDecimal("150.00"));
        dto.setCodigoCupom("FIXO200");

        AgendamentoResponseDTO response = agendamentoService.criar(dto);

        assertEquals(new BigDecimal("150.00"), response.getDescontoAplicado());
    }

    @Test
    @DisplayName("Cupom de tatuador A rejeitado em agendamento do tatuador B")
    void cupomDeOutroTatuador() {
        Tatuador tatB = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat B cupom").email("cupom.tatb@tattos.com").senha("hash")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
        cupomRepository.save(com.tattos.model.Cupom.builder()
                .codigo("SOB").tipo(com.tattos.model.Cupom.Tipo.PORCENTAGEM)
                .valor(new BigDecimal("10")).tatuador(tatB).build());

        AgendamentoRequestDTO dto = agendamentoBase(new BigDecimal("500.00"));
        dto.setCodigoCupom("SOB");

        assertThrows(com.tattos.exception.BusinessException.class, () -> agendamentoService.criar(dto));
    }

    @Test
    @DisplayName("Cupom esgotado (limite de usos) é rejeitado")
    void cupomEsgotado() {
        cupomRepository.save(com.tattos.model.Cupom.builder()
                .codigo("SO1").tipo(com.tattos.model.Cupom.Tipo.PORCENTAGEM)
                .valor(new BigDecimal("10")).limiteUsos(1).usos(1).build());

        AgendamentoRequestDTO dto = agendamentoBase(new BigDecimal("500.00"));
        dto.setCodigoCupom("SO1");

        assertThrows(com.tattos.exception.BusinessException.class, () -> agendamentoService.criar(dto));
    }

    @Test
    @DisplayName("Cupom de porcentagem acima de 100% é rejeitado na criação")
    void cupomPorcentagemAcimaDe100() {
        assertThrows(com.tattos.exception.BusinessException.class, () ->
                cupomService.criar("INVALIDO", com.tattos.model.Cupom.Tipo.PORCENTAGEM,
                        new BigDecimal("150"), null, null, null, null));
    }

    @Autowired
    private com.tattos.service.CupomService cupomService;

    @Test
    @DisplayName("Cupom expirado é rejeitado")
    void cupomExpirado() {
        cupomRepository.save(com.tattos.model.Cupom.builder()
                .codigo("VELHO").tipo(com.tattos.model.Cupom.Tipo.PORCENTAGEM)
                .valor(new BigDecimal("10")).validade(java.time.LocalDate.now().minusDays(1)).build());

        AgendamentoRequestDTO dto = agendamentoBase(new BigDecimal("500.00"));
        dto.setCodigoCupom("VELHO");

        assertThrows(com.tattos.exception.BusinessException.class, () -> agendamentoService.criar(dto));
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

    @Test
    @DisplayName("Deve gerar sinal de 20% e código PIX quando valor do sinal não for informado")
    void deveGerarSinalAutomaticoEPix() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0);

        AgendamentoRequestDTO dto = AgendamentoRequestDTO.builder()
                .clienteId(cliente.getId())
                .tatuadorId(tatuador.getId())
                .dataHora(dataHora)
                .duracaoMinutos(60)
                .cabine("Cabine 01")
                .valor(new BigDecimal("500.00"))
                .build();

        AgendamentoResponseDTO response = agendamentoService.criar(dto);

        assertEquals(new BigDecimal("100.00"), response.getValorSinal()); // 20% de 500
        assertFalse(response.isSinalPago());
        assertNotNull(response.getCodigoPixSinal());
        assertTrue(response.getCodigoPixSinal().contains("BR.GOV.BCB.PIX"));
    }

    @Test
    @DisplayName("Deve confirmar sinal via PIX, conciliar receita e abater na conclusão da sessão")
    void deveConfirmarSinalEConciliarReceita() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);

        AgendamentoRequestDTO dto = AgendamentoRequestDTO.builder()
                .clienteId(cliente.getId())
                .tatuadorId(tatuador.getId())
                .dataHora(dataHora)
                .duracaoMinutos(120)
                .cabine("Cabine 01")
                .valor(new BigDecimal("1000.00"))
                .valorSinal(new BigDecimal("200.00"))
                .build();

        AgendamentoResponseDTO criado = agendamentoService.criar(dto);
        assertEquals(StatusAgendamento.AGENDADO, criado.getStatus());
        assertFalse(criado.isSinalPago());

        // 1. Confirma pagamento de sinal
        AgendamentoResponseDTO comSinalPago = agendamentoService.confirmarPagamentoSinal(criado.getId());
        assertEquals(StatusAgendamento.CONFIRMADO, comSinalPago.getStatus());
        assertTrue(comSinalPago.isSinalPago());

        var transacoesAposSinal = transacaoRepository.findAll();
        assertEquals(1, transacoesAposSinal.size());
        assertEquals(new BigDecimal("200.00"), transacoesAposSinal.get(0).getValor());
        assertEquals("PIX", transacoesAposSinal.get(0).getFormaPagamento());

        // Idempotência do sinal: chamada repetida não duplica transação
        agendamentoService.confirmarPagamentoSinal(criado.getId());
        assertEquals(1, transacaoRepository.findAll().size());

        // 2. Conclui agendamento: deve gerar apenas a receita do saldo restante (1000 - 200 = 800)
        agendamentoService.atualizarStatus(criado.getId(), StatusAgendamento.REALIZADO, null);
        var transacoesFinais = transacaoRepository.findAll();
        assertEquals(2, transacoesFinais.size());

        BigDecimal totalRecebido = transacoesFinais.stream()
                .map(com.tattos.model.TransacaoFinanceira::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Conciliação perfeita: R$ 200 (sinal) + R$ 800 (saldo restante) = R$ 1000 (total)
        assertEquals(new BigDecimal("1000.00"), totalRecebido);
    }
}

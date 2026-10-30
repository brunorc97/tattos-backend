package com.tattos.service;

import com.tattos.dto.request.AgendamentoRequestDTO;
import com.tattos.exception.ConflictException;
import com.tattos.model.Bancada;
import com.tattos.model.Cliente;
import com.tattos.model.Tatuador;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.BancadaRepository;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AgendamentoConflitoBancadaTest {

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private BancadaRepository bancadaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Cliente cliente;
    private Tatuador tatA;
    private Tatuador tatB;
    private Bancada b1;
    private Bancada b2;
    private LocalDateTime inicio;

    @BeforeEach
    void setUp() {
        agendamentoRepository.deleteAll();
        bancadaRepository.deleteAll();
        usuarioRepository.deleteAll();

        cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cli Conf").email("conf.c@tattos.com").senha("h").ativo(true).build());
        tatA = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat A").email("confa.t@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
        tatB = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat B").email("confb.t@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
        b1 = bancadaRepository.save(Bancada.builder().nome("Bancada 1").build());
        b2 = bancadaRepository.save(Bancada.builder().nome("Bancada 2").build());
        inicio = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0);
    }

    private AgendamentoRequestDTO dto(Tatuador tatuador, Bancada bancada, LocalDateTime ini) {
        return AgendamentoRequestDTO.builder()
                .clienteId(cliente.getId())
                .tatuadorId(tatuador.getId())
                .dataHora(ini)
                .duracaoMinutos(120)
                .cabine(bancada != null ? bancada.getNome() : "Cabine Legada")
                .bancadaId(bancada != null ? bancada.getId() : null)
                .valor(new BigDecimal("500.00"))
                .build();
    }

    @Test
    @DisplayName("Conflito: mesma bancada, horarios sobrepostos (tatuadores diferentes) -> 409")
    void conflitoMesmaBancada() {
        agendamentoService.criar(dto(tatA, b1, inicio));
        assertThrows(ConflictException.class,
                () -> agendamentoService.criar(dto(tatB, b1, inicio.plusMinutes(30))));
    }

    @Test
    @DisplayName("Sem conflito: bancadas diferentes no mesmo horario")
    void semConflitoBancadasDiferentes() {
        agendamentoService.criar(dto(tatA, b1, inicio));
        assertDoesNotThrow(() -> agendamentoService.criar(dto(tatB, b2, inicio)));
    }

    @Test
    @DisplayName("Legado: agendamento so com cabine continua validando conflito por texto")
    void legadoSomenteCabine() {
        agendamentoService.criar(dto(tatA, null, inicio));
        assertThrows(ConflictException.class,
                () -> agendamentoService.criar(dto(tatB, null, inicio.plusMinutes(30))));
    }

    @Test
    @DisplayName("Conflito por bancadaId mesmo apos renomear a bancada")
    void conflitoMesmoAposRenomearBancada() {
        agendamentoService.criar(dto(tatA, b1, inicio));
        b1.setNome("Bancada Um (renomeada)");
        bancadaRepository.save(b1);

        assertThrows(ConflictException.class,
                () -> agendamentoService.criar(dto(tatB, b1, inicio.plusMinutes(30))));
    }

    @Test
    @DisplayName("Reserva por bancadaId detecta agendamento legado (cabine-only) no mesmo espaco")
    void reservaPorBancadaDetectaLegadoCabine() {
        // legado: mesmo espaço físico (cabine = nome da bancada), sem bancada_id
        agendamentoService.criar(AgendamentoRequestDTO.builder()
                .clienteId(cliente.getId()).tatuadorId(tatA.getId())
                .dataHora(inicio).duracaoMinutos(120)
                .cabine("Bancada 1").valor(new BigDecimal("500.00")).build());

        assertThrows(ConflictException.class,
                () -> agendamentoService.criar(dto(tatB, b1, inicio.plusMinutes(30))));
    }
}

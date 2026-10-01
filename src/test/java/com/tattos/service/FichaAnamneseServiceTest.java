package com.tattos.service;

import com.tattos.dto.request.FichaAnamneseRequestDTO;
import com.tattos.dto.response.FichaAnamneseResponseDTO;
import com.tattos.exception.BusinessException;
import com.tattos.model.Cliente;
import com.tattos.repository.FichaAnamneseRepository;
import com.tattos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FichaAnamneseServiceTest {

    @Autowired
    private FichaAnamneseService fichaService;

    @Autowired
    private FichaAnamneseRepository fichaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        fichaRepository.deleteAll();
        usuarioRepository.deleteAll();

        cliente = Cliente.builder()
                .nome("Mariana Souza")
                .email("mariana@teste.com")
                .senha("pass123")
                .ativo(true)
                .build();
        cliente = (Cliente) usuarioRepository.save(cliente);
    }

    @Test
    @DisplayName("Deve registrar ficha de anamnese e ativar alerta clínico e dermatológico automaticamente")
    void deveRegistrarFichaComAlertas() {
        FichaAnamneseRequestDTO dto = FichaAnamneseRequestDTO.builder()
                .clienteId(cliente.getId())
                .historicoSaude("Histórico familiar de queloide")
                .alergias("Alergia a pigmentos vermelhos e látex")
                .termoAssinado(true)
                .assinaturaDigital("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
                .diabetes(true)
                .hemofilia(false)
                .hipertensao(false)
                .gestanteLactante(false)
                .observacoes("Primeira tatuagem")
                .build();

        FichaAnamneseResponseDTO response = fichaService.registrarFicha(dto);

        assertNotNull(response.getId());
        assertTrue(response.getAlertaClinico(), "Deveria ter alerta clínico devido a diabetes e alergias");
        assertTrue(response.getRiscoDermatologico(), "Deveria ter risco dermatológico devido a queloide e látex");
        assertTrue(response.getTermoAssinado());
        assertEquals("Mariana Souza", response.getClienteNome());

        // Valida se o cliente NÃO está apto direto sem avaliação prévia
        assertFalse(fichaService.validarAptidaoCliente(cliente.getId()));
    }

    @Test
    @DisplayName("Deve impedir confirmação de termo assinado sem o envio da assinatura digital")
    void deveExigirAssinaturaDigitalQuandoTermoAssinado() {
        FichaAnamneseRequestDTO dto = FichaAnamneseRequestDTO.builder()
                .clienteId(cliente.getId())
                .termoAssinado(true)
                .assinaturaDigital(null) // Ausente
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> fichaService.registrarFicha(dto));
        assertTrue(ex.getMessage().contains("assinatura digital"));
    }
}

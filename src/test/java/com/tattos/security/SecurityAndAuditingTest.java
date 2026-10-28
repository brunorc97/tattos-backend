package com.tattos.security;

import com.tattos.model.Insumo;
import com.tattos.repository.InsumoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityAndAuditingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InsumoRepository insumoRepository;

    @BeforeEach
    void setUp() {
        insumoRepository.deleteAll();
    }

    @Test
    @DisplayName("Auditoria: deve preencher createdBy, lastModifiedBy, createdAt e updatedAt como SISTEMA quando não autenticado")
    void deveAuditarAutomaticamenteSemUsuarioAutenticado() {
        Insumo insumo = Insumo.builder()
                .nome("Tinta Preta Tribal")
                .quantidadeAtual(10)
                .nivelCritico(2)
                .unidadeMedida("UN")
                .valorUnitario(new BigDecimal("90.00"))
                .build();

        Insumo salvo = insumoRepository.saveAndFlush(insumo);

        assertNotNull(salvo.getId());
        assertEquals("SISTEMA", salvo.getCreatedBy());
        assertEquals("SISTEMA", salvo.getLastModifiedBy());
        assertNotNull(salvo.getCreatedAt());
        assertNotNull(salvo.getUpdatedAt());
    }

    @Test
    @WithMockUser(username = "gestor@tattos.com", roles = {"GESTOR"})
    @DisplayName("Auditoria: deve preencher createdBy e lastModifiedBy com o email do usuário autenticado")
    void deveAuditarComUsuarioAutenticado() {
        Insumo insumo = Insumo.builder()
                .nome("Tinta Vermelha Viper")
                .quantidadeAtual(5)
                .nivelCritico(1)
                .unidadeMedida("UN")
                .valorUnitario(new BigDecimal("85.00"))
                .build();

        Insumo salvo = insumoRepository.saveAndFlush(insumo);

        assertNotNull(salvo.getId());
        assertEquals("gestor@tattos.com", salvo.getCreatedBy());
        assertEquals("gestor@tattos.com", salvo.getLastModifiedBy());
    }

    @Test
    @DisplayName("RBAC: deve barrar requisições não autenticadas em rotas protegidas de estoque")
    void deveBloquearAcessoNaoAutenticadoEmEstoque() throws Exception {
        mockMvc.perform(get("/api/estoque"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "cliente@tattos.com", roles = {"CLIENTE"})
    @DisplayName("RBAC: deve proibir CLIENTE de acessar rotas de estoque (retornar 403 Forbidden)")
    void deveProibirClienteEmEstoque() throws Exception {
        mockMvc.perform(get("/api/estoque"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "tatuador@tattos.com", roles = {"TATUADOR"})
    @DisplayName("RBAC: deve permitir TATUADOR listar estoque")
    void devePermitirTatuadorListarEstoque() throws Exception {
        mockMvc.perform(get("/api/estoque"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "tatuador@tattos.com", roles = {"TATUADOR"})
    @DisplayName("RBAC: deve impedir TATUADOR de deletar insumo (apenas GESTOR pode deletar)")
    void deveImpedirTatuadorDeletarInsumo() throws Exception {
        Insumo insumo = insumoRepository.save(Insumo.builder()
                .nome("Batoque")
                .quantidadeAtual(100)
                .nivelCritico(20)
                .unidadeMedida("UN")
                .valorUnitario(new BigDecimal("0.50"))
                .build());

        mockMvc.perform(delete("/api/estoque/" + insumo.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "gestor@tattos.com", roles = {"GESTOR"})
    @DisplayName("RBAC: deve permitir GESTOR deletar insumo")
    void devePermitirGestorDeletarInsumo() throws Exception {
        Insumo insumo = insumoRepository.save(Insumo.builder()
                .nome("Batoque descartável")
                .quantidadeAtual(100)
                .nivelCritico(20)
                .unidadeMedida("UN")
                .valorUnitario(new BigDecimal("0.50"))
                .build());

        mockMvc.perform(delete("/api/estoque/" + insumo.getId()))
                .andExpect(status().isNoContent());
    }
}

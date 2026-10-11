---
name: tattos-frontend-sync
description: Sincroniza e adapta a aplicação frontend com os contratos REST do backend, autenticação JWT, formulários e regras de fidelização.
---

# TattOS Front-End Synchronization Skill

## Missão
Garantir a integração perfeita, sem atrito e tipada entre o front-end React/TypeScript e os serviços REST do TattOS ERP, adaptando telas existentes e novas funcionalidades conforme o backlog do backend evolui.

---

## 1. Mapeamento de Contratos e DTOs Recentes

### A. Fidelização (Indique e Ganhe)
* **Perfil do Usuário (`GET /api/auth/me` ou `GET /api/usuarios/{id}`):**
  - O DTO agora inclui o campo `codigoIndicacao` (`string`, ex: `"TATTO-A1B2C3"`).
  - **Ação Front-End:** Exibir na tela do Cliente um card destacado com o seu código de indicação, um botão de "Copiar Código" (usando a Clipboard API) e texto explicativo (*"Compartilhe com amigos e dê 10% de desconto no primeiro agendamento!"*).
* **Novo Agendamento (`POST /api/agendamentos`):**
  - O payload aceita `codigoIndicacao?: string`.
  - O response retorna:
    - `valor`: valor base original do procedimento.
    - `descontoAplicado`: valor monetário descontado (10% se aplicável, ou `0.00`).
    - `valorFinal`: valor líquido a pagar (`valor - descontoAplicado`).
  - **Ação Front-End:** Adicionar um campo opcional *"Tem um código de indicação de amigo?"* no formulário de reserva, com feedback visual do valor com desconto.

---

## 2. Tratamento Unificado de Erros da API

O backend responde erros no formato padronizado:
```json
{
  "timestamp": "2026-10-11T20:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Não é permitido utilizar o próprio código de indicação.",
  "path": "/api/agendamentos"
}
```

### Regra de Consumo:
1. Sempre capturar `error.response?.data?.message` e exibir diretamente ao usuário em toasts de erro ou na validação do campo do formulário.
2. Não mascarar mensagens de validação com textos genéricos. O backend fornece mensagens claras para conflitos de horário, autoindicação e código inválido.

---

## 3. Gestão de Rotas e Autenticação JWT

1. **Armazenamento:**
   - Salvar o token retornado em `LoginResponseDTO.token` e adicionar o cabeçalho:
     `Authorization: Bearer <token>`
2. **Guarda de Rotas (`ProtectedRoute`):**
   - Rota `/gestao/*`: Apenas usuários com `role === 'ROLE_GESTOR'`.
   - Rota `/tatuador/*`: Apenas usuários com `role === 'ROLE_TATUADOR'` ou `ROLE_GESTOR`.
   - Rota `/meus-agendamentos`: Acessível por `ROLE_CLIENTE`.

---

## 4. Checklist de Adequação do Front-End

- [ ] Interface de Perfil: Componente visual do Código de Indicação com botão de cópia rápida.
- [ ] Formulário de Agendamento: Input opcional de `codigoIndicacao` e exibição dinâmica de resumo financeiro (Valor, Desconto e Total Final).
- [ ] Interceptor HTTP: Injeção automática de Bearer Token e interceptação de 401 para logout.
- [ ] Responsividade: Telas utilizáveis em smartphones (tatuadores na bancada e clientes agendando pelo celular).

# Budget (Orçamento) Feature

Implemented the Budget module with the following additions:

## Backend (tattos-backend)
- **Migrations**
  - `V9__create_orcamento_table.sql`
  - `V10__create_projeto_and_kanbancard_tables.sql`
- **Entities**
  - `Orcamento.java`
  - `Projeto.java`
  - `KanbanCard.java`
- **Repositories**
  - `OrcamentoRepository.java`
  - `ProjetoRepository.java`
  - `KanbanCardRepository.java`
- **DTOs**
  - Request: `OrcamentoRequestDTO.java`, `ProjetoRequestDTO.java`, `KanbanCardRequestDTO.java`
  - Response: `OrcamentoResponseDTO.java`, `ProjetoResponseDTO.java`, `KanbanCardResponseDTO.java`
- **Services**
  - `OrcamentoService.java`
  - `ProjetoService.java`
  - `KanbanCardService.java`
- **Controllers**
  - `OrcamentoController.java`
  - `ProjetoController.java`
  - `KanbanCardController.java`
- **Tests** (example)
  - `OrcamentoServiceTest.java`

## Frontend (tattos-frontend)
- **Pages**
  - `Orcamento.jsx` – List and create budgets.
- **Components**
  - `KanbanBoard.jsx` – Simple board rendering budgets.
  - `ProjectApprovalModal.jsx` – Modal for approving projects.

## Documentation
- Updated `README.md` with a note about the new Budget feature.

All files are placed under the appropriate packages and compile with Java 25 and Gradle.

# Spec v2 (verbatim, colada pelo usuário em 2026-10-29)

Se já tiver um módulo parecido não precisa criar nada, senão tiver criar o módulo.

🧭 1. Navegação, Layout e Acessos
* Centralização de Menus: Na visão das roles ROLE_GESTOR e ROLE_TATUADOR, centralizar todas as opções de menu na tela (se necessário, alinhar o logo para a esquerda).
* Remoção de Funcionalidade: Retirar completamente o módulo "Tattoo AI".
* Correção de Terminologia: Trocar todas as ocorrências de "ateliê" para "Studio" em todo o sistema e remover termos com cara de inteligência artificial (como referências a "aerografia" nos gráficos financeiros).
* Correção de Erros (Acessos de Teste): Corrigir tokens ou falhas que estão quebrando a página e redirecionando para o início ao clicar em determinadas abas (ex: aba de tatuadores com acesso de teste).
* Restrições de Acesso por Role:
    * Tatuador (ROLE_TATUADOR): NÃO pode ter acesso à aba "Tatuadores"; tem acesso livre às abas de Estoque e Bancada; NÃO tem acesso ao Dashboard geral (visualiza apenas as suas próprias informações).
    * Gestor (ROLE_GESTOR): acesso completo às abas de Estoque e Bancada.

📋 2. Módulo de Ficha de Anamnese & Biossegurança
* Correção Crítica: A ficha de anamnese parou de funcionar e não está permitindo o preenchimento de novas fichas (clientes, gestores e tatuadores).
* Regras de Visualização: Cliente vê apenas as próprias fichas; Tatuador vê exclusivamente as fichas dos seus clientes; Gestor vê todas, com filtro por tatuador, cliente e período (mês/ano).

🎨 3. Módulo de Projetos e Orçamentos
* Vínculo com o Cliente: Todo projeto atrelado ao seu cliente.
* Prazos e Status: prazo para término; status Aprovado ou Não Aprovado registrado no perfil do cliente; se não aprovado, salvar motivo da recusa (valor, não gostou do desenho, data adiada, etc.) para contatos posteriores.
* Controle de Sinal: indicar se o projeto necessita de sinal (abatido do total); registrar se foi pago, como e quando; cliente pode enviar comprovante anexado (PDF, tamanho limitado em MB) para tatuador/gestor dar entrada.
* Quadro Kanban de Projetos: flag de andamento indicando no prazo ou atrasado (aviso visual).
* Permissões: Tatuador vê apenas projetos alocados a ele; Gestor vê todos, com filtro por tatuador e status.

👥 4. Módulo de Clientes (CRM)
* Nova Aba de Clientes para tatuador e gestor, com navegação e contato direto.
* Busca por e-mail, nome ou telefone (remover limitação de busca por ID).
* Visibilidade: Tatuador vê apenas seus clientes; Gestor vê todos.
* Perfil do Cliente: histórico de contato (CRM); quantidade de trabalhos e projetos; projetos que não viraram trabalhos (com pagamentos, datas, valores); trabalhos feitos (anexar imagem, tatuador responsável, data); data de nascimento (campanhas de aniversário); histórico de fichas de anamnese e alergias.

💰 5. Módulo Financeiro & Máquinas de Cartão
* Separar estritamente receita de despesa.
* Detalhes de pagamento exibidos.
* Custos de Transação: módulo de cadastro de taxas por operadora (Stone, PagSeguro, Rede, etc.) e por modalidade (débito, crédito à vista, crédito parcelado).
* Gráficos: interatividade (clicar em despesas exibe interações de despesas; clicar em receita, de receitas); dashboard mais profissional.

📅 6. Módulo de Agenda & Sessões
* Busca de clientes por nome, telefone ou e-mail (gestores e tatuadores), sem limitação por ID.
* Visão: Gestor vê tudo (filtros por bancada/tatuador); Tatuador vê só a sua agenda; Cliente vê estritamente as suas sessões.
* Próximas Sessões: tatuador vê apenas as suas, com filtros; gestor vê todas, com filtros.
* Fluxo de Agendamento do Cliente: enviar solicitação inicial com ideia/desenho/imagem de exemplo; atrelar projeto; indicar dia/semana de preferência; selecionar tatuador de preferência ou por estilo — sistema cruza com estilos dos tatuadores e indica a melhor opção.

🪑 7. Módulo de Bancada
* Filtros por Horário/Dia: tatuador verifica se a bancada está livre por horário e dia, podendo marcar/registrar uso.
* Acesso restrito a tatuador e gestor.

📦 8. Módulo de Estoque
* Acesso restrito a tatuador e gestor.
* Baixa (Saída): obrigatório alocar o tatuador que utilizou; tela para digitar quantidade; gestor pode selecionar o tatuador.
* Entrada & Custo Médio: tela similar exigindo custo de aquisição; cálculo automático de custo médio (ex.: 10 agulhas a R$1 + 10 a R$10 → custo médio proporcional).

💸 9. Comissões, Pagamentos e Adiantamentos
* Visão Gestor: aba de tatuadores → clicar em tatuador → filtros de data → lista de trabalhos com comissões → marcar/selecionar trabalhos somando comissões → status "Pagamento Agendado" com tela de dados bancários (Pix + anexar PDF do comprovante) ou dinheiro (anexar recibo).
* Adiantamentos: módulo dedicado, gestor anexa recibo; se tatuador tem adiantamento em aberto, fluxo de pagamento abate automaticamente.
* Recibos e Histórico: ao pagar, gerar recibo detalhado com exportação PDF salva no sistema; consultar histórico de pagamentos e adiantamentos.
* Visão Tatuador: no próprio perfil, ver trabalhos, valores, status de recebimento (recebido/pendente), com filtros por data.

🏷️ 10. Promoções e Cupons
* Gestor e Tatuador criam promoções/cupons de desconto (diferentes de códigos de indicação).
* Abrangência: todos os tatuadores do studio ou um tatuador específico.
* Tipos: porcentagem ou valor fixo.

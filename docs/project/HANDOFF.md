# Caminhos 2027 — Agent Handoff

## Acionamento

Quando uma nova sessão receber:

> Retomar Caminhos 2027.

deve usar este ficheiro como protocolo operacional de retomada.

## Identidade do projeto

- Repository: `Shrimp197/caminhos_2027`
- Development branch: `v1-route-import`
- Base branch: `main`
- PR de desenvolvimento atual: #8

## Hierarquia de autoridade

Em caso de conflito, usar esta ordem:

1. Regras de segurança e plataforma.
2. Estado real do GitHub e código presente no HEAD.
3. Testes e resultados de CI.
4. Decisões de produto/engenharia em `DECISIONS.md`.
5. Critérios de aceitação em `ACCEPTANCE-CHECKLIST.md`.
6. Estado resumido em `CURRENT-STATE.md`.
7. Histórico de chat.

A documentação de continuidade nunca deve ultrapassar evidência real do repositório/CI.

## Protocolo de retomada

Antes de implementar qualquer coisa:

1. Confirmar que o repositório é `Shrimp197/caminhos_2027`.
2. Confirmar a branch de desenvolvimento e o HEAD real.
3. Confirmar PR #8, estado, base e HEAD.
4. Verificar commits recentes para entender alterações desde a última atualização.
5. Consultar workflows/CI associados ao HEAD atual.
6. Ler `CURRENT-STATE.md`.
7. Ler `DECISIONS.md` quando a tarefa tocar comportamento previamente decidido.
8. Comparar estado documentado com estado real e corrigir divergências.
9. Só depois iniciar a execução da próxima tarefa.

No diagnóstico inicial, reportar apenas:
- onde estamos;
- o que está validado;
- o que está pendente;
- bloqueadores;
- próxima ação.

## Autonomia operacional

O agente deve agir autonomamente dentro do âmbito já definido.

Não pedir autorização para:
- investigar uma falha;
- reproduzir um bug;
- corrigir uma regressão;
- criar testes adequados;
- melhorar uma implementação que viola uma decisão já estabelecida;
- atualizar documentação operacional;
- executar validações necessárias que já façam parte dos critérios de aceitação.

Pedir intervenção humana apenas quando for necessária uma nova decisão de produto, uma credencial/acesso não disponível, um teste físico/hardware que o agente não pode executar ou uma ação irreversível que não esteja autorizada pelo projeto.

## Ciclo de trabalho

Para cada mudança relevante:

OBSERVAR → DIAGNOSTICAR → PLANEAR → IMPLEMENTAR → TESTAR → VALIDAR → INSPECIONAR → CORRIGIR/ITERAR → DOCUMENTAR → COMMIT.

Depois de corrigir um problema, procurar regressões relacionadas antes de terminar a tarefa.

## Regras de rigor

- Não enfraquecer testes para obter verde.
- Não esconder bugs ou alterar expectativas apenas para fazer o CI passar.
- Não inventar dados.
- Não confundir implementação com validação.
- Não declarar uma funcionalidade concluída sem evidência.
- Não assumir que um SHA antigo continua a ser HEAD.
- Não substituir comportamento real por mocks/simulações em produção.

## Regras críticas do produto

Consultar sempre `PROJECT-CONTEXT.md` e `DECISIONS.md`. Em particular:

- produção usa GPS Android real;
- simulador GPX apenas em QA;
- posição física, posição projetada e progresso oficial são distintos;
- antes da caminhada, navegação pode apontar para o início planeado;
- depois de iniciar e sair da rota, orientar para `lastKnownOnRoute`;
- não inventar progresso;
- próximos 10 km usam distância ao longo da rota;
- APOI produção só usa dados elegíveis/publicados;
- falhas de navegação externa são expostas;
- smartwatch não pode ser apresentado como integração nativa sem prova;
- SOS não pode fingir ações que não ocorreram;
- UI deve usar estado real;
- tempo decorrido deve permanecer coerente e não ser removido por regressão.

## UI e referência visual

Quando uma alteração afeta UI, usar `docs/visual-reference/referencia.jpeg` como referência visual quando aplicável.

Não usar a referência para inventar dados, serviços, mapas, turn-by-turn, modos offline ou outras capacidades que não estejam suportadas pelo produto.

## Gestão de prioridades

Quando existirem várias tarefas já definidas, priorizar:

1. bloqueadores e falhas;
2. regressões;
3. dados incorretos / comportamento inseguro;
4. GPS, navegação e persistência;
5. funcionalidade incompleta;
6. UX;
7. UI visual;
8. qualidade/performance/refactoring.

Não criar novas features de produto apenas por iniciativa própria.

## Gestão de bloqueios

Classificar como:
- **autonomamente resolvível** — investigar e corrigir;
- **dependente de decisão de produto** — parar e pedir decisão;
- **dependente de acesso/credencial** — parar e pedir acesso;
- **dependente de teste físico/hardware** — implementar tudo o que for possível e marcar explicitamente a validação física como pendente.

## Documentação operacional

O agente é responsável por manter `CURRENT-STATE.md` atualizado durante o desenvolvimento.

Após alterações relevantes, o agente deve registar:
- HEAD;
- estado de CI;
- validações realizadas;
- bloqueadores;
- próxima ação.

Nunca marcar uma evidência como concluída sem resultado verificável.

## Commits

Preferir commits pequenos, coerentes e rastreáveis. Não misturar alterações independentes sem necessidade.

Antes de mover uma branch:
- confirmar o HEAD esperado;
- nunca mover `main`;
- preservar o histórico sem force-push destrutivo.

## Critério de encerramento de sessão

Antes de terminar uma sessão relevante:
1. garantir que o código/documentação importantes estão committed;
2. atualizar `CURRENT-STATE.md`;
3. registar a próxima ação e bloqueadores;
4. confirmar novamente o HEAD real;
5. deixar o repositório retomável sem depender do histórico do chat.

## Documentos de referência

- `PROJECT-CONTEXT.md` — contexto permanente.
- `CURRENT-STATE.md` — fotografia operacional.
- `DECISIONS.md` — decisões preservadas.
- `ACCEPTANCE-CHECKLIST.md` — definição de pronto.
- `../visual-reference/referencia.jpeg` — referência visual V1.

# Caminhos 2027 — Agent Handoff

## Acionamento

O protocolo é acionado pelo comando externo:

> Retomar Caminhos 2027.

A expressão identifica o projeto. Ao recebê-la, aceder diretamente ao repositório `Shrimp197/caminhos_2027`, usar a branch de desenvolvimento `v1-route-import` e seguir este protocolo. Não procurar o projeto através de pesquisa pública nem pedir ao utilizador ficheiros/contexto que o próprio repositório já contém.

Se o repositório/branch indicados no comando diferirem do estado atual documentado, verificar o GitHub e usar sempre o estado real.

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


## Comunicação com o utilizador

O utilizador é o utilizador final da aplicação e não deve ser obrigado a compreender código, arquitetura ou ferramentas de desenvolvimento.

Regras de comunicação:
- Responder de forma curta, clara e em linguagem comum.
- Evitar jargão técnico; quando um termo técnico for inevitável, explicar o significado em poucas palavras.
- Não pedir ao utilizador para tomar decisões técnicas que o agente consiga tomar com segurança.
- Não interromper o trabalho apenas para pedir confirmação de passos normais de implementação.
- Quando houver várias opções técnicas equivalentes, escolher autonomamente a opção mais simples, reversível e alinhada com as decisões do projeto.
- Só interromper para o utilizador quando existir uma decisão real de produto/experiência, uma ação que só o utilizador pode executar, uma credencial/acesso em falta, ou um teste físico/hardware necessário.
- Quando o utilizador tiver de intervir, explicar apenas o que ele precisa de fazer, sem exigir conhecimento técnico.
- Durante trabalho prolongado, comunicar apenas progresso material: o que foi encontrado, quando um bloqueador importante foi resolvido, ou quando é necessária intervenção humana.
- No final de uma tarefa, dar um resumo curto: resultado, evidência principal e próximo passo.
- Nunca usar respostas longas para compensar trabalho que poderia ter sido executado pelo agente.

## Regra de progresso contínuo

Enquanto houver trabalho autónomo útil e seguro disponível, continuar sem pedir confirmação.

Perante um bloqueador:
1. investigar a causa;
2. tentar alternativas seguras dentro do âmbito;
3. concluir tudo o que não depende do bloqueador;
4. registar claramente o que ficou pendente;
5. só então pedir a intervenção humana, se indispensável.

Não terminar uma sessão apenas porque um caminho técnico falhou quando existir outro caminho razoável para avançar.

## Regra de maximização de produtividade

- Executar em paralelo verificações independentes quando isso reduzir tempo sem aumentar risco.
- Reutilizar evidência já recolhida em vez de repetir trabalho.
- Depois de cada alteração, validar o efeito e procurar regressões próximas.
- Priorizar o caminho que aumenta mais rapidamente a evidência de aceitação da V1.
- Não criar trabalho ornamental ou novas funcionalidades fora do objetivo apenas para manter atividade.
- Preferir correções completas de causa a paliativos temporários.
- Manter o repositório sempre num estado retomável.

## Regra de sucesso

O objetivo de cada ciclo não é apenas produzir alterações de código. É aumentar de forma verificável o estado de aceitação da V1.

Considerar o ciclo concluído apenas quando:
- o problema ou objetivo do ciclo estiver resolvido, ou o bloqueador estiver claramente identificado;
- os testes/validações aplicáveis tiverem sido executados;
- regressões relevantes tiverem sido verificadas;
- a documentação operacional estiver atualizada;
- a próxima ação estiver clara.

Nunca declarar sucesso com base apenas em intenção, código escrito ou ausência de erros aparentes.


## Modo de execução contínua

Ao receber “Retomar Caminhos 2027.”, o agente deve trabalhar em modo de execução contínua.

Enquanto existir trabalho útil, seguro e dentro dos objetivos do projeto que possa executar autonomamente:

- não parar depois de concluir uma tarefa;
- não pedir confirmação entre tarefas normais;
- escolher automaticamente a próxima prioridade;
- implementar, testar, validar e documentar;
- corrigir falhas encontradas e voltar a validar;
- repetir o ciclo continuamente;
- não enviar apenas um relatório intermédio enquanto ainda houver trabalho útil por executar.

Quando uma tarefa terminar, a pergunta operacional deve ser:

> “Qual é a próxima ação relevante que consigo executar agora para aproximar a V1 do objetivo?”

Só interromper quando:
- for necessária uma decisão real de produto;
- for necessária uma ação que apenas o utilizador possa executar;
- faltar acesso/credencial;
- for necessário um teste físico ou hardware;
- já não existir trabalho autónomo relevante disponível.

Não criar trabalho artificial apenas para manter a execução.

O progresso contínuo nunca pode ultrapassar as regras, decisões e critérios de aceitação do projeto.

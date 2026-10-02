# Caminhos 2027 — Project Context

## Objetivo

Caminhos 2027 é um produto Android para apoio real à experiência de caminhada no percurso do Caminho do Centenário. A V1 deve privilegiar confiança, clareza e comportamento verificável em condições reais de utilização.

O objetivo não é apenas apresentar uma interface convincente: a V1 deve ligar produto, dados, GPS, navegação, persistência, UX/UI, testes, CI e validação física de forma coerente.

## Fonte de verdade

O repositório GitHub é a fonte de verdade operacional do projeto. A documentação ajuda a preservar contexto, mas nunca substitui a verificação do estado real do código, branch, commits, PR e CI.

Repository: `Shrimp197/caminhos_2027`
Development branch: `v1-route-import`
Base branch: `main`
Current PR: #8

## Regras permanentes

- Nunca alterar `main` como parte do desenvolvimento normal.
- Não assumir que um SHA histórico continua a ser HEAD.
- Não inventar dados de produção, serviços, horários, disponibilidade, cartografia ou instruções.
- Dados de QA só podem existir sob `BuildConfig.DEBUG` e nunca podem mascarar a ausência de dados de produção.
- Produção usa GPS Android real; simulação GPX é exclusivamente para QA.
- Manter separados: posição física do dispositivo, posição projetada na rota e progresso oficial da caminhada.
- Antes de iniciar a caminhada, o utilizador pode estar fora da rota; o alvo de navegação é o início planeado e o progresso oficial continua separado.
- Depois de iniciar a caminhada, uma saída da rota deve orientar para `lastKnownOnRoute`; nunca saltar para o início nem inventar progresso.
- A progressão oficial é baseada na rota, não em distância em linha reta.
- “Próximos 10 km” significa distância ao longo da geometria da rota.
- Falhas de navegação externa devem ser expostas; lançar um intent não é prova de que outra aplicação executou a navegação.
- Informação histórica não deve ser apresentada como garantia atual.
- Smartwatch só pode ser descrito como integração nativa quando existir integração real. Caso contrário, a superfície deve permanecer explicitamente limitada ao que a aplicação/plataformas externas suportam.
- SOS deve refletir apenas ações Android realmente executadas e nunca fingir envio de comunicações.
- A UI deve estar ligada ao estado real de `WalkingState`/`AppStateStore`; não manter mocks permanentes.
- A referência visual em `docs/visual-reference/referencia.jpeg` é uma referência de UX/UI, não autorização para inventar funcionalidades ou dados que não existam.

## Princípio de qualidade

Não confundir implementação com validação:

implementado ≠ testado ≠ validado em CI ≠ validado visualmente ≠ validado fisicamente em Android real.

A V1 só é considerada concluída quando os critérios de entrega documentados em `ACCEPTANCE-CHECKLIST.md` tiverem evidência suficiente.

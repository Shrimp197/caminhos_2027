# Caminhos 2027 — Current State

> Estado inicial desta documentação de continuidade, sincronizado com o GitHub em 2026-09-30.

## Identidade atual

- Repository: `Shrimp197/caminhos_2027`
- Development branch: `v1-route-import`
- Base branch: `main`
- Pull Request: #8
- PR title: V1 route import — official geometry provenance and runtime policy
- PR state: open, draft
- Current HEAD do último estado verificado: `cd44acb6e738bef347eaca5940cd25a8d95cb311`
- Base SHA do PR: `c5222c1fb3052ea22ccf4b25701257a84f55f704`
- Última implementação validada: `cd44acb6e738bef347eaca5940cd25a8d95cb311` — correção da inicialização offline do mapa.
- Última alteração verificada: `docs/visual-reference/referencia.jpeg`

## CI da implementação validada

Na implementação `cd44acb6e738bef347eaca5940cd25a8d95cb311`:

- V1 Route Import Validation: sucesso (run 1590).
- V1 Route Source Provenance: sucesso (run 2155).
- V1 Route Import Build: sucesso (run 1603).
- O build passou por testes JVM, build/assinatura do APK, emulador Android e E2E.

A evidência visual do build inclui `preparacao.png` e `navegacao.png`. A última mostra cartografia renderizada depois da reconstrução da caminhada sem rede, confirmando a correção observada no mapa offline.

## Produto atualmente documentado no PR

- Cartografia nativa MapLibre no mapa de caminhada.
- Traçado oficial, início, destino, posição do peregrino e próximo APOI no read model/mapa.
- Cartografia OpenFreeMap/MapLibre com atribuição visível.
- Mecanismo real de guardar cartografia offline por região, com disponibilidade apenas após conclusão.
- Bottom sheet redimensionável por gesto sobre o mapa.
- Orientação por bearing configurável.
- E2E verifica cartografia real, não o mapa simulado.
- APOI 2027 de produção permanece vazio enquanto não houver registos elegíveis/publicados.
- SOS abre a função telefónica Android para 112 e apresenta a localização disponível.
- Smartwatch permanece limitado a notificações Android/plataformas externas, sem alegar integração nativa.
- Validação física em Android real continua pendente.

## Estado de validação

- Visual reference adicionada em `docs/visual-reference/referencia.jpeg`.
- Preparação e caminhada/mapa já têm auditoria visual baseada em screenshots reais do CI.
- O problema do mapa offline foi identificado e corrigido; a reconstrução sem rede agora mostra cartografia.
- Persistência e E2E do fluxo principal passaram no último build.
- Offline tem evidência positiva no cenário de CI, mas a validação em condições reais continua necessária.
- **Teste físico ainda não realizado.**

## Bloqueadores / próximos trabalhos conhecidos

1. Completar a auditoria visual das 10 superfícies com screenshots representativos.
2. Melhorar as principais diferenças visuais encontradas, sem inventar funcionalidades ou dados.
3. Revalidar regressões de persistência e caminhada após as próximas mudanças de UI.
4. Manter/regredir o teste offline como critério funcional.
5. Fazer validação física em Android real, incluindo GPS, background e comportamento/bateria.
6. Só então avaliar a passagem para release candidate.

## Regra de manutenção deste ficheiro

Este ficheiro é operacional e deve ser atualizado pelo agente quando ocorrerem alterações relevantes. O estado documentado nunca substitui a verificação do GitHub. Se houver divergência, o estado real do GitHub prevalece e este ficheiro deve ser corrigido.

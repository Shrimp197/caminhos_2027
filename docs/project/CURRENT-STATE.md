# Caminhos 2027 — Current State

> Estado inicial desta documentação de continuidade, sincronizado com o GitHub em 2026-09-30.

## Identidade atual

- Repository: `Shrimp197/caminhos_2027`
- Development branch: `v1-route-import`
- Base branch: `main`
- Pull Request: #8
- PR title: V1 route import — official geometry provenance and runtime policy
- PR state: open, draft
- Último HEAD funcional verificado: `474856a581652a5a4b0a31d96fedf2d20032081e`
- Base SHA do PR: `c5222c1fb3052ea22ccf4b25701257a84f55f704`
- Última implementação funcional verificada: `474856a581652a5a4b0a31d96fedf2d20032081e` — teste E2E estabilizado para localizar a opção de orientação por texto ou descrição.
- O HEAD exato deve ser sempre confirmado no GitHub antes de trabalhar.

## CI da implementação validada

Na implementação funcional verificada até ao commit `36cb0b8af60f7ba267adb1f48212ec3bd8a2c81b`:

- V1 Route Import Validation: sucesso (run 1590).
- V1 Route Source Provenance: sucesso (run 2155).
- V1 Route Import Build: sucesso (run 1615).
- O build passou por testes JVM, build/assinatura do APK, emulador Android e E2E.

A evidência visual do build inclui `preparacao.png` e `navegacao.png`. A captura `navegacao.png` mostra cartografia renderizada depois da reconstrução da caminhada sem rede. Uma falha anterior no mesmo ramo foi causada por um seletor E2E dependente de texto; o seletor foi estabilizado e o build 1615 passou o fluxo completo.

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
- Persistência e E2E do fluxo principal passaram no build 1615.
- Offline tem evidência positiva no cenário de CI, mas a validação em condições reais continua necessária.
- **Teste físico ainda não realizado.**

## Bloqueadores / próximos trabalhos conhecidos

1. Completar a auditoria visual das 10 superfícies com screenshots representativos.
2. Melhorar as principais diferenças visuais encontradas, sem inventar funcionalidades ou dados.
3. Revalidar regressões de persistência e caminhada após as próximas mudanças de UI.
4. Manter/regredir o teste offline como critério funcional.
5. Fazer validação física em Android real, incluindo GPS, background e comportamento/bateria.
6. Só então avaliar a passagem para release candidate.

> Nota: este ficheiro não é usado para decidir qual é o HEAD atual; o agente deve sempre verificar o GitHub. Este documento preserva o último estado funcional confirmado.

## Regra de manutenção deste ficheiro

Este ficheiro é operacional e deve ser atualizado pelo agente quando ocorrerem alterações relevantes, após verificação do HEAD e CI correspondentes. O estado documentado nunca substitui a verificação do GitHub. Se houver divergência, o estado real do GitHub prevalece e este ficheiro deve ser corrigido.

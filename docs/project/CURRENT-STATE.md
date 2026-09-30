# Caminhos 2027 — Current State

> Estado inicial desta documentação de continuidade, sincronizado com o GitHub em 2026-09-30.

## Identidade atual

- Repository: `Shrimp197/caminhos_2027`
- Development branch: `v1-route-import`
- Base branch: `main`
- Pull Request: #8
- PR title: V1 route import — official geometry provenance and runtime policy
- PR state: open, draft
- Current HEAD (verificado): `8c4d6d8d2bddd345240fc68aa33238960d3f03d2`
- Base SHA do PR: `c5222c1fb3052ea22ccf4b25701257a84f55f704`
- Último commit verificado: Add files via upload — Visual reference for V1 — 10 screens.
- Última alteração verificada: `docs/visual-reference/referencia.jpeg`

## CI no HEAD atual

No HEAD `8c4d6d8d2bddd345240fc68aa33238960d3f03d2`:

- V1 Route Import Validation: concluído com sucesso (run 1568).
- V1 Route Source Provenance: concluído com sucesso (run 2133).
- V1 Route Import Build: concluído com falha (run 1581).

A falha do Build é um bloqueador técnico corrente até ser investigada e resolvida; não assumir a causa sem inspeção dos jobs/logs.

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
- Auditoria visual das 10 telas ainda não concluída.
- Persistência, offline, GPS real e restante aceitação devem ser reconfirmados com evidência atual antes da conclusão da V1.
- **Teste físico ainda não realizado.**

## Bloqueadores / próximos trabalhos conhecidos

1. Investigar e corrigir a falha do workflow V1 Route Import Build run 1581 no HEAD atual.
2. Fazer a auditoria visual real da referência de 10 telas contra a implementação atual.
3. Revalidar persistência e regressões do fluxo de caminhada após as últimas alterações.
4. Revalidar offline com evidência adequada.
5. Fazer validação física em Android real, incluindo GPS, background e comportamento/bateria.
6. Só então avaliar a passagem para release candidate.

## Regra de manutenção deste ficheiro

Este ficheiro é operacional e deve ser atualizado pelo agente quando ocorrerem alterações relevantes. O estado documentado nunca substitui a verificação do GitHub. Se houver divergência, o estado real do GitHub prevalece e este ficheiro deve ser corrigido.

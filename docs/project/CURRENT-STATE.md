# Caminhos 2027 — Current State

> Estado operacional atualizado após a auditoria visual e validação CI de 2026-10-07.

## Identidade atual

- Repository: `Shrimp197/caminhos_2027`
- Development branch: `v1-route-import`
- Base branch: `main`
- Pull Request: #8
- PR title: V1 route import — official geometry provenance and runtime policy
- PR state: open, draft
- Último HEAD funcional verificado: `817f50f374e4d3043e0417413a5ab4484a169880`
- Base SHA do PR: `c5222c1fb3052ea22ccf4b25701257a84f55f704`
- Última implementação funcional verificada: `817f50f374e4d3043e0417413a5ab4484a169880` — caminhada abre compacta e o traçado usa o azul da referência.

## CI da implementação validada

No HEAD `817f50f374e4d3043e0417413a5ab4484a169880`:

- V1 Route Import Validation: sucesso (run 1672).
- V1 Route Source Provenance: sucesso (run 2237).
- V1 Route Import Build: sucesso (run 1685).
- O build passou por testes JVM, compilação, APK debug assinado, emulador Android, E2E com process-death, validação visual e APK release.

Artefactos validados:
- APK debug validado;
- APK release unsigned validado;
- conjunto de 11 screenshots visuais.

## Estado do produto

- Cartografia nativa MapLibre no mapa de caminhada.
- Traçado oficial, início, destino, posição do peregrino e próximo APOI.
- OpenFreeMap/MapLibre com atribuição visível.
- Mapa offline por região com estado de disponibilidade após conclusão.
- Estilo cartográfico Liberty integrado localmente para permitir reconstrução offline sem depender da rede para carregar o estilo.
- Bottom sheet redimensionável por gesto.
- A caminhada abre por defeito em modo compacto/mapa-primeiro.
- Orientação por bearing configurável.
- E2E verifica cartografia real e não um mapa simulado.
- APOI 2027 de produção continua sem registos elegíveis/publicados.
- SOS usa a função telefónica Android real para 112 e apresenta apenas localização disponível.
- Smartwatch permanece limitado a uma pré-visualização/notificações externas, sem alegar integração nativa.
- Modo Peregrino está ligado ao estado real da caminhada.

## Estado de validação

- Auditoria visual das 10 superfícies concluída com screenshots reais.
- Preparação alinhada estruturalmente com a referência.
- Caminhada/mapa alinhada com a referência principal: mapa visível, estado compacto por defeito e traçado azul.
- Persistência e E2E passam no build 1685.
- Offline tem evidência positiva no Android virtual; validação em condições reais continua necessária.
- APKs validáveis foram produzidos pelo CI.
- **Teste físico ainda não realizado.**

## Pendentes / próximos trabalhos

1. Melhorias visuais finas do bottom sheet e tipografia/espaçamentos.
2. Melhorar Progresso quando os dados reais de etapa justificarem maior riqueza visual.
3. Rever SOS/Diário exclusivamente dentro das capacidades reais suportadas.
4. Validação física em Android real: GPS, background, bateria, navegação externa e uso prolongado.
5. Reavaliar o conjunto final para release candidate apenas depois da validação física e restantes critérios de aceitação.

## Regra de manutenção

Este ficheiro é operacional. O agente deve atualizá-lo após alterações relevantes e nunca o usar como substituto da verificação do GitHub. Se houver divergência, o estado real do GitHub prevalece.

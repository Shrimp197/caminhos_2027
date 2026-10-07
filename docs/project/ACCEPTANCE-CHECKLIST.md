# Caminhos 2027 — Acceptance Checklist

## Regra

Uma caixa só deve ser marcada como concluída quando existir evidência suficiente. “Compila” ou “parece funcionar” não substitui validação adequada.

## Produto e funcionalidades

- [ ] Preparação usa estado real e seleção de percurso determinística.
- [ ] Início/pausa/retoma/paragem preservam o estado oficial.
- [ ] Caminhada ativa apresenta mapa, progresso e contexto coerentes.
- [ ] Tempo decorrido permanece correto e persistente.
- [ ] Próximos 10 km usam distância ao longo da rota.
- [ ] APOI respeita elegibilidade dos dados e não inventa conteúdo.
- [ ] Diário existe como superfície funcional coerente com o estado do produto.
- [ ] SOS usa apenas ações Android realmente executadas.
- [ ] Smartwatch não promete integração inexistente.
- [ ] Pilgrim Mode permanece coerente com o estado real.

## GPS, rota e navegação

- [ ] Geometria oficial local é usada.
- [ ] Posição é projetada sobre a rota de forma consistente.
- [ ] Estados de GPS são convertidos em linguagem compreensível na UI.
- [ ] Antes de iniciar, navegação pode orientar para o início planeado sem alterar progresso.
- [ ] Depois de iniciar e fora da rota, orientação usa `lastKnownOnRoute`.
- [ ] Progresso oficial nunca é inventado nem salta por causa de uma posição off-route.
- [ ] Produção não contém simulador GPS.
- [ ] Navegação externa tenta o destino suportado e expõe falhas.

## Cartografia e offline

- [ ] Cartografia real está presente no fluxo de caminhada.
- [ ] Atribuição cartográfica está visível quando exigida.
- [ ] User-Agent das requisições identifica a aplicação/projeto.
- [ ] Download offline tem estado verificável.
- [ ] Disponibilidade offline só é indicada depois de conclusão real.
- [ ] Não há afirmação de offline totalmente validado sem evidência adequada.

## Persistência

- [ ] Estado sobrevive à recriação da Activity.
- [ ] Estado sobrevive a fechar/reabrir.
- [ ] Estado sobrevive a process death/recreation.
- [ ] Pause/resume mantém estado e tempo coerentes.
- [ ] Seleção de percurso não é substituída por defaults indevidos.

## QA, testes e CI

- [ ] Testes JVM relevantes passam.
- [ ] Instrumented/E2E relevantes passam.
- [ ] Build debug/release relevante passa.
- [ ] APK está assinado/instalável conforme o fluxo de CI.
- [ ] Artefactos esperados são publicados.
- [ ] Falhas de CI são investigadas pela causa e não ocultadas.
- [ ] Testes não são enfraquecidos para fazer o pipeline passar.

## UX/UI e validação visual

- [ ] Interface permanece ligada a estados reais.
- [ ] Alterações de UI têm screenshot/inspeção quando aplicável.
- [ ] A referência `docs/visual-reference/referencia.jpeg` foi usada como referência visual quando relevante.
- [ ] Divergências visuais relevantes foram corrigidas ou documentadas.
- [ ] Não se declara conformidade visual apenas por testes automatizados.

## Dados

- [ ] Dados oficiais e respetiva proveniência estão preservados.
- [ ] Dataset de produção não usa dados QA.
- [ ] Histórico está distinguido de informação atual.
- [ ] Ausência de dados elegíveis é comunicada honestamente.

## Release

- [ ] Build final verificável.
- [ ] E2E final verificável.
- [ ] APK instalável disponível.
- [ ] Evidência de persistência.
- [ ] Evidência de offline.
- [ ] Evidência de validação visual.
- [ ] Evidência de GPS/navegação adequada.
- [ ] **Teste físico ainda não realizado** permanece explicitamente indicado até o utilizador executar o teste em Android real.
- [ ] Só depois da evidência final a V1 pode ser considerada concluída.

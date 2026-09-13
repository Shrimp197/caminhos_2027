# UI REFRESH V1 — preparação

## Estado

A preparação V1 passou para uma composição Android nativa em Compose, mantendo a navegação e o estado de domínio existentes.

## Estrutura do produto

A superfície inicial apresenta o percurso escolhido, a etapa e o intervalo de início/fim, além de acessos secundários para áudio, orientação, pausas, APOIs e notas.

Cada acesso secundário regressa à preparação preservando as escolhas em memória. O plano continua a ser persistido pelo fluxo de preparação existente e só transita de `PLANNED` para `ACTIVE` perante início explícito e GPS válido.

## Percursos

- Caminho do Centenário — produção;
- SR — QA debug;
- HF — QA debug.

SR/HF permanecem isolados do caminho de produção no catálogo Android.

## Conteúdo visual

O cabeçalho do Centenário usa o recurso Android `caminho_centenario_hero.jpg`. Percursos QA não reutilizam esse conteúdo e recebem tratamento visual explicitamente marcado como teste.

## Regra de engenharia

Este refresh é apresentação/composição. Regras de GPS, progressão, APOI, publicação, decisão e persistência continuam nos respectivos serviços/modelos; não são duplicadas na UI.

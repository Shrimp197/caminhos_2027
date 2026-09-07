# Caminhos de Fátima V1 — Caminho do Centenário

Aplicação Android em evolução para peregrinos independentes dos Caminhos de Fátima.

A V1 mantém o princípio **“Orientar sem retirar autonomia”**: o aplicativo informa percurso, posição, progresso, APOIs, incerteza e opções disponíveis, sem impor um itinerário rígido.

## Estado actual

O desenvolvimento activo está na branch `v1-route-import`.

- Percurso oficial do Centenário integrado a partir do GPX oficial da Associação Caminhos de Fátima.
- Distância publicada de referência: **211,87 km**.
- Distância técnica da geometria é tratada separadamente.
- Etapas oficiais são referência de planeamento; não limitam a caminhada real.
- GPS bruto e posição projectada no percurso são entidades distintas.
- Sessão de caminhada persistente com estados `PLANNED`, `ACTIVE`, `COMPLETED` e `CANCELLED`.
- Perda de sinal mantém a última posição fiável; observações implausíveis não alteram progresso/posição fiável.
- APOIs seguem o modelo de publicação/qualificação/confiança; o catálogo de produção permanece vazio até existir evidência 2027 qualificada.
- SR/HF existem apenas no build debug para validação determinística da experiência.
- Preparação V1 apresenta subseções de percurso, etapa/início/fim, áudio, orientação, pausas, APOIs e notas.

## Validação

A branch tem CI de Android e de proveniência da fonte oficial. A validação física em dispositivo Android continua necessária para o comportamento de GPS real.

Consulte `PROJECT-STATE.md` para o estado técnico actual e `docs/QA-SR-HF-SCENARIO.md` para o cenário executável de SR/HF.

# Caminhos 2027 — Decisions

Este ficheiro preserva decisões de produto e engenharia que não devem ser reabertas sem nova evidência ou decisão explícita.

## D-001 — Desenvolvimento isolado de main

O desenvolvimento da V1 ocorre em `v1-route-import`. `main` não deve ser alterada diretamente.

## D-002 — Três posições distintas

A posição física do dispositivo, a posição projetada na geometria da rota e o progresso oficial da caminhada são estados distintos. Nenhuma camada deve os confundir.

## D-003 — Comportamento GPS antes do início

Antes de iniciar a caminhada, estar fora da rota é permitido. A orientação de deslocação pode apontar para o início planeado sem alterar o progresso oficial.

## D-004 — Comportamento GPS após saída da rota

Depois de iniciada a caminhada, uma saída da rota deve preservar o progresso oficial e orientar para `lastKnownOnRoute`. Não regressar automaticamente ao início nem inventar progresso.

## D-005 — Produção vs QA

Dados e simulação de QA existem apenas em modo debug. A produção nunca deve recorrer a dados sintéticos para preencher lacunas.

## D-006 — APOI 2027

O dataset de produção APOI 2027 só pode apresentar registos qualificados/publicados. Quando não existirem dados elegíveis, a interface deve refletir essa ausência.

## D-007 — Distância ao longo da rota

Qualquer funcionalidade do tipo “Próximos 10 km” deve usar distância ao longo da geometria da rota e não distância em linha reta.

## D-008 — Navegação externa

A aplicação pode tentar abrir Google Maps e usar `geo:` como fallback. A aplicação deve comunicar a falha quando a navegação externa não for executada.

## D-009 — Cartografia

A cartografia de produção usa o pipeline real definido pelo projeto. Um mapa simulado não é equivalente a cartografia real para fins de aceitação.

## D-010 — Smartwatch

A superfície de smartwatch é visual/representacional enquanto não existir integração nativa comprovada com o dispositivo/plataforma alvo.

## D-011 — Validação física

GPS real, background, bateria e condições de utilização reais continuam a exigir teste físico em Android. Enquanto não houver esse teste, o estado deve permanecer explicitamente pendente.

## D-012 — Tempo decorrido

A superfície de caminhada deve preservar e apresentar o tempo decorrido quando o estado de caminhada o disponibiliza. Alterações futuras não devem remover esta informação sem decisão explícita.

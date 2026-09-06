# QA — SR / HF V1 PRIMARY WALKING SCENARIO

## Objetivo

Validar a experiência V1 primordial nos ambientes sintéticos `SR` e `HF`, sem introduzir dados reais nem alterar a política de produção.

O cenário testa a cadeia única de experiência:

`Preparar → Guardar → Iniciar → GPS → progresso → APOI → detalhe → voltar → decisão → perda de sinal → recuperação → desvio → recuperação → persistência/resume`

## Pré-condições

- Usar o APK debug gerado a partir da branch `v1-route-import`.
- Selecionar `SR` ou `HF` na preparação.
- Confirmar visualmente que o ambiente está identificado como teste.
- Usar um início planeado diferente de zero para verificar que a simulação começa na zona correta do percurso.

## Cenário A — preparação e início

1. Selecionar o percurso de teste.
2. Definir um início planeado não-zero e um destino posterior.
3. Guardar o plano.
4. Confirmar que guardar não inicia a caminhada.
5. Pedir início e aguardar a primeira posição GPS simulada.
6. Confirmar que só um GPS válido/on-route transforma a sessão em `ACTIVE`.

**Resultado esperado:** o plano permanece `PLANNED` até ao início explícito; a posição real da caminhada nasce do primeiro GPS válido, não do ponto de planeamento.

## Cenário B — caminhada e seis superfícies

1. Confirmar mapa local/esquemático e marcador de posição.
2. Confirmar progresso e distância restante para o destino planeado.
3. Confirmar o contexto do próximo APOI no bottom sheet.
4. Avançar no percurso.
5. Abrir a lista de APOI a partir do contexto da caminhada.
6. Confirmar ordenação pela distância no percurso, não por distância em linha reta.
7. Abrir um APOI multi-serviço.
8. Voltar à lista e confirmar que o filtro/seleção anterior é preservado pela mesma fonte de estado.

**Resultado esperado:** as seis superfícies mostram a mesma caminhada e o mesmo contexto, sem cálculos paralelos na UI.

## Cenário C — informação e incerteza

Exercitar pelo menos um APOI com:

- múltiplos serviços;
- custo não conhecido;
- reserva requerida ou recomendada;
- disponibilidade a confirmar;
- `PUBLISHED_WITH_WARNING`;
- informação deliberadamente incompleta.

**Resultado esperado:** o detalhe apresenta apenas informação suportada, assinala incerteza quando aplicável e nunca converte desconhecido em gratuito/confirmado.

## Cenário D — decisão

1. Abrir `OPÇÕES` durante uma caminhada com destino planeado.
2. Comparar `Parar agora` com `Continuar até ao destino`.
3. Confirmar distância restante e contexto de apoios.
4. Confirmar que a interface não escolhe automaticamente uma opção.

**Resultado esperado:** suporte à decisão, não recomendação automática.

## Cenário E — GPS

1. Com caminhada ativa, usar `Perder GPS`.
2. Confirmar retenção da última posição fiável e ausência de salto visual.
3. Usar `Recuperar GPS`.
4. Usar `Simular desvio`.
5. Confirmar transição de estado segundo a avaliação partilhada e sem mover a posição por uma observação implausível.
6. Recolocar o percurso e confirmar recuperação de `ON_ROUTE` quando suportada pelo cenário.

**Resultado esperado:** perda de sinal não inventa movimento; observação impossível não altera posição/progresso; desvio é contextual e recuperável.

## Cenário F — persistência

1. Com caminhada ativa, provocar uma recriação/saída da UI compatível com o mecanismo de teste.
2. Reabrir a aplicação.
3. Confirmar restauração da caminhada ativa e do contexto essencial.

**Resultado esperado:** o runtime persistente é a fonte de recuperação; a Activity não inventa uma nova sessão.

## Critérios de aprovação

O cenário só é considerado aprovado quando:

- não há bloqueio ou crash nos passos A–F;
- `PLANNED` e `ACTIVE` permanecem semanticamente distintos;
- posição/progresso/next APOI não divergem entre superfícies;
- o catálogo QA não apresenta itens de outra rota;
- warnings e desconhecidos são apresentados sem garantias falsas;
- perda de sinal, recuperação e desvio percorrem o pipeline comum;
- a sessão pode ser restaurada sem perda indevida do estado.

## Registo de resultados

Para cada execução, registar apenas:

- percurso: `SR` ou `HF`;
- versão/commit do APK;
- passo que falhou;
- comportamento observado;
- comportamento esperado;
- evidência reproduzível (captura/ecrã/log, quando disponível);
- severidade: bloqueador / alto / médio / baixo.

Não alterar thresholds, arquitetura ou dataset de produção apenas por uma observação isolada. Primeiro reproduzir e caracterizar o defeito.

# V1 — Six Primary Walking Surfaces

Este documento transforma o vertical slice principal da V1 num contrato de aceitação verificável para os ambientes SR e HF.

A referência funcional é `V1-FOUNDATION.md`. A composição visual fornecida para o projeto é referência de experiência e hierarquia, não uma especificação para copiar literalmente elementos de funcionalidades futuras.

## 1. Preparação da caminhada

Objetivo: permitir selecionar o caminho, definir livremente o início e o destino e rever o plano antes de começar.

Obrigatório:

- mostrar claramente o caminho selecionado;
- permitir início e destino no percurso;
- manter as etapas oficiais como referência, nunca como obrigação;
- guardar o plano como `PLANNED`;
- não iniciar tracking ao guardar;
- exigir uma transição explícita de início.

Aceitação SR/HF: o utilizador consegue preparar e guardar um intervalo do percurso e regressar ao plano sem criar uma caminhada ativa.

## 2. Caminhada ativa — mapa

Objetivo: responder imediatamente a "Onde estou?".

Obrigatório:

- mapa como elemento central da experiência ativa;
- geometria real do percurso disponível localmente;
- posição derivada do pipeline GPS e da projeção no percurso;
- distância percorrida e distância restante;
- identificação do próximo APOI quando existir;
- estado GPS visível de forma compreensível;
- manutenção do contexto offline sem apagar progresso ou posição fiável.

A UI não calcula uma segunda posição nem um segundo progresso; consome `WalkingState`.

## 3. Bottom sheet contextual

Objetivo: responder rapidamente a "O que tenho à minha frente?" sem retirar o mapa do contexto.

Obrigatório:

- estado compacto e expandido;
- resumo da caminhada;
- progresso atual;
- próximo APOI e distância pelo percurso;
- acesso direto à consulta de APOI;
- acesso ao suporte à decisão;
- retorno à caminhada sem perder estado.

A ausência de APOI deve ser explícita e não significar que o terreno não possui qualquer apoio.

## 4. Próximos 10 km

Objetivo: permitir uma leitura contextual dos apoios à frente.

Obrigatório:

- distância calculada pelo percurso, não em linha reta;
- apenas APOI elegíveis à frente;
- ordenação crescente por `routeKm`;
- limite contextual de 10 km;
- categorias e distância imediatamente legíveis;
- warnings visíveis quando aplicável;
- histórico, encerrado e não publicado não entram na descoberta atual.

A lista deve derivar do `ApoiBrowser` e de `WalkingState`; a UI não duplica o algoritmo de descoberta.

## 5. Apoios — lista / cartões

Objetivo: encontrar rapidamente um apoio relevante e compreender o seu contexto.

Obrigatório:

- pesquisa por nome;
- filtros por serviço;
- suporte a múltiplos serviços no mesmo APOI;
- cartões com nome, serviço, distância e estado relevante;
- custo distinguindo gratuito, contribuição opcional, pago e desconhecido;
- warning visível quando a informação tem ressalva;
- seleção abre o detalhe;
- voltar preserva consulta, filtro, resultados e caminhada.

A UI apresenta informação disponível sem inventar campos ausentes.

## 6. Progresso da caminhada

Objetivo: responder rapidamente a "O que acontece se continuar?" em termos de percurso.

Obrigatório:

- posição atual no percurso;
- distância efetivamente percorrida;
- distância restante até ao destino planeado;
- percentagem/representação de progresso;
- destino planeado separado da etapa oficial;
- etapa oficial mostrada apenas como referência quando disponível;
- progresso não pode ser alterado por uma observação GPS implausível.

## Vertical slice de aceitação

A sequência deve ser contínua:

```text
Selecionar caminho
  ↓
Preparar
  ↓
Rever
  ↓
Guardar (PLANNED)
  ↓
Iniciar explicitamente
  ↓
Obter posição válida
  ↓
WalkingState
  ↓
Mapa + progresso + próximo APOI
  ↓
Bottom sheet
  ↓
Próximos 10 km
  ↓
Lista / cartões
  ↓
Filtro / pesquisa
  ↓
Detalhe
  ↓
Voltar
  ↓
Decisão
  ↓
Prosseguir / regressar à caminhada
```

O contexto que deve sobreviver à navegação inclui, no mínimo, caminho, caminhada ativa, posição, progresso, consulta de APOI, filtro, resultados e seleção.

## SR — cenário técnico controlado

O SR deve permitir validar:

- todos os 8 serviços APOI;
- multi-serviço;
- gratuito, contribuição opcional, pago e desconhecido;
- reserva não necessária, recomendada, necessária e desconhecida;
- informação atual, recorrente, aguardando confirmação, histórica, expirada e encerrada;
- publicação normal, com warning, histórica, encerrada e em revisão;
- confiança e localização com diferentes níveis de certeza;
- perda e recuperação controladas de GPS;
- desvio e rejeição de saltos implausíveis.

## HF — cenário de validação humana

O HF deve usar os mesmos princípios de produto, mas a avaliação é feita com pessoas que não participaram no desenvolvimento.

Tarefas mínimas:

1. Encontrar um local para dormir nos próximos 10 km.
2. Encontrar água.
3. Encontrar um local para carregar o telemóvel.
4. Comparar parar agora com continuar.
5. Voltar à caminhada sem perder o contexto.

Registar:

- tempo para completar;
- número de toques;
- erros;
- hesitação;
- necessidade de explicação;
- compreensão.

## Critério de entrada em teste físico

O APK só deve ser apresentado para validação física desta V1 quando:

- o CI estiver verde;
- o vertical slice estiver coberto por testes de domínio/integração/estado;
- SR/HF tiverem dados de teste isolados e suficientes para exercitar as seis superfícies;
- nenhuma das seis superfícies depender de uma implementação paralela de estado ou cálculo.

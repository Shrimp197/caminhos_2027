# V1 — Six Primary Walking Surfaces

Este documento transforma o vertical slice principal da V1 num contrato de aceitação verificável para os ambientes SR e HF.

A referência funcional é `V1-FOUNDATION.md`. A composição visual fornecida pelo produto é também a referência visual de aceitação desta V1: deve orientar hierarquia, densidade, navegação, cartões, tipografia, cores e composição dos dez momentos apresentados. Não é necessário copiar pixels literalmente nem inventar funcionalidades ausentes na referência.

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

### Navegação até ao percurso

O botão de início nunca deve obrigar o utilizador a estar fisicamente sobre o traçado para iniciar a orientação.

- se a caminhada ainda não começou (`PLANNED`), a orientação deve usar a posição GPS atual do utilizador como origem e encaminhá-lo para o início planeado do intervalo;
- se já existe uma caminhada ativa e o utilizador se encontra fora do traçado, a orientação deve usar a posição GPS atual como origem e encaminhá-lo para o último ponto fiável conhecido no percurso;
- a aplicação não deve substituir a posição atual por um ponto artificial do percurso;
- a chegada ao alvo de reentrada não altera por si só o progresso: o progresso continua a ser derivado do pipeline GPS comum quando a posição passa a ser aceite no percurso.

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

## Referência visual — dez momentos

A imagem de referência fornecida pelo produto representa dez momentos da experiência e é usada como referência de aceitação visual:

1. ecrã inicial / preparação;
2. ecrã de caminhada / mapa;
3. bottom sheet inteligente;
4. próximos 10 km;
5. apoios — lista / cartões;
6. progresso da caminhada;
7. diário do peregrino;
8. SOS / acesso rápido;
9. smartwatch / notificações e orientação;
10. modo peregrino / maior contraste.

A implementação deve manter a mesma linguagem de produto entre estes momentos: mapa como contexto da caminhada, cartões claros, ações primárias evidentes, verde para ações positivas, azul para informação/navegação, cantos arredondados, densidade controlada e navegação inferior coerente quando a superfície pertence ao fluxo principal.

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
- desvio e rejeição de saltos implausíveis;
- orientação para o início quando o utilizador ainda não iniciou a caminhada;
- orientação de reentrada para o último ponto fiável do percurso quando uma caminhada ativa fica fora do traçado.

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

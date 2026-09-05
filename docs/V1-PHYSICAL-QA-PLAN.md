# V1 — Plano de validação física

## Objetivo

Validar no dispositivo Android real a experiência V1 já construída, sem introduzir alterações funcionais não observadas.

A validação física começa a partir do baseline funcional `943c3b70ba8ab3149ae8ffc5ec94d63c78a75275`.

## Regra

Este documento é um protocolo de validação, não um mecanismo de simulação.

SR e HF usam GPX de teste e servem para validar o comportamento da aplicação. O Caminho do Centenário é o percurso de produção e não deve receber dados fictícios.

## 1. Preparação

- Instalar a APK debug validada no Android físico.
- Confirmar que a aplicação inicia sem crash.
- Confirmar pedido e estado das permissões de localização.
- Confirmar que a seleção apresenta explicitamente:
  - Caminho do Centenário — produção
  - SR — TEST/FICTITIOUS
  - HF — TEST/FICTITIOUS
- Confirmar que a escolha de SR/HF não altera o percurso de produção nem o catálogo de APOI de produção.

## 2. SR — fluxo completo controlado

### 2.1 Preparação

- Selecionar SR.
- Criar caminhada `PLANNED`.
- Escolher início e destino dentro dos limites do GPX.
- Rever plano.
- Guardar plano.
- Confirmar que guardar não inicia a caminhada.

### 2.2 Início

- Iniciar a caminhada guardada.
- Confirmar estado `ACTIVE`.
- Confirmar publicação da posição inicial.
- Confirmar estado GPS inicial `ACQUIRING`.

### 2.3 Simulação GPS

- Avançar alguns pontos do GPX.
- Confirmar atualização da posição no caminho.
- Confirmar atualização de km percorridos.
- Confirmar diminuição dos km restantes.
- Confirmar atualização do próximo APOI quando a posição avança.
- Confirmar que a lista APOI usa a mesma posição publicada no estado da caminhada.

### 2.4 Perda/recuperação

- Simular ausência de sinal.
- Confirmar preservação da última posição fiável.
- Confirmar estado `NO_SIGNAL` após o limiar configurado.
- Retomar pontos GPS válidos.
- Confirmar recuperação sem salto artificial.

### 2.5 Desvio

- Alimentar pontos sucessivos fora do caminho.
- Confirmar `POSSIBLE_DEVIATION` após a condição configurada.
- Confirmar `PROBABLE_DEVIATION` após a condição configurada.
- Regressar ao traçado.
- Confirmar recuperação para `ON_ROUTE`.
- Confirmar que uma única leitura anómala não produz salto de progresso.

## 3. HF — validação de experiência humana

HF deve ser executado por pessoas que não participaram no desenvolvimento.

As tarefas devem ser dadas como necessidades reais, sem explicar previamente onde tocar:

1. Encontrar um local de água nos próximos quilómetros.
2. Encontrar um local para comer.
3. Encontrar uma opção de pernoita.
4. Abrir um APOI e descobrir custo/reserva/serviços disponíveis.
5. Perceber se a informação tem ressalvas.
6. Voltar à caminhada e perceber onde está.
7. Consultar as opções para parar agora ou continuar.

Registar:

- tempo até encontrar a informação;
- número de toques;
- hesitações;
- interpretações erradas;
- informação que a pessoa procurou e não encontrou;
- linguagem que causou dúvida;
- qualquer situação em que a pessoa sentiu que a aplicação estava a decidir por ela.

Não corrigir o utilizador durante a primeira execução de cada tarefa. As dificuldades observadas devem alimentar correções de produto/UX posteriores.

## 4. Centenário — GPS físico

Só executar depois de SR/HF passarem o fluxo básico.

### Fora do percurso

- Iniciar com o utilizador afastado do percurso.
- Confirmar ausência de crash.
- Confirmar estado GPS coerente.
- Confirmar que a aplicação não inventa uma posição no percurso.

### No percurso

- Entrar no percurso real.
- Confirmar projeção da posição para o traçado validado disponível no APK.
- Confirmar atualização do progresso.
- Confirmar cálculo do próximo APOI.

### Condições degradadas

- Perda temporária de GPS.
- GPS fraco.
- Movimento parado.
- Mudança de direção.
- Leituras pontuais anómalas.
- Recuperação do sinal.

## 5. Persistência e recriação

- Com caminhada `ACTIVE`, fechar/reabrir a Activity.
- Recriar processo quando possível.
- Confirmar restauração da caminhada ativa.
- Confirmar preservação da posição/checkpoint.
- Confirmar preservação do percurso selecionado.
- Confirmar que o sistema não troca silenciosamente para o Centenário, SR ou HF.

## 6. Critérios de falha imediata

Considerar falha crítica qualquer ocorrência de:

- crash durante preparação ou caminhada;
- troca silenciosa de percurso;
- posição GPS impossível apresentada como válida;
- salto artificial de progresso;
- APOI de teste apresentado como produção;
- APOI fechado/histórico apresentado como garantia atual;
- perda de caminhada ativa após recriação quando o checkpoint existe;
- aplicação a impor uma decisão ao peregrino sem base explícita.

## 7. Evidência

Para cada execução registar:

- dispositivo e versão Android;
- versão APK/commit;
- cenário (SR/HF/Centenário);
- passos executados;
- resultado esperado;
- resultado observado;
- evidência (captura/log/descrição);
- severidade;
- reproduzível: sim/não.

## 8. Regra de correção

Só alterar código depois de existir um comportamento observado e reproduzível ou uma violação comprovada de requisito.

Uma correção deve ser pequena, específica e acompanhada de teste/regressão quando tecnicamente possível.

Não reabrir arquitetura nem alterar múltiplas camadas para resolver um defeito localizado.

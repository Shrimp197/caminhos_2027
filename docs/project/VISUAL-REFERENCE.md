# Caminhos 2027 — Auditoria Visual da V1

## Referência

Imagem de referência: `docs/visual-reference/referencia.jpeg`.

A referência contém 10 superfícies e define a direção visual da V1. Não autoriza inventar dados, serviços, mapas, instruções ou integrações que o produto não suporte.

## Evidência usada

A auditoria foi concluída com screenshots reais produzidos pelo Android virtual no workflow `V1 Route Import Build`. A execução que fechou a auditoria foi o build 1685, no commit `817f50f374e4d3043e0417413a5ab4484a169880`, com 11 imagens de validação: preparação, 4 estados de caminhada/mapa e as restantes superfícies.

## As 10 superfícies

1. Preparação
2. Caminhada / mapa
3. Bottom sheet inteligente
4. Próximos 10 km
5. Apoios
6. Progresso
7. Diário
8. SOS
9. Smartwatch
10. Modo Peregrino

## Auditoria realizada

### 1 — Preparação

A estrutura principal está alinhada com a referência:
- cabeçalho;
- título “Prepare a sua caminhada”;
- cartão do Caminho do Centenário;
- seis opções de preparação;
- ação principal “INICIAR CAMINHADA”;
- navegação inferior.

A imagem de capa real já está presente e aproxima-se claramente da composição de referência. Continuam existindo diferenças de tipografia, espaçamento e alguns ícones, sem impacto funcional.

### 2 — Caminhada / mapa

A experiência atual apresenta:
- mapa cartográfico real;
- traçado da rota;
- quilómetros percorridos/restantes;
- estado GPS;
- tempo decorrido;
- bottom sheet redimensionável;
- próximo APOI;
- estado offline.

Melhorias validadas:
- o fundo cartográfico continua visível após reconstrução sem rede;
- o mapa abre por defeito em modo compacto, deixando mais contexto cartográfico;
- o traçado principal usa o azul da referência;
- o painel expandido continua disponível por gesto.

Diferenças restantes:
- a referência usa uma composição mais limpa do topo e menos informação de QA;
- o bottom sheet expandido é mais denso do que a referência;
- os controlos QA aparecem apenas no ambiente de teste e não pertencem à experiência de produção.

### 3 — Bottom sheet inteligente

A interação de expandir/recolher está presente e mantém o mapa como contexto. O estado compacto aproxima-se da referência principal de caminhada; o estado expandido é uma interpretação funcional da referência.

A diferença principal é densidade: o produto mostra ações e estado técnico adicionais porque estes são reais e úteis para a V1.

### 4 — Próximos 10 km

A superfície está implementada e validada. Os resultados são ordenados por distância ao longo da rota e usam iconografia por categoria.

A composição é mais simples do que a referência. Não são criados cartões, custos ou estados não suportados pelos dados.

### 5 — Apoios

A superfície está implementada com pesquisa, filtros, distância, categoria e detalhe. A iconografia foi alinhada com as categorias da referência.

A referência apresenta cartões mais ricos e alguns estados de disponibilidade. Esses estados só podem ser mostrados quando suportados pelos dados de produção.

### 6 — Progresso

A superfície existe e está ligada ao estado real da caminhada, incluindo percurso, progresso e próximo APOI.

A referência tem maior riqueza gráfica e informação de etapa. A aplicação já suporta o conceito de etapa quando os dados do percurso o disponibilizam, mas o cenário de QA usado para as screenshots não fornece todo o conteúdo visual da referência.

### 7 — Diário

A superfície está implementada com criação de nota, associação à caminhada e fotografia.

A screenshot de validação apresenta o estado vazio, por honestidade dos dados do cenário. A referência mostra entradas preenchidas; não serão fabricadas entradas apenas para reproduzir a imagem.

### 8 — SOS

A superfície está implementada com chamada real para 112, localização disponível e contexto de apoios de emergência.

A composição é mais simples do que a referência. A diferença é aceitável enquanto as ações adicionais da referência não tiverem execução Android real correspondente.

### 9 — Smartwatch

A superfície apresenta uma representação visual de smartwatch e notificações/orientação.

A aplicação declara explicitamente que esta é uma pré-visualização e não uma integração nativa com Huawei/Amazfit. Esta limitação é intencional e correta.

### 10 — Modo Peregrino

A superfície está validada e mostra:
- alto contraste;
- quilómetros percorridos/restantes;
- progresso;
- próximo APOI;
- acesso a APOI e SOS;
- saída do modo.

A composição aproxima-se da referência, mantendo uma implementação real baseada no estado da caminhada.

## Conclusão

A auditoria visual da V1 foi concluída nas 10 superfícies.

Não existe conformidade pixel-perfect com a imagem de referência e não é esse o critério de aceitação. O critério é:
- manter a intenção visual e a hierarquia principais;
- preservar comportamento real;
- não inventar dados;
- não apresentar capacidades inexistentes;
- corrigir diferenças que prejudiquem a utilização.

### Prioridades visuais futuras

1. reduzir a densidade visual do bottom sheet expandido;
2. aproximar tipografia/espaçamento da referência;
3. enriquecer visualmente Progresso quando os dados de etapa reais estiverem disponíveis;
4. rever composição de SOS e Diário sem inventar ações/conteúdo.

## Regra de validação

A auditoria deve sempre separar:
- fidelidade visual;
- comportamento funcional;
- dados reais e elegíveis;
- validação física em Android.

Não declarar a V1 concluída apenas porque o CI passa.

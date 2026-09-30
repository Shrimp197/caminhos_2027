# Caminhos 2027 — Auditoria Visual da V1

## Referência

Imagem de referência: `docs/visual-reference/referencia.jpeg`.

A referência contém 10 superfícies e define a direção visual da V1. Não autoriza inventar dados, serviços, mapas, instruções ou integrações que o produto não suporte.

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

A implementação atual reproduz a estrutura principal da referência:
- cabeçalho;
- título “Prepare a sua caminhada”;
- cartão do Caminho do Centenário;
- seis opções de preparação;
- ação principal “INICIAR CAMINHADA”;
- navegação inferior.

Diferenças visuais observadas no screenshot validado:
- a imagem de capa atual não tem a mesma aparência fotográfica da referência;
- cabeçalho/marca é uma representação simplificada;
- dimensões, espaçamentos e tipografia ainda não são uma reprodução exata.

Estas diferenças são visuais; não devem ser resolvidas criando dados ou capacidades fictícias.

### 2 — Caminhada / mapa

A implementação atual apresenta:
- “Caminhada atual”;
- tempo decorrido;
- quilómetros percorridos e restantes;
- estado GPS;
- progresso;
- bottom sheet redimensionável;
- controlos do mapa;
- contexto do próximo APOI;
- indicação de mapa offline quando a região foi guardada.

**Problema funcional identificado:** no screenshot de validação recolhido após guardar a cartografia e recriar a caminhada sem rede, a rota aparece mas o fundo cartográfico não é renderizado. A aplicação diz “MAPA OFFLINE · DISPONÍVEL”, mas o utilizador vê essencialmente o traçado sobre um fundo vazio.

Este ponto é tratado como defeito de cartografia/offline e permanece aberto até ser explicado e validado.

Também se observa maior densidade visual que na referência, sobretudo no topo e no bottom sheet.

Os controlos “QA · percurso de teste” são específicos do ambiente de teste e não devem aparecer em produção.

### 3 — Bottom sheet inteligente

A implementação atual tem o conceito correto de manter o mapa como contexto e permitir expandir/recolher o painel. A fidelidade visual final ainda precisa de comparação de um estado representativo com a referência.

### 4 — Próximos 10 km

Existe uma superfície funcional específica e a distância é calculada ao longo do percurso. A referência visual usa cartões mais ricos e ícones/categorias mais evidentes. A comparação final depende de conteúdo elegível disponível para o cenário.

### 5 — Apoios

Existe uma superfície de consulta com filtros, pesquisa e detalhe. A referência usa cartões com categoria, distância e informação de custo/estado. Os dados de produção continuam sujeitos à regra de elegibilidade; não serão fabricados para completar o layout.

### 6 — Progresso

Existe superfície de resumo/progresso ligada ao estado da caminhada. A composição atual é mais simples que o painel de progresso da referência. A comparação final detalhada fica pendente de screenshot dedicado.

### 7 — Diário

Existe criação de notas, associação à caminhada e fotografias persistidas. A composição atual é mais orientada a formulário que a referência. Não é considerado conformidade visual final.

### 8 — SOS

Existe superfície dedicada com chamada para 112, localização disponível, partilha da localização e contexto de apoios de emergência. A implementação é funcionalmente mais explícita sobre ações reais do Android do que a referência; visualmente é diferente da composição da imagem.

### 9 — Smartwatch

Existe superfície explicativa que limita corretamente a capacidade a notificações Android/plataformas externas. A referência contém uma representação visual do relógio e cartões de orientação; essa apresentação visual ainda não foi reproduzida.

### 10 — Modo Peregrino

Existe uma superfície real de alto contraste ligada ao estado da caminhada, com progresso, distância e ações de APOI/SOS. Visualmente é uma interpretação própria e ainda não corresponde à composição da referência.

## Estado da auditoria

- Preparação: estrutura auditada; fidelidade visual ainda incompleta.
- Caminhada/mapa: auditada; problema de fundo cartográfico offline identificado.
- Bottom sheet: funcionalmente auditado; comparação visual detalhada pendente.
- Próximos 10 km: funcionalidade existente; comparação visual final pendente.
- Apoios: funcionalidade existente; comparação visual final pendente.
- Progresso: funcionalidade existente; comparação visual final pendente.
- Diário: funcionalidade existente; comparação visual final pendente.
- SOS: funcionalidade existente; comparação visual final pendente.
- Smartwatch: funcionalidade delimitada honestamente; fidelidade visual pendente.
- Modo Peregrino: funcionalidade existente; fidelidade visual pendente.

## Regra de validação

A auditoria deve sempre separar:
- fidelidade visual;
- comportamento funcional;
- dados reais e elegíveis;
- validação física em Android.

Não declarar a V1 visualmente concluída apenas porque o CI passa.

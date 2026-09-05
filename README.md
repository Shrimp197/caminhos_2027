# Caminho do Centenário — V1 Android

Projeto Android do Caminho do Centenário com uma V1 autónoma de walking, GPS, consulta APOI e apoio à decisão, preparada para compilação e validação por GitHub Actions.

## Estado atual

A branch de desenvolvimento oficial é `v1-route-import` e o trabalho continua separado de `main`.

### Percurso
- GPX oficial ACF preservado em `data/routes/Caminho_do_centenario.gpx`;
- SHA-256 da fonte oficial registado e verificado em CI;
- geometria normalizada para `app/src/main/assets/data/route.geojson`;
- distância publicada ACF (`211.87 km`) mantida distinta da extensão técnica da geometria (`214.778165 km`);
- o ecrã ativo usa essa geometria para uma visualização esquemática local, sem depender de cartografia externa;
- o marcador corrente é posicionado pela distância acumulada da geometria, em vez de assumir pontos uniformemente espaçados;
- a superfície também mostra o sentido do segmento atual do traçado em oito direções cardeais/intercardeais, sem o apresentar como orientação física do utilizador ou instrução de virar;
- KML histórico `ACF_2020` mantido apenas como referência, nunca como geometria de produção.

### Walking e GPS
- preparação de caminhada;
- arranque confirmado pelo primeiro sinal GPS;
- projeção da posição na rota validada;
- progresso e etapa quando esta estiver disponível;
- proteção contra timestamps futuros e movimentos GPS implausíveis;
- preservação da última posição fiável durante perda de sinal;
- persistência e recuperação da sessão;
- superfície ativa inferior limitada e rolável para ecrãs pequenos;
- degradação defensiva da visualização local quando a geometria recebida contém pontos inválidos.

### APOI
- catálogo publicado e qualificado;
- consulta por posição;
- pesquisa e seleção;
- detalhe operacional;
- disponibilidade e confiança apresentados explicitamente;
- decisão sobre opções para a posição atual;
- dados históricos não são tratados como garantia 2027;
- o catálogo de produção permanece vazio enquanto não existir evidência qualificada para os APOI de 2027.

## Validação

GitHub Actions executa validação estrutural, testes JVM, build e verificação do APK, além da verificação independente da proveniência do GPX oficial.

A versão atual continua a ser uma build `debug`. Ainda não é uma release assinada para distribuição.

## Próximos marcos

1. validação em Android físico do GPS, acompanhamento do percurso, perda/recuperação de sinal e consumo de bateria;
2. consolidação da UX para utilização durante a caminhada;
3. integração de etapas oficiais apenas quando confirmadas por fonte oficial;
4. preparação de estratégia de mapas offline;
5. integração dos dados APOI 2027 após confirmação e qualificação;
6. preparação da release de produção.

## Regra de integridade

Não inventar geometria, etapas ou APOI. Fontes comunitárias são referência auxiliar. Alterações de dados não devem exigir alterações à lógica de domínio e a aplicação não deve descarregar dados externos durante o build.

## Correção da primeira build

A primeira APK carregava o HTML via `file://`, o que impedia o `fetch()` dos ficheiros locais. A V1 usa uma origem HTTPS local (`appassets.androidplatform.net`) para servir os assets e permitir o carregamento do GeoJSON/JSON.

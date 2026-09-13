# Captura da fonte oficial — Caminho do Centenário

## Objetivo

Este documento define a entrada controlada dos bytes oficiais do Caminho do Centenário para a V1.

A produção só pode ser promovida depois de existir o GPX e/ou KML oficial disponibilizado pela ACF – Associação Caminhos de Fátima, com rastreabilidade suficiente para reproduzir a origem.

## Ficheiros aceites

- `Caminho_do_centenario.gpx` — fonte oficial ACF, arquivo de documentos de 2024.
- `caminho-do-centenario.kml` — fonte oficial ACF, arquivo de documentos de 2024.

Não são aceites como substitutos:

- `ACF_2020` ou qualquer KML histórico do repositório;
- tracks de Wikiloc, Komoot, Strava ou outros terceiros;
- screenshots, PDFs de mapas ou listas de coordenadas copiadas;
- geometrias reconstruídas por proximidade ou divisão matemática.

## Procedimento de entrada

1. Obter o ficheiro original a partir da fonte oficial ACF.
2. Não editar, reformatar ou regravar o ficheiro antes da captura.
3. Registar nome, formato, URL oficial, data/hora de captura, número de bytes e SHA-256.
4. Preservar o ficheiro original como fonte imutável.
5. Fazer o parse para uma geometria ordenada separada do original.
6. Validar estrutura, continuidade, origem, destino e comprimento antes da promoção.

## URLs oficiais conhecidas

- GPX: https://caminhosdefatima.com/wp-content/uploads/2024/10/Caminho_do_centenario.gpx
- KML: https://caminhosdefatima.com/wp-content/uploads/2024/09/caminho-do-centenario.kml

## Estado atual

**GPX oficial capturado e verificado por SHA-256; promoção ainda pendente.**

A identidade dos bytes do GPX é verificada automaticamente no GitHub Actions contra a URL oficial ACF. O ficheiro tem 377117 bytes, SHA-256 `1159c88bc316f0b73257e2c4d89cf3911ddf2191106609de43763a0bf2999266`, 1 track, 1 segmento e 5082 pontos.

O original binário continua fora do repositório enquanto a infraestrutura de ingestão de bytes não permitir preservá-lo sem alteração. A verificação de proveniência não equivale à promoção da geometria para produção.

## Gate de promoção

Só promover para o dataset V1 quando todos os seguintes forem verdadeiros:

- fonte identificada e rastreável;
- bytes preservados;
- SHA-256 calculado e reproduzível;
- parser concluído sem perda/reordenação silenciosa;
- geometria com pelo menos dois pontos e coordenadas válidas;
- continuidade validada;
- origem compatível com Vila Nova de Gaia;
- destino compatível com Fátima;
- comprimento compatível com a referência oficial de 211,87 km / cerca de 212 km;
- etapas oficiais confirmadas separadamente, quando existirem.

A validação geométrica não deve corrigir, normalizar, encurtar ou substituir a distância medida para fazer o candidato coincidir com a distância publicada. Qualquer diferença deve permanecer explícita no registo de validação.

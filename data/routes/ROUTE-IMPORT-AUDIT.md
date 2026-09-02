# Importação do percurso oficial — auditoria V1

## Estado

**Em revisão — sem dataset de produção do percurso.**

Data da auditoria: 2026-09-02
Branch: `v1-route-import`

## Fonte oficial pretendida

A geometria de produção do Caminho do Centenário deve vir dos ficheiros GPX/KML disponibilizados pela ACF — Associação Caminhos de Fátima.

- Página do percurso: https://caminhosdefatima.com/caminho-do-centenario/
- Arquivo de documentos: https://caminhosdefatima.com/category/documentos/
- GPX oficial: `Caminho do Centenário – Completo GPX`, publicado em 15/10/2024.
- KML oficial: `Caminho do Centenário – Completo KML`, publicado em 17/09/2024.

## O que foi encontrado no repositório

Existe um KML legado em `app/src/main/assets/caminho-do-centenario.kml`.

O conteúdo interno identifica-se como uma base anterior (incluindo referências `ACF_2020`). Apesar de ser potencialmente útil como material histórico/referência, **não há evidência suficiente nesta auditoria para o tratar como os bytes dos ficheiros oficiais disponibilizados pela ACF em 2024**.

Por isso:

- não é copiado para o dataset de produção;
- não é usado para inventar ou reconstruir etapas;
- não é usado para declarar uma geometria V1 como oficial;
- permanece disponível apenas como referência histórica do primeiro protótipo.

## Regra de importação

O dataset V1 só poderá receber a geometria de produção depois de ser possível identificar e preservar os bytes do GPX/KML oficial, juntamente com a sua origem e data de consulta.

A partir desse ficheiro será feita uma normalização determinística para GeoJSON `LineString`, preservando a ordem do traçado.

A distância oficial publicada pela ACF (cerca de 212 km; 211,87 km na página específica) é uma referência de controlo, não uma licença para fabricar uma geometria ou ajustar artificialmente o traçado.

As etapas oficiais só serão incluídas quando a sua definição estiver confirmada por fonte oficial. Uma lista de etapas criada a partir de blogs, Wikiloc, trilhos de terceiros ou divisão matemática da geometria não será apresentada como oficial.

## Critério de conclusão desta fase

Esta fase só fica concluída quando existir:

1. ficheiro oficial preservado ou referência inequívoca aos bytes oficiais;
2. dataset V1 do percurso com geometria real;
3. validação estrutural da geometria;
4. validação da distância contra a referência oficial, com tolerância documentada;
5. etapas oficiais incluídas apenas se confirmadas;
6. testes do parser/validador com dados TEST/FICTITIOUS separados da produção.

Até lá, **não criar um `caminho-do-centenario-v1.json` fictício nem preencher coordenadas/stages aproximados**.

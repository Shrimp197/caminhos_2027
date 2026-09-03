# Importação do percurso oficial — auditoria V1

## Estado

**Fonte oficial localizada — bytes ainda não capturados/validados; sem dataset de produção do percurso.**

Data da auditoria: 2026-09-03  
Branch: `v1-route-import`

## Fonte oficial pretendida

A geometria de produção do Caminho do Centenário deve vir dos ficheiros GPX/KML disponibilizados pela ACF — Associação Caminhos de Fátima.

A página oficial do percurso e o arquivo oficial de documentos continuam a disponibilizar o percurso. O arquivo lista separadamente o GPX completo publicado em 15/10/2024 e o KML completo publicado em 17/09/2024. Na verificação de 2026-09-03, os dois links resolveram para os endpoints oficiais, mas a camada de captura disponível não aceitou os content-types `application/gpx+xml` e `application/vnd.google-earth.kml+xml`; por isso os bytes ainda não foram preservados localmente.

- Página do percurso: https://caminhosdefatima.com/caminho-do-centenario/
- Arquivo de documentos: https://caminhosdefatima.com/category/documentos/
- GPX oficial: `Caminho_do_centenario.gpx`, publicado no arquivo de documentos em 15/10/2024.
- KML oficial: `caminho-do-centenario.kml`, publicado no arquivo de documentos em 17/09/2024.
- URL GPX oficial resolvida no arquivo: https://caminhosdefatima.com/wp-content/uploads/2024/10/Caminho_do_centenario.gpx
- URL KML oficial resolvida no arquivo: https://caminhosdefatima.com/wp-content/uploads/2024/09/caminho-do-centenario.kml

## O que foi encontrado no repositório

Existe um KML legado em `app/src/main/assets/caminho-do-centenario.kml`.

O conteúdo interno identifica-se como uma base anterior (incluindo referências `ACF_2020`). Apesar de ser potencialmente útil como material histórico/referência, **não há evidência suficiente nesta auditoria para o tratar como os bytes dos ficheiros oficiais disponibilizados pela ACF em 2024**.

Por isso:

- não é copiado para o dataset de produção;
- não é usado para inventar ou reconstruir etapas;
- não é usado para declarar uma geometria V1 como oficial;
- permanece disponível apenas como referência histórica do primeiro protótipo.

Foi também preservado um capture técnico vazio (`caminho-do-centenario-acf-2020-source-capture.geojson`) para representar explicitamente a ausência de geometria de produção; esse ficheiro **não** é um dataset de rota utilizável.

## Validação já implementada

A branch já contém uma camada determinística para medir o comprimento da geometria e validar discrepâncias grosseiras entre a geometria e a distância declarada.

- `RouteGeometryMetrics.lengthKm(...)` mede a distância acumulada entre pontos consecutivos usando Haversine.
- `RouteValidator` rejeita uma discrepância superior a uma tolerância documentada no código.
- `RouteGeometryValidator` rejeita geometrias vazias, coordenadas inválidas e pontos consecutivos duplicados antes de uma geometria poder conduzir navegação.
- Existem testes com dados `TEST/FICTITIOUS` para a métrica e para a rejeição de uma discrepância grosseira.
- Existe uma workflow isolada de CI para executar testes e build da branch sem os scripts de reescrita do protótipo legado.

Esta validação é deliberadamente **pré-importação**: ainda não é aplicada a uma geometria oficial real.

## Captura controlada da fonte

O procedimento de captura está documentado em `data/routes/CENTENARIO-OFFICIAL-CAPTURE.md` e o utilitário `scripts/capture_centenário_source.py` aceita apenas os nomes oficiais esperados, calcula SHA-256 sobre os bytes originais e regista metadados sem regravar a fonte.

A captura assistida de um ficheiro original é válida quando o ficheiro é preservado sem alteração e a origem é registada. Não é válido fornecer screenshots, PDFs, listas de coordenadas ou uma geometria reconstruída.

## Auditoria do KML histórico

A análise do `ACF_2020.kml` encontrou 371 `LineString`, 7.924 pontos e cerca de 216,099 km de geometria acumulada, face aos 211,87 km publicados para o percurso. A análise de conectividade mostra que existem segmentos com lacunas suficientemente grandes para tornar insegura uma reconstrução por vizinho mais próximo.

Conclusão: o KML histórico serve apenas como fixture técnico/diagnóstico. Não deve ser reparado ou concatenado para fabricar a geometria oficial.

## Regra de importação

O dataset V1 só poderá receber a geometria de produção depois de ser possível identificar e preservar os bytes do GPX/KML oficial, juntamente com a sua origem e data de consulta.

A partir desse ficheiro será feita uma normalização determinística para GeoJSON `LineString`, preservando a ordem do traçado.

A distância oficial publicada pela ACF (211,87 km; apresentada também como cerca de 212 km) é uma referência de controlo, não uma licença para fabricar uma geometria ou ajustar artificialmente o traçado. A página oficial confirma que o percurso liga Vila Nova de Gaia a Fátima e percorre 14 municípios.

As etapas oficiais só serão incluídas quando a sua definição estiver confirmada por fonte oficial. Uma lista de etapas criada a partir de blogs, Wikiloc, trilhos de terceiros ou divisão matemática da geometria não será apresentada como oficial.

## Critério de conclusão desta fase

Esta fase só fica concluída quando existir:

1. ficheiro oficial preservado ou referência inequívoca aos bytes oficiais;
2. dataset V1 do percurso com geometria real;
3. validação estrutural da geometria;
4. validação da distância contra a referência oficial, com tolerância documentada;
5. validação de origem Gaia e destino Fátima;
6. etapas oficiais incluídas apenas se confirmadas;
7. testes do parser/validador com dados TEST/FICTITIOUS separados da produção.

Até lá, **não criar um `caminho-do-centenario-v1.json` fictício nem preencher coordenadas/stages aproximados**.

## Próximo bloco autónomo

1. Obter os bytes do GPX e/ou KML oficiais de 2024 por captura assistida ou outro meio que preserve o ficheiro original.
2. Registar SHA-256, tamanho, URL, data de consulta e tipo de fonte.
3. Extrair a geometria sem reordenar silenciosamente segmentos.
4. Produzir relatório de continuidade, origem/destino e comprimento.
5. Só depois promover a geometria para o dataset V1 e ligar o `WalkingMapModel` ao asset validado.

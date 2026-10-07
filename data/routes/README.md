# V1 — Dados de percurso

Esta pasta contém os dados versionados dos Caminhos de Fátima utilizados pela V1.

## Caminho do Centenário

O percurso encontra-se atualmente em estado **official_gpx_bytes_ingested_validated** para a fonte GPX oficial ACF preservada e verificada em CI.

O ficheiro histórico `app/src/main/assets/caminho-do-centenario.kml` é mantido para comparação e investigação, mas não pode ser promovido a geometria de produção. A geometria de produção V1 é a normalização determinística do GPX oficial validado em `data/routes/Caminho_do_centenario.gpx` para `app/src/main/assets/data/route.geojson`.

A validação atualmente registada inclui:

1. preservação dos bytes oficiais e SHA-256;
2. continuidade e ordem dos pontos;
3. validação estrutural da geometria;
4. cálculo da distância geométrica;
5. comparação com a distância oficial publicada;
6. verificação da origem e destino;
7. validação dos inputs de runtime e da política de distância;
8. testes do parser/validador com dados `TEST/FICTITIOUS` separados da produção.

As etapas oficiais continuam dependentes de confirmação específica por fonte oficial e não são inferidas automaticamente da geometria.

## Fontes comunitárias

Fontes de peregrinos podem ser mantidas como **community_reference** para comparação, contexto de planeamento histórico e descoberta de candidatos APOI. Não constituem autoridade para substituir a geometria oficial nem confirmam automaticamente condições para 2027.

O manifesto `community-source-manifest.json` regista a referência Wikiloc da peregrinação Porto–Fátima de julho de 2025. Os seus metadados podem orientar investigação e comparação, mas os bytes GPX/KML comunitários não são consumidos pela aplicação.

## Regras

- O percurso oficial é referência de navegação e não deve ser alterado pela execução do peregrino.
- Etapas oficiais são dados de referência; não constituem obrigação de distância diária.
- A geometria deve ter uma fonte identificável, data de consulta e identidade de bytes quando aplicável.
- Não descarregar dados externos durante o build.
- Alterações aos dados não devem exigir alterações ao código da aplicação.
- Nenhum ficheiro de terceiros ou traçado histórico deve ser promovido automaticamente a geometria oficial V1.
- A distância publicada pela ACF e a distância técnica da geometria são factos distintos e não podem ser substituídos silenciosamente.

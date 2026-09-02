# V1 — Dados de percurso

Esta pasta contém os dados versionados dos Caminhos de Fátima utilizados pela V1.

## Caminho do Centenário

O percurso está atualmente em estado **awaiting_official_geometry_validation**.

O ficheiro histórico `app/src/main/assets/caminho-do-centenario.kml` é mantido para comparação e investigação, mas não pode ser promovido automaticamente a geometria de produção.

A geometria de produção deverá passar por este processo:

1. obter o GPX/KML oficial validado;
2. preservar o ficheiro original;
3. extrair a geometria para um formato normalizado V1;
4. verificar continuidade e ordem dos pontos;
5. calcular a distância geométrica;
6. comparar com a distância oficial publicada;
7. verificar origem e destino;
8. verificar compatibilidade com as etapas oficiais;
9. só depois publicar o dataset consumido pela aplicação.

## Regras

- O percurso oficial é referência de navegação e não deve ser alterado pela execução do peregrino.
- Etapas oficiais são dados de referência; não constituem obrigação de distância diária.
- A geometria deve ter uma fonte identificável e data de atualização.
- Não descarregar dados externos durante o build.
- Alterações aos dados não devem exigir alterações ao código da aplicação.
- Nenhum ficheiro de terceiros ou traçado histórico deve ser promovido automaticamente a geometria oficial V1.

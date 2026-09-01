# V1 — Dados de percurso

Esta pasta contém os dados versionados dos Caminhos de Fátima utilizados pela V1.

## Regras

- O percurso oficial é referência de navegação e não deve ser alterado pela execução do peregrino.
- Etapas oficiais são dados de referência; não constituem obrigação de distância diária.
- A geometria deve ter uma fonte identificável e data de atualização.
- Não descarregar dados externos durante o build.
- Alterações aos dados não devem exigir alterações ao código da aplicação.

## Estrutura prevista

Cada percurso terá um dataset estruturado contendo, no mínimo:

- `id`
- `name`
- `official_name`
- `geometry`
- `total_distance_km`
- `stages`
- `source`
- `updated_at`

A publicação de dados para a aplicação será feita apenas depois de validação.

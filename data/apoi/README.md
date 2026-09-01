# V1 — APOI

APOI significa **Apoio ao Peregrino**.

Esta pasta contém os dados estruturados e versionados dos pontos de apoio aos peregrinos.

## Princípios

- APOI é informação de suporte à peregrinação, não um catálogo turístico genérico.
- Um local físico pode disponibilizar vários serviços.
- Existência de um local não prova que presta apoio a peregrinos.
- Informação de 2026 não é automaticamente válida para 2027.
- `unknown` não significa `false`.
- Preço desconhecido não significa gratuito.
- A potabilidade da água nunca deve ser inferida.
- Fonte, data, estado e confiança devem ser preservados no master dataset.
- Apenas dados qualificados podem chegar ao dataset publicado da aplicação.

## Categorias V1

1. alimentação
2. água
3. descanso
4. pernoita
5. duches
6. carregamento
7. transporte
8. emergência

## Separação

- `master`: informação completa, incluindo histórico, conflitos e candidatos.
- `published`: subconjunto validado destinado à aplicação.

Dados SR/HF não podem ser publicados no dataset de produção.

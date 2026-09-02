# APOI 2026 → V1 — registo de migração

## Objetivo

Este documento regista como a base `app/src/main/assets/data/apoios-2026.json` será tratada na V1.

A base de 2026 é **referência histórica/teste**. Não é um dataset operacional de 2027 e não deve ser publicada diretamente na aplicação.

## Regra principal

`informação existente em 2026` ≠ `apoio confirmado para 2027`.

A migração preserva os dados úteis, a proveniência e o histórico, mas exige nova qualificação antes de um APOI poder ser apresentado como opção atual ao peregrino.

## Registo dos 12 itens existentes

| ID 2026 | Local | Tratamento V1 | Motivo principal |
|---|---|---|---|
| apoio_2026_001 | Bombeiros Voluntários da Arrifana | migrar para master; 2027 a confirmar | apoio de emergência identificado; não inferir pernoita |
| apoio_2026_002 | Bombeiros Voluntários de Lourosa | migrar para master; 2027 a confirmar | emergência identificada; não inferir outros serviços |
| apoio_2026_003 | Albergue do Apeadeiro | migrar para master; 2027 a confirmar | apoio a peregrinos e dados de pernoita existentes; confirmar 2027 |
| apoio_2026_004 | Albergue Albergaria-a-Nova | migrar para master; 2027 a confirmar | pernoita/duche/WC documentados; confirmar 2027 |
| apoio_2026_005 | Albergue de Peregrinos Rainha D. Teresa | migrar para master; 2027 a confirmar | apoio a peregrinos documentado; preço/horário precisam reconfirmação |
| apoio_2026_006 | Albergue de Peregrinos Santo António | migrar para master; capacidade em revisão | divergência de capacidade; não publicar capacidade sem resolução |
| apoio_2026_007 | Albergue-Residencial Hilário | preservar como histórico | fonte/registo antigo; não assumir funcionamento atual |
| apoio_2026_008 | Albergue de Peregrinos Rainha Santa Isabel | preservar como histórico | funcionamento atual necessita nova confirmação |
| apoio_2026_009 | Bombeiros Voluntários de Ansião | migrar como apoio potencial distante | distância ao traçado demasiado elevada para ser tratado como próximo APOI |
| apoio_2026_010 | Posto de Apoio ao Peregrino — Ribeiro da Vide | migrar para master; 2027 a confirmar | apoio sazonal anunciado em 2026; serviços específicos precisam confirmação |
| apoio_2026_011 | Acolhimento de peregrinos a pé — Santuário de Fátima | migrar para master; 2027 a confirmar | apoio relevante, mas regras sazonais de 2027 ainda não confirmadas |
| apoio_2026_012 | Centro Social de São José de Cluny — candidato institucional | manter como candidato/revisão | existência institucional não prova apoio específico ao peregrino |

## Decisões de qualidade

1. **Não inferir serviços pela natureza da entidade.** Um quartel de bombeiros não passa automaticamente a oferecer pernoita, banho, água ou carregamento.
2. **Não transformar `free: null` em gratuito.** Custo desconhecido continua desconhecido.
3. **Não transformar `reservation: null` em sem reserva.** Política desconhecida continua desconhecida.
4. **Não considerar 2026 como confirmação de 2027.** Informação de 2026 entra no histórico/proveniência e pode gerar uma tarefa de reconfirmação.
5. **Não publicar coordenadas inexistentes ou aproximadas como exatas.**
6. **Não usar APOI fora do traçado como se estivesse no caminho.** Apoios distantes podem ser preservados para contexto, mas têm relação espacial explícita.
7. **Conflitos críticos bloqueiam a publicação do atributo afetado.** Exemplo: capacidade divergente do Albergue de Peregrinos Santo António.
8. **A publicação pode ser parcial por serviço.** Um local pode ter água confirmada e pernoita por confirmar.

## Estados esperados durante a transição

- `candidate` — informação insuficiente para publicação.
- `review` — existe evidência útil, mas falta validação.
- `published_with_warning` — útil para o peregrino, mas existe incerteza relevante que deve ser visível.
- `published` — informação suficientemente atual e qualificada.
- `historical` — informação preservada para histórico, não apresentada como opção atual.
- `closed` — local/serviço confirmado como encerrado.
- `excluded` — não cumpre os critérios de APOI ou está fora do âmbito.

## Relação com o percurso

Os valores de `route_km` e `distance_to_track_m` presentes na base de 2026 **não substituem** a geometria oficial V1.

Quando o GPX/KML oficial for incorporado, as relações espaciais dos APOI deverão ser recalculadas/validadas contra o traçado oficial. Não reutilizar automaticamente distâncias derivadas do traçado de 2026.

## Próxima ação de dados

Após a incorporação da geometria oficial do Caminho do Centenário:

1. normalizar estes registos para o modelo APOI V1;
2. recalcular relação com o traçado oficial;
3. separar master de dataset publicado;
4. atribuir fontes e datas de verificação;
5. criar tarefas de reconfirmação 2027 para informação crítica;
6. só então produzir o primeiro dataset de produção.

# APOI V1 — regras de qualificação e publicação

## Objetivo

Separar claramente **informação existente** de **opção que pode ser apresentada ao peregrino**.

A existência de um local não implica que exista apoio ao peregrino, e informação de 2026 não implica disponibilidade em 2027.

## Estados de publicação

- `candidate` — informação recolhida, ainda sem evidência suficiente.
- `review` — necessita de validação humana ou resolução de conflito.
- `published` — evidência suficiente para apresentação normal.
- `published_with_warning` — útil para o peregrino, mas existe incerteza relevante que deve ser explicitada.
- `historical` — informação de interesse histórico, não apresentada como opção atual.
- `closed` — local/serviço confirmado como encerrado.
- `excluded` — não deve entrar no dataset publicado.

## Mínimo para publicação

Um APOI precisa de:

1. local identificado;
2. evidência de apoio relevante para peregrinos;
3. pelo menos um serviço enquadrado nas 8 categorias V1;
4. relação com o percurso suficientemente compreensível;
5. estado temporal conhecido ou explicitamente incerto;
6. ausência de conflito crítico não resolvido.

## Regras de segurança dos dados

- Nunca inferir pernoita porque o local é um quartel de bombeiros.
- Nunca inferir água potável apenas porque existe água.
- Nunca inferir gratuidade porque o preço está ausente.
- `unknown` não significa `false`.
- Um dado de 2026 deve ser tratado como histórico ou `awaiting_confirmation` para 2027 até existir nova evidência.
- Capacidade, preço, reserva, existência do serviço e encerramento são dados críticos.
- Informação crítica contraditória bloqueia a publicação normal até resolução ou publicação com aviso explícito.
- Dados SR/HF nunca podem ser publicados em produção.

## Localização

A precisão da localização deve ser preservada:

- `exact`
- `approximate`
- `locality_only`
- `unknown`

A distância pelo percurso é preferida à distância em linha reta. Um APOI afastado do percurso deve ser marcado como desvio ou apoio potencial distante, não como estando diretamente no caminho.

## Tempo

Estados possíveis:

- `current`
- `future_confirmed`
- `recurring`
- `historical`
- `expired`
- `awaiting_confirmation`
- `closed`

APOI temporários devem ter validade explícita quando conhecida.

## Confiança

A confiança pode ser global e por atributo. Pelo menos estes aspetos devem poder divergir:

- localização;
- existência/apoio ao peregrino;
- disponibilidade;
- informação crítica.

A interface pode mostrar linguagem simples — `Confirmado`, `Confirmado recentemente`, `Informação a confirmar`, `Informação histórica` — em vez de percentagens.

## Custos e reservas

Custo:

- `free`
- `optional_contribution`
- `paid`
- `unknown`

Reserva:

- `not_required`
- `recommended`
- `required`
- `unknown`

Ausência de valor nunca deve ser convertida automaticamente em gratuito ou sem reserva.

## Publicação por serviço

A qualificação pode ser feita ao nível do serviço/atributo.

Exemplo: um local pode ter `agua = published`, enquanto `pernoita = awaiting_confirmation`. A aplicação não deve transformar a incerteza de um serviço em certeza sobre os restantes.

## Recomendação contextual

Publicar não significa destacar sempre.

A ordenação e destaque devem considerar, entre outros fatores:

- distância pelo percurso;
- necessidade pesquisada;
- disponibilidade temporal;
- confiança;
- eventual desvio.

A aplicação apresenta alternativas e consequências; não decide pelo peregrino.

## Fonte oficial do percurso

O Caminho do Centenário é apresentado pela ACF como percurso entre Vila Nova de Gaia e Fátima, com cerca de 212 km (211,87 km na página específica). A ACF também indica apoio ao peregrino e pontos de interesse ao longo do percurso. A geometria oficial continua a ser a referência para os cálculos de distância do APOI.

## Regra final

> Publicação é determinada por qualidade, relevância, atualidade e contexto — não apenas pela existência de um local.

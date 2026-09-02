# V1 — APOI Data Contract

## Objetivo

Definir o contrato mínimo para que um APOI possa transportar informação operacional real sem confundir ausência de informação com inexistência.

## Campos operacionais

- `cost`: gratuito, contribuição opcional, pago ou desconhecido.
- `reservation`: não necessária, recomendada, obrigatória ou desconhecida.
- `availability`: atual, futura confirmada, recorrente, histórica, expirada, a confirmar ou fechada.
- `capacity`: capacidade total e capacidade de pernoita quando conhecidas.
- `characteristics`: tipo de pernoita, duche, água quente, WC, lavandaria, secagem e acessibilidade quando conhecidas.
- `contact`: responsável, organização, telefone, email, website e social quando disponíveis.
- `confidence`: confiança global, localização, apoio, disponibilidade e informação crítica.

## Regras

1. `UNKNOWN` não significa `false`.
2. `UNKNOWN` não significa gratuito.
3. `UNKNOWN` não significa reserva desnecessária.
4. Informação de 2026 não é automaticamente válida para 2027.
5. A existência de uma instituição não permite inferir serviços de apoio ao peregrino.
6. Dados críticos devem conservar a sua confiança e proveniência antes de serem apresentados como confirmados.
7. A UI omite campos ausentes; não apresenta “não informado” como conteúdo principal.
8. Um APOI pode prestar vários serviços sem criar marcadores duplicados.

## Compatibilidade

Os novos campos têm valores por defeito seguros para não invalidar os APOI existentes durante a migração progressiva dos dados.

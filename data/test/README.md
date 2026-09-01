# V1 — Dados de teste

Os ambientes de teste são separados dos dados de produção.

## SR

Ambiente controlado para testes técnicos, funcionais e de dados. Pode conter APOI fictícios e cenários artificiais.

## HF

Ambiente de validação de experiência com pessoas que não participaram no desenvolvimento. Os dados e cenários devem ser identificados de forma inequívoca como teste.

## Regra crítica

Nenhum registo marcado como `sr` ou `hf` pode entrar num build de produção.

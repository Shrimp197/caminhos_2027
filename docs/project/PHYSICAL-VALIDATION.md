# Caminhos 2027 — Validação Física da V1

## Objetivo

Confirmar num Android real aquilo que o emulador não consegue provar: GPS real, background, bateria, navegação externa e utilização prolongada.

## APK

Usar o APK debug validado produzido pelo CI. O APK deve ser instalado como aplicação de teste e não deve ser confundido com uma versão final de produção.

## Procedimento

1. Instalar o APK no Android.
2. Abrir a aplicação e permitir a localização quando solicitada.
3. Selecionar **Caminho do Centenário**.
4. Na preparação, confirmar que o percurso, início/fim e opções são apresentados corretamente.
5. Aproximar-se do início real do percurso e iniciar a caminhada.
6. Confirmar que o GPS começa a procurar a posição e que, quando o dispositivo está realmente no percurso, a aplicação passa a indicar que está **GPS no percurso**.
7. Caminhar durante algum tempo e confirmar que os quilómetros avançam pela rota.
8. Bloquear o ecrã / deixar a aplicação em segundo plano durante alguns minutos e confirmar que a caminhada continua corretamente.
9. Reabrir a aplicação e confirmar que a caminhada, posição, progresso e tempo continuam coerentes.
10. Pausar e retomar a caminhada e confirmar que o progresso não salta.
11. Testar a navegação externa e confirmar que a aplicação comunica claramente qualquer falha.
12. Guardar a cartografia offline numa ligação disponível, desligar a rede e confirmar que o mapa guardado continua utilizável.
13. Usar a aplicação durante um período prolongado e observar comportamento, aquecimento e consumo de bateria.

## Regras de segurança da validação

- Não usar dados QA para avaliar a experiência de produção.
- Não marcar um teste como passado por inferência.
- Se um passo não puder ser testado, registar **não testado** e o motivo.
- Se o GPS perder sinal, confirmar se o progresso oficial permanece estável antes de tirar qualquer conclusão.
- Uma falha de outra aplicação na navegação externa não deve ser confundida com sucesso da aplicação Caminhos 2027.

## Registo

Data:
Dispositivo:
Versão Android:

GPS real:
Background:
Persistência:
Pausa/retoma:
Navegação externa:
Offline:
Bateria/uso prolongado:

Resultado geral:
- [ ] Aprovado
- [ ] Aprovado com problemas
- [ ] Reprovado

Observações:

## Estado atual

**Teste físico ainda não realizado.**

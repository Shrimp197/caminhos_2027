# V1 — SR: preparação da caminhada

## Objetivo

Validar o primeiro fluxo completo de preparação sem depender da geometria oficial do Caminho do Centenário.

## Fluxo

1. Início
2. Preparar caminhada
3. Escolher início
4. Escolher destino
5. Rever plano
6. Guardar plano
7. Iniciar caminhada

## Regras

- A rota usada no SR é sintética e fica isolada dos dados de produção.
- O peregrino escolhe início e destino; não existe planeamento automático.
- Uma caminhada pode começar ou terminar a meio de uma etapa oficial.
- As etapas oficiais são apenas referência.
- A distância planeada é calculada pela posição no caminho.
- APOI são mostrados apenas quando pertencem ao caminho, estão dentro do intervalo do plano e têm publicação válida.
- Dados históricos, candidatos e dados SR/HF não entram na apresentação de produção.
- Nenhuma coordenada real é inventada para fazer o fluxo parecer funcional.

## Critério de aceitação

Um utilizador de teste deve conseguir criar um plano válido, ver a distância e as etapas atravessadas, consultar os APOI relevantes e guardar o plano sem alterar os dados oficiais das etapas.

O fluxo só pode avançar para `Iniciar caminhada` quando existir uma preparação válida e o plano tiver sido persistido pelo controlador de caminhada.

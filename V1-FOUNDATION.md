# V1 Foundation

Esta branch inicia a reconstrução da aplicação como V1, separada do protótipo WebView legado.

## Regras

- O protótipo existente permanece preservado como referência histórica.
- A V1 não depende de scripts que reescrevam `index.html` ou descarreguem dados durante o build.
- O build deve ser determinístico: código + dados versionados -> APK.
- Dados de rota e APOI são independentes do código da aplicação.
- A V1 inclui apenas os Caminhos de Fátima definidos pelo projeto; não expandir automaticamente o âmbito.
- SR e HF são ambientes de teste e nunca entram no dataset de produção.

## Estado desta fase

Foi fixada a distribuição Gradle 8.11.1 no projeto. A existência deste ficheiro `gradlew` é apenas um ponto de entrada compatível; a criação de um wrapper binário completo continua a depender de uma execução local/CI do Gradle.

## Critério de conclusão da Foundation

Antes de avançar para GPS/mapa, a CI deve conseguir:

1. preparar o checkout sem scripts de transformação do protótipo;
2. executar o build da V1;
3. validar os contratos de dados;
4. gerar um APK debug;
5. verificar que os dados de teste não entram no artefacto de produção.

Se uma alteração exigir remendos sucessivos e não relacionados em várias camadas, parar e investigar a causa estrutural antes de continuar.

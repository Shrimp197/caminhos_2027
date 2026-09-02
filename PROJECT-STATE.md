# Estado do projeto — Caminhos de Fátima V1

> Este ficheiro é o ponto de recuperação do projeto quando uma conversa do ChatGPT termina ou fica inacessível.

Data de referência: 2026-09-02
Repositório: `Shrimp197/caminhos_2027`
Branch principal: `main`

## Objetivo

Construir uma app Android para peregrinos dos Caminhos de Fátima, centrada em autonomia: preparar a caminhada, conhecer a posição no caminho, consultar APOI, comparar opções e prosseguir sem impor uma forma de peregrinar.

## Escopo fechado

- Apenas Caminhos de Fátima dentro do âmbito do projeto.
- Não incluir Rota Carmelita nem outras rotas.
- Etapas oficiais são referência/motivação, não obrigação.
- APOI significa pontos de apoio concretos ao peregrino, não POIs turísticos genéricos.
- As 8 categorias APOI são: alimentação, água, descanso, pernoita, duches, carregamento, transporte e emergência.

## Arquitetura V1 decidida

- Android nativo, Kotlin e Jetpack Compose.
- Dados de rota/APOI versionados e independentes do código.
- Dados mestre separados de dataset publicado.
- Dados do utilizador locais; DataStore para preferências/estado simples e Room quando forem necessários dados estruturados persistentes.
- Sem base de dados online complexa na V1.
- Sem downloads externos durante o build.
- Sem scripts que reescrevam `index.html` durante o build.
- UI não é fonte de verdade; existe estado central da aplicação.
- GPS: posição física bruta → validação/filtro → projeção no caminho → estado da caminhada → UI.

## Dados APOI

- Existe uma base APOI de 2026 útil como referência histórica/transição.
- Não é dataset de produção 2027.
- Não inferir serviços pela natureza da instituição.
- Informação desconhecida não significa falso nem gratuito.
- Publicação depende de qualidade, relevância, recência e contexto.
- Informação incerta pode ser publicada com aviso quando tiver utilidade, mas nunca apresentada como garantia.
- Qualificação pode existir ao nível do APOI e de cada serviço/atributo.
- Proveniência, data de verificação, estado e confiança devem ser preservados.

## Notas

Decisões fechadas:
- Nota de caminhada recebe título automático `Nota da caminhada — DD/MM/AAAA HH:MM`.
- Se data/hora da nota mudar, o título automático mantém a data/hora original; a nova data/hora é guardada separadamente.
- Título introduzido manualmente não é alterado automaticamente.
- Menu de Notas é uma única área com filtros: Todas, Geral, Caminhada.

Ainda pendente:
- Q96: pesquisa nas notas — recomendação atual: pesquisar simultaneamente título e conteúdo, respeitando o filtro ativo.
- Q97: eliminação de nota — ainda não decidido.

## Estado técnico atual

### Foundation
Branch `v1-foundation` criou a base de modelos, schemas, validação, repositories/data sources e CI. O workflow de foundation já teve build e testes JVM concluídos com sucesso.

### APOI
Branches relevantes:
- `v1-apoi-qualification`
- `v1-apoi-data-transition`

Já existem schema V1, modelos Kotlin, parser JSON, validação de publicação, repository/data source e testes com dados TEST/FICTITIOUS isolados.

### Percurso
Branch ativa: `v1-route-import`.

Estado: **em revisão; ainda sem dataset de produção do percurso**.

A ACF disponibiliza oficialmente GPX/KML do Caminho do Centenário, mas o ficheiro KML existente no protótipo é identificado internamente como legado (`ACF_2020`) e não deve ser tratado como os bytes oficiais de 2024.

Não fabricar geometria, etapas ou coordenadas para desbloquear artificialmente esta fase.

Último marco conhecido da branch de percurso: `a443ce48d561c180406d92a561385350c734fe55`, documentação da auditoria de importação.

## Desenvolvimento anterior

O protótipo antigo/monolítico e o ciclo de patches continuam apenas como referência histórica. Não retomar a arquitetura de `index.html` monolítico nem os scripts de reescrita automática.

## Testes

- SR = ambiente técnico/controlado com dados TEST/FICTITIOUS isolados.
- HF = validação de usabilidade com pessoas que não participaram no desenvolvimento.
- Dados SR/HF nunca entram no dataset de produção.
- A V1 deve ser construída por fases com critérios de entrada, alteração, teste, aceitação e conclusão.
- Se forem necessários patches não relacionados entre módulos para manter a aplicação a funcionar, parar e investigar a arquitetura em vez de continuar a remendar.

## Funcionalidades fora do caminho crítico V1

Smartwatch, meteorologia, IA, notificações inteligentes, áudio avançado, social, ranking, gamificação, chat e personalização complexa ficam adiados. A arquitetura não deve bloqueá-los, mas a aplicação V1 tem de ser útil sem eles.

## Próximo trabalho prioritário

1. Consolidar a validação/importação determinística da geometria de rota usando TEST/FICTITIOUS.
2. Continuar a procurar/obter os bytes oficiais GPX/KML sem fabricar dados.
3. Só depois criar o dataset de produção do percurso e validar distância/geometria/etapas oficiais.
4. Integrar a localização no caminho sobre geometria real.
5. Só então avançar para UI V1 de forma controlada.

## Regra de recuperação

Ao retomar o projeto numa nova conversa:

1. Ler este ficheiro primeiro.
2. Verificar branches e últimos commits.
3. Confirmar testes/CI antes de alterar código.
4. Ler a documentação da fase ativa.
5. Não reabrir decisões fechadas sem evidência de que estão erradas.
6. Não criar dados fictícios para preencher lacunas de produção.
7. Trabalhar com progresso verificável e reportar: CONCLUÍDO / EM CURSO / PRÓXIMO / BLOQUEIO.

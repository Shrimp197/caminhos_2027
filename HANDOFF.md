# Caminhos do Peregrino — HANDOFF

Data: 2026-08-31

## Estado real auditado
- Repositório: `Shrimp197/caminhos_2027`
- Branch principal: `main`
- Último commit observado em `main` antes desta candidata: `8da1d532340eab270d46f323d48ba51b6caaea39` (`Remove temporary CI trigger from main`).
- PR #5 (`V1.1.5 candidate — approved preparation UI`) está aberto e não merged; a sua branch é `ci/v1.1.5-candidate`.
- O PR #5 não representa, por si só, uma versão integrada concluída: contém apenas o marcador `CI-V1.1.5-CANDIDATE.md` no commit final observado.
- A última execução CI observada para esse marcador terminou com sucesso e produziu o artifact `Caminhos-do-Peregrino-v1.1.5-debug`, mas isso não equivale a integração em `main`.

## Candidata em desenvolvimento
- Branch: `candidate/v1.1.6-integrated`
- Versão Android: `1.1.6`
- versionCode: `32`
- Objetivo: próxima candidata integrada, sem tratar a V1.1.5 como concluída.

## Requisitos não-regressão
1. O percurso selecionado na preparação é a mesma rota usada pela caminhada.
2. A seleção atualiza o estado central (`activeRoute`/`activeName`) antes do arranque.
3. Cabeçalho, cartão de preparação, menu/biblioteca e navegação mostram o mesmo percurso ativo.
4. SR e HF permanecem como percursos de teste e aparecem explicitamente identificados.
5. Caminho do Centenário permanece percurso oficial, usando os dados 2026 existentes enquanto não houver dados oficiais 2027.
6. Não apresentar os restantes percursos como reais sem os tracks/fontes correspondentes incorporados.
7. Preservar requisitos de `PROJECT-SPEC.md`, incluindo POI/apoios, navegação, áudio, notificações e smartwatch, pausas, meteorologia, suporte e atualização autónoma.
8. Não entregar APK candidata sem validação estrutural, regressão, sintaxe, compilação e verificação do APK.

## Correções consolidadas nesta candidata
- Workflow passa a executar em branches `candidate/**`.
- Artifact passa a identificar `v1.1.6`.
- Foi adicionada uma regressão automática dedicada à seleção de percurso (`scripts/regression_routes.py`).
- `scripts/validate_release.py` passou a verificar invariantes da seleção de percurso e da UI aprovada.
- A pipeline continua a aplicar a UI aprovada, a correção de carregamento SR/HF e a validação antes da compilação.

## Testes técnicos
- A CI anterior da candidata V1.1.5 foi observada como `success` (run `33379620522`), com APK não expirada.
- A candidata V1.1.6 deve ser considerada válida apenas após a nova execução CI terminar com sucesso, incluindo regressão, `node --check`, Gradle assembleDebug e verificação do ficheiro APK.
- Não foi possível executar um clone local nesta sessão porque o ambiente sem acesso DNS à Internet não resolve `github.com`; a validação/build desta candidata deve ser feita pelo GitHub Actions.

## Testes físicos ainda pendentes
- Telefone Android real: GPS, acompanhamento do track, afastamento, bateria e comportamento de ecrã.
- Bluetooth/áudio.
- Huawei Watch GT 2.
- Amazfit Active 2.

## Próxima tarefa
Auditar a execução CI da `candidate/v1.1.6-integrated`; se passar, criar/atualizar PR para `main`, rever o diff e só então considerar a APK como candidata integrada.

## Regra de entrega
Uma build intermédia de CI não deve ser apresentada como candidata. A entrega deve conter versão, commit exato, artifact/APK, validações concluídas, testes físicos dependentes do utilizador e limitações conhecidas.

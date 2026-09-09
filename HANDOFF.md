# Caminhos do Peregrino — HANDOFF

Data: 2026-08-31

## Estado real auditado
- Repositório: `Shrimp197/caminhos_2027`
- Branch principal: `main`
- Último commit observado em `main` antes desta candidata: `8da1d532340eab270d46f323d48ba51b6caaea39` (`Remove temporary CI trigger from main`).
- PR #5 (`V1.1.5 candidate — approved preparation UI`) está aberto e não merged; a sua branch é `ci/v1.1.5-candidate`.
- O PR #5 não representa, por si só, uma versão integrada concluída.

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
- Regressão automática dedicada à seleção de percurso (`scripts/regression_routes.py`).
- Regressão automática de arranque (`scripts/regression_startup.py`).
- `scripts/validate_release.py` verifica invariantes da seleção de percurso e da UI aprovada.
- A pipeline aplica a UI aprovada, a correção de carregamento SR/HF e as validações antes da compilação.

## Incidente encontrado após instalação da primeira APK V1.1.6
- Sintoma reportado no telefone: `Cannot set properties of null (setting "onclick")` ao iniciar.
- Causa confirmada no `assets/index.html` gerado pela pipeline: o runtime da UI aprovada removia o `startWalkBtn` do DOM depois de capturar uma referência a ele. O `bindControls()` original corria depois e tentava atribuir `.onclick` ao elemento já removido.
- Correção aplicada: o botão original deixou de ser removido; fica oculto e continua no DOM para preservar o handler original. O runtime também trata explicitamente o wrapper da preparação e move os cartões legados para a área de detalhe.
- Foi acrescentada regressão que verifica ausência do `removeChild` indevido e confirma que todos os destinos estáticos de `.onclick` existem.

## Testes técnicos
- Run CI V1.1.6 corrigido: `33394368466`, commit `89ff80d1d4f0e3955d561a02f4ae9847ae6aeda3`.
- Resultado: sucesso em todas as etapas, incluindo Harden startup UI, Route selection regression, Startup regression, Validate application structure, Build debug APK, Verify APK e Upload APK.
- Artifact corrigido: `Caminhos-do-Peregrino-v1.1.6-debug`, artifact id `9758746227`.
- SHA-256 do APK corrigido: `dbae11427f3a42bc3fe9282e7ab156c3d9645df78f620e07c04632e0fca5a012`.
- `node --check` do JavaScript extraído da APK corrigida: OK.
- Inspeção da APK corrigida: o `startWalkBtn` permanece no DOM, o `removeChild` indevido desapareceu e não existem destinos `.onclick` estáticos em falta.

## Testes físicos ainda pendentes
- Telefone Android real: confirmar arranque sem erro, seleção SR/HF/Centenário, início da caminhada com a rota selecionada, GPS, acompanhamento do track, afastamento, bateria e comportamento de ecrã.
- Bluetooth/áudio.
- Huawei Watch GT 2.
- Amazfit Active 2.

## Próxima tarefa
Instalar a APK do artifact corrigido e confirmar o arranque e, depois, a seleção de percurso. Só após essa confirmação deve ser considerado o merge para `main`.

## Regra de entrega
Uma build intermédia de CI não deve ser apresentada como candidata. A entrega deve conter versão, commit exato, artifact/APK, validações concluídas, testes físicos dependentes do utilizador e limitações conhecidas.

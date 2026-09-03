# Caminho do Centenário — Android APK (protótipo 2026)

Projeto Android preparado para compilação via GitHub Actions.

## O que já contém
- percurso base do `caminho-do-centenario.kml`;
- versão GeoJSON do track para o mapa;
- dados de apoio 2026 do protótipo;
- WebView Android com GPS/geolocalização;
- interface móvel do protótipo;
- GitHub Actions que gera `app-debug.apk`.

## Como usar no GitHub
1. Copiar o conteúdo deste projeto para o repositório.
2. Fazer commit para `main` (ou `master`).
3. Abrir **Actions** no GitHub.
4. Executar **Build Android APK**.
5. Abrir o workflow concluído e descarregar o artifact **Caminho-do-Centenario-debug**.
6. Dentro do artifact está o `app-debug.apk`.

## Limitações desta versão
- O mapa de fundo ainda usa tiles online quando disponíveis.
- O track e os dados locais ficam incorporados no APK.
- Os marcadores de apoio herdados do protótipo devem ser validados antes de uso real.
- Esta é uma build de teste (debug), não uma release assinada para Play Store.

## Objetivo seguinte
Testar no Android real: GPS, acompanhamento do track, afastamento e consumo de bateria; depois melhorar os mapas offline e substituir os dados 2026 pelos dados oficiais 2027.


## Correção da primeira build
A primeira APK carregava o HTML via `file://`, o que impedia o `fetch()` dos ficheiros locais e deixava o mapa e botões sem inicialização. Esta versão usa uma origem HTTPS local (`appassets.androidplatform.net`) para servir os assets e permitir o carregamento do GeoJSON/JSON.

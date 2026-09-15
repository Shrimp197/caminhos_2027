# Importação do percurso oficial — auditoria V1

## Estado

**Fonte oficial capturada, preservada e validada; geometria de produção V1 criada e verificada por CI.**

Data da auditoria inicial: 2026-09-03  
Branch: `v1-route-import`

## Fonte oficial

A geometria de produção do Caminho do Centenário usa o GPX oficial disponibilizado pela ACF — Associação Caminhos de Fátima.

- GPX oficial: `Caminho_do_centenario.gpx`, publicado no arquivo de documentos da ACF em 15/10/2024.
- URL GPX oficial: `https://caminhosdefatima.com/wp-content/uploads/2024/10/Caminho_do_centenario.gpx`
- Ficheiro preservado: `data/routes/Caminho_do_centenario.gpx`
- SHA-256: `1159c88bc316f0b73257e2c4d89cf3911ddf2191106609de43763a0bf2999266`
- Tamanho: `377117` bytes
- GPX: `1.1`
- Tracks: `1`
- Segmentos: `1`
- Pontos: `5082`

O KML oficial de 2024 permanece identificado como fonte complementar possível. Não é necessário para a navegação runtime enquanto o GPX oficial validado satisfizer os critérios de importação.

## O que foi encontrado no repositório

Existe um KML legado em `app/src/main/assets/caminho-do-centenario.kml` identificado como `ACF_2020`. É mantido como referência histórica e diagnóstico, mas **não** é geometria de produção.

Esse material histórico:

- não é usado para inventar ou reconstruir a rota oficial;
- não é usado para inferir limites de etapas;
- não é apresentado como fonte 2027;
- permanece separado do dataset de produção.

## Geometria de produção

O GPX oficial foi normalizado deterministicamente para:

`app/src/main/assets/data/route.geojson`

A normalização preserva a ordem do track oficial e não reordena segmentos por heurística.

Métricas validadas:

- Distância geométrica Haversine: `214.778165 km`;
- Duplicados consecutivos: `0`;
- Maior salto consecutivo: `916.77 m`;
- Saltos superiores a 1 km: `0`;
- Primeiro ponto: `41.1390874108962, -8.60912359669724`;
- Último ponto: `39.6295907550017, -8.67756042435647`.

A distância publicada pela ACF, `211.87 km` (cerca de 212 km), é preservada separadamente da extensão técnica acumulada da geometria. A aplicação não substitui silenciosamente um valor pelo outro.

## Validação implementada

A branch contém validação determinística para:

- estrutura do GPX;
- geometria vazia ou inválida;
- coordenadas inválidas;
- pontos consecutivos duplicados;
- comprimento geométrico;
- discrepâncias grosseiras face à distância publicada;
- origem e destino;
- inputs de runtime;
- manifesto e política de distância;
- integridade dos bytes oficiais por SHA-256.

Os testes `TEST/FICTITIOUS` permanecem separados da fonte de produção.

A workflow de proveniência oficial executa estas verificações automaticamente e, no commit `252384800b99cce89ee8e74c388600ef28a69aa1`, terminou com sucesso.

## Regras que permanecem

- O percurso oficial é referência de navegação e não deve ser alterado pela execução do peregrino.
- Etapas oficiais são dados de referência e não constituem obrigação de distância diária.
- Etapas só entram quando existir confirmação oficial específica.
- Fontes comunitárias permanecem como `community_reference` e não substituem fontes oficiais.
- A geometria oficial não é fabricada, reparada ou reordenada a partir de material histórico.
- A distância publicada e a distância técnica da geometria são factos distintos.
- Nenhum dado histórico é promovido automaticamente a garantia 2027.
- Não descarregar dados externos durante o build.

## Critério de conclusão da importação

A fase de importação oficial considera-se concluída porque existem simultaneamente:

1. bytes oficiais preservados e identificados por SHA-256;
2. dataset V1 com geometria real;
3. validação estrutural e geométrica;
4. comparação documentada com a distância oficial;
5. origem e destino validados;
6. separação explícita entre fonte oficial e material histórico;
7. testes do parser/validador separados da produção;
8. validação automática em CI do commit de importação.

A partir daqui, o trabalho do eixo de rota é manutenção de proveniência, eventual atualização quando a ACF publicar uma nova versão oficial e integração da informação de etapas apenas quando comprovada.

# v0.2 — teste imersivo

Esta build acrescenta a definição funcional para:

- modo Caminhar dedicado;
- seleção de início/fim por localidades do percurso;
- continuar a partir da posição atual;
- simulação com todos os pontos de apoio já identificados;
- perfil de áudio **Imersivo**;
- avisos de navegação, desvios, recuperação, progresso, localidades,
  aproximação/serviços dos apoios, pernoita e sugestão de pausas.

## Importante
Os dados de apoio continuam a ser os dados de teste de 2026 e devem ser
revalidados para 2027. O KML continua a ser a referência absoluta do percurso.

## Áudio
O perfil “Imersivo” está preparado para ser usado no teste de campo e depois
afinaremos a cadência para a versão de utilização real.

## Compilação
Este pacote está preparado para ser enviado para o repositório GitHub já criado:
https://github.com/Shrimp197/caminhos_2027

O workflow GitHub Actions existente deve voltar a gerar:
`app-debug.apk`

## Objetivo desta build
Validar a experiência no Android, especialmente:
1. seleção do percurso;
2. GPS;
3. rotação/seguimento;
4. simulação;
5. áudio;
6. pontos de apoio.

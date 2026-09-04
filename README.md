# Caminhos do Centenário — V1

A V1 oficial usa o traçado ACF do Caminho do Centenário como fonte local do percurso. A geometria fica embutida no APK para o fluxo de caminhada e não depende de rede para apresentar progresso, relação com o percurso ou a visualização esquemática local.

O ecrã ativo usa a geometria oficial para uma visualização esquemática do percurso, com início, posição corrente e destino. O marcador corrente é calculado pela distância acumulada da geometria, evitando assumir que os pontos estão uniformemente espaçados. A mesma superfície expõe apenas o **sentido do traçado** em oito direções cardeais/intercardeais, derivado do segmento corrente. Isto é uma pista de orientação do próprio percurso, não uma indicação da orientação física do utilizador nem uma instrução de virar.

A cartografia detalhada ainda não está disponível offline nesta V1. A UI deixa essa limitação explícita, enquanto os dados locais de percurso e progresso continuam disponíveis sem rede. Geometrias inválidas são degradadas de forma defensiva para não impedir o desenho da superfície.

O painel inferior da caminhada é limitado e rolável em ecrãs pequenos. O estado GPS, a confiança da posição, o progresso, o próximo APOI e as ações de consulta/decisão permanecem acessíveis sem sobreposição forçada.

A produção de APOI continua vazia até existir evidência qualificada para 2027. Dados de 2026 podem permanecer na camada de migração/análise, mas não são promovidos silenciosamente para garantia operacional de 2027.

O build atual continua como APK de debug para validação no PR; não há publicação assinada.
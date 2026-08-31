# Caminhos do Peregrino — especificação consolidada

Esta especificação reúne as decisões funcionais aprovadas. Não remover requisitos aprovados sem discussão explícita.

## Percursos
- Trajeto teste do SR — cenário QA, GPX casa/trabalho, POI/apoios fictícios.
- Trajecto teste do HF — cenário QA, GPX enviado pelo HF, POI/apoios fictícios.
- Caminho do Centenário — percurso real, não é teste; usar dados específicos de 2026 enquanto não existirem os de 2027.
- Restantes Caminhos de Fátima acordados: Tejo, Norte ao Centro, Nazaré, Médio Tejo, Candeeiros e Rota Carmelita. Só apresentar como reais quando os tracks/fontes correspondentes estiverem efetivamente incorporados.

## POI e apoios
- Track e base de POI/apoios são camadas independentes.
- Reutilizar registos permanentes entre percursos quando geograficamente aplicável; evitar duplicação.
- Cruzar Caminhos de Fátima, Peregrinar.pt 2026 e fontes institucionais relevantes (autarquias, Cruz Vermelha, Ordem de Malta, bombeiros, saúde, etc.).
- Morada/nome suficiente deve ser usado para obter e validar coordenadas; não rejeitar um registo apenas porque as coordenadas não vêm na fonte.
- Separar localização da sede da entidade de localização operacional/temporária do apoio ao peregrino.
- Distinguir coordenada da fonte, confirmada externamente, geocodificada/validada, aproximada e por validar.
- Pernoitas: gratuito, donativo/contributo, pago, desconhecido.
- Dados: confirmado, sazonal, histórico, candidato, por validar.
- Apoios temporários têm validade e expiram automaticamente.
- Apoios ultrapassados podem sair do contexto visual sem serem apagados da base.

## Navegação
- Uma única seta de posição/direção.
- Indicação dinâmica do próximo passo (ex.: “Vire à esquerda em 53 m”).
- POI/apoios interativos.
- Ficha do POI com distância, tipo, estado e ação “Navegar a pé”. Para pontos fora do percurso, indicar o desvio e abrir Google Maps com destino pedonal.
- “Próximos 10 km” e “Onde dormir?” funcionais e contextuais.
- Informação já ultrapassada desaparece progressivamente do contexto principal.

## UX/UI
- Aplicação profissional, moderna, limpa e contextual.
- Cada função tem um local principal; evitar duplicações.
- Preparação: percurso → etapa → apoios/POI → configuração → iniciar.
- Menu global: Percursos, Caminhada, Apoios/POI, Diário, Definições, Ajuda, Contacto, Sobre.
- Todo o percurso tem imagem adequada e utilizável; respeitar origem/licença.
- Dados de teste devem estar explicitamente identificados.
- Ecrã de preparação aprovado: cabeçalho verde com menu, marca/título do produto, título “Prepare a sua caminhada”, subtítulo explicativo, cartão visual do percurso selecionado, grelha de seis funções (Início e fim, Áudio, Orientação, Pausas, Apoios, Notas) e botão verde “INICIAR CAMINHADA”.
- O cartão do percurso é a representação principal do percurso selecionado; a seleção feita nele deve atualizar imediatamente o estado central usado pela caminhada.
- O percurso mostrado no cabeçalho, cartão, menu e navegação deve ser sempre o mesmo percurso ativo.

## Notificações / áudio / smartwatch
- Notificações Android para alertas de caminhada.
- Android 13+: pedir POST_NOTIFICATIONS quando apropriado e sincronizar o estado visual com a permissão real.
- Após autorização: mostrar “✅ Ativos”; se já ativos, abrir definições.
- Notificações devem poder chegar a smartwatches compatíveis.
- Huawei Buds e outros auriculares são áudio Bluetooth genérico.
- Integração inicial de smartwatch: notificações/informação curta; testar Huawei Watch GT 2 e Amazfit Active 2.

## Pausas
- Por tempo, distância e local adequado.
- Entrada personalizada: 95 = 95:00.
- Contextualizar com apoios, clima e exigência do trecho.

## Conselhos e meteorologia
- “Conselhos ao peregrino” com preparação, calçado/meias, hidratação, alimentação, ritmo/pausas, calor, chuva, segurança, equipamento e primeiros cuidados.
- Não usar linguagem prescritiva ou publicidade comercial.
- Cruzar previsão online com percurso, desnível, apoios e posição.
- Mostrar hora da última atualização; offline usar última informação disponível e indicá-la.
- Briefing áudio no início e alertas contextuais curtos durante eventos relevantes.

## Suporte
- Ajuda/FAQ, reportar problema, corrigir POI/apoio, sugerir melhoria e contacto por email.
- Não usar SMS.
- IA responde com base nos dados da aplicação e encaminha casos não resolvidos para formulário pré-preenchido.
- Cada contacto submetido recebe confirmação e agradecimento por email.
- Preferir arquitetura sem custos obrigatórios e free tiers quando suficientes.

## Atualização autónoma
- Verificação automática periódica de POI/apoios.
- Frequência reforçada na época intensa Maio–Outubro.
- Janelas prioritárias em torno dos dias 12 e 13 de Maio, Junho, Julho, Agosto, Setembro e Outubro.
- Alterações de alta confiança podem ser aplicadas automaticamente; conflitos/baixa confiança devem ser sinalizados para validação.

## QA / entrega
- Builds internas não são entregues como candidatas.
- Antes do teste físico: sintaxe, dados, regressão, compilação e verificação do APK.
- Testes físicos: telefone, GPS, Bluetooth, áudio, Huawei GT 2 e Amazfit Active 2.
- Não declarar “completo” sem implementação real.
- Não afirmar trabalho em segundo plano entre mensagens.

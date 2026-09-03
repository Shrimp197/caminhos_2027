from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

assert len(re.findall(r'async function\s+selectRoute\s*\(id\)',html))==1
assert 'id="prepRoute"' in html and 'id="cpRouteSelect"' in html

# The route bridge is canonical even though the visible UI now synchronizes the two selectors directly.
assert 'prepRoute.value=finalRoute.value;dispatch(prepRoute)' in html
assert 'function dispatch(el)' in html
assert 'prepRoute' in html and 'finalRoute' in html

for label in ('Trajeto teste do SR','Trajecto teste do HF'):
    assert label in html,label
for name in ('caminho-tejo.gpx','caminho-norte.gpx','caminho-nazare.kml','caminho-candeeiros.kml','medio-tejo-tomar.gpx','medio-tejo-serta.gpx','medio-tejo-abrantes.gpx','rota-carmelita.kml'):
    assert name in html,name

handlers=re.findall(r"\$\(['\"]startWalkBtn['\"]\)\.onclick",html)
assert len(handlers)<=1,f'unexpected start handler count: {len(handlers)}'
assert "if(cpStart)cpStart.onclick=function(){if(legacyStart)legacyStart.click()};" in html

# Início/Fim is a dedicated modal and must not route through the full legacy route card.
assert "title='Início e fim'" in html
assert "id=\"cpModalStart\"" in html and "id=\"cpModalEnd\"" in html
assert 'detail.appendChild' not in html
assert 'openConfig(kind)' not in html

print('Route regression: OK')
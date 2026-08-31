from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

assert len(re.findall(r'async function\s+selectRoute\s*\(id\)',html))==1
assert 'id="prepRoute"' in html and 'id="cpRouteSelect"' in html
assert 'finalSelect.onchange=function()' in html
assert "prepSelect.value=chosen" in html
assert "prepSelect.dispatchEvent(new Event('change',{bubbles:true}))" in html
assert "finalSelect.value=prepSelect.value" in html

for label in ('Trajeto teste do SR','Trajecto teste do HF'):
    assert label in html,label
for name in ('caminho-tejo.gpx','caminho-norte.gpx','caminho-nazare.kml','caminho-candeeiros.kml','medio-tejo-tomar.gpx','medio-tejo-serta.gpx','medio-tejo-abrantes.gpx','rota-carmelita.kml'):
    assert name in html,name

handlers=re.findall(r"\$\(['\"]startWalkBtn['\"]\)\.onclick",html)
assert len(handlers)<=1,f'unexpected start handler count: {len(handlers)}'
assert "byId('cpStart').onclick=function(){legacyStart.click()};" in html

print('Route regression: OK')
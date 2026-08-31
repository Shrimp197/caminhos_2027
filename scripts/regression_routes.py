from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
html = (ROOT / 'app/src/main/assets/index.html').read_text(encoding='utf-8')

# Regression guard for the historical bug: changing the preparation route must
# update the central active route before navigation starts.
assert "const chosen=$('prepRoute').value" in html
assert "if(chosen!==activeRoute)" in html
assert "await selectRoute(chosen)" in html
assert "$('prepRoute').value=activeRoute" in html
assert "$('headerRoute').textContent=activeName" in html
assert "sel.onchange=async()=>" in html

# The approved preparation card must be a view over the same selector/state.
assert 'id="cpRouteSelect"' in html
assert "finalSelect.addEventListener('change',async function(){" in html
assert "if(typeof selectRoute==='function')await selectRoute(chosen)" in html

# Test routes must remain visible and explicitly identifiable.
for label in ('Trajeto teste do SR', 'Trajecto teste do HF'):
    assert label in html, label
assert 'r.test?' in html

# Official route catalog must remain present in the build source.
for name in (
    'caminho-tejo.gpx', 'caminho-norte.gpx', 'caminho-nazare.kml',
    'caminho-candeeiros.kml', 'medio-tejo-tomar.gpx', 'medio-tejo-serta.gpx',
    'medio-tejo-abrantes.gpx', 'rota-carmelita.kml'
):
    assert name in html, name

# No obvious duplicate start handler may bypass the hardened async path.
handlers = re.findall(r"\$\('startWalkBtn'\)\.onclick", html)
assert len(handlers) == 1, f'unexpected start handler count: {len(handlers)}'

print('Route regression: OK')

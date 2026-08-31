from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
html = (ROOT / 'app/src/main/assets/index.html').read_text(encoding='utf-8')

# Validate the real route controller instead of one exact JavaScript formatting.
assert re.search(r'function\s+selectRoute\s*\(', html), 'selectRoute implementation missing'

# Preparation route must be connected to the controller.
assert 'id="prepRoute"' in html
assert re.search(r'prepRoute[^\n]{0,300}selectRoute|selectRoute[^\n]{0,300}prepRoute', html), 'preparation route is not connected to route controller'

# Approved preparation selector must use the same route-selection path.
assert 'id="cpRouteSelect"' in html
assert re.search(r'cpRouteSelect', html)
assert re.search(r'selectRoute\s*\(', html), 'approved selector has no route-controller path'

# Test routes must remain available.
for label in ('Trajeto teste do SR', 'Trajecto teste do HF'):
    assert label in html, label
assert 'r.test?' in html

# Official route assets must remain represented in the candidate.
for name in (
    'caminho-tejo.gpx', 'caminho-norte.gpx', 'caminho-nazare.kml',
    'caminho-candeeiros.kml', 'medio-tejo-tomar.gpx', 'medio-tejo-serta.gpx',
    'medio-tejo-abrantes.gpx', 'rota-carmelita.kml'
):
    assert name in html, name

# At most one legacy jQuery start handler may exist.
handlers = re.findall(r"\$\(['\"]startWalkBtn['\"]\)\.onclick", html)
assert len(handlers) <= 1, f'unexpected start handler count: {len(handlers)}'

print('Route regression: OK')

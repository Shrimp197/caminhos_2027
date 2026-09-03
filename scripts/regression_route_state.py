from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

# One canonical route state variable and one route controller.
assert len(re.findall(r"let features=\[\],lines=\[\],supports=\[\],activeRoute=", html))==1
assert len(re.findall(r'async function\s+selectRoute\s*\(id\)',html))==1

# The modal/global route selector must feed the canonical preparation selector.
assert 'prepRoute.value=finalRoute.value;dispatch(prepRoute)' in html
assert 'function dispatch(el)' in html
assert 'if(finalRoute&&prepRoute)' in html

# The canonical start action remains connected to the existing walking start path.
assert re.search(r"\$\('startWalkBtn'\)\.onclick=function\(\)",html)
assert "if(cpStart)cpStart.onclick=function(){if(legacyStart)legacyStart.click()};" in html

# Test routes and official Centenário remain in the application source.
for token in ("id:'centenario'","id:'teste-sr'","id:'teste-hf'",'Trajeto teste do SR','Trajecto teste do HF'):
    assert token in html, token

# Início/Fim is intentionally a separate stage modal and must not reuse the legacy route card.
assert "title='Início e fim'" in html
assert 'detail.appendChild' not in html
assert 'openConfig(kind)' not in html

print('Route state regression: OK')
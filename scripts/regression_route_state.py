from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

# One canonical route state variable and one route controller.
assert len(re.findall(r"let features=\[\],lines=\[\],supports=\[\],activeRoute=", html))==1
assert len(re.findall(r'async function\s+selectRoute\s*\(id\)',html))==1

# Visible route selector must feed the canonical preparation selector.
assert 'finalSelect.onchange=function()' in html
assert "prepSelect.value=chosen" in html
assert "prepSelect.dispatchEvent(new Event('change',{bubbles:true}))" in html

# The canonical start action applies the stage before navigation/GPS.
assert re.search(r"\$\('startWalkBtn'\)\.onclick=function\(\)\{[^}]*applyPrep\(\).*showNav\(\).*startGPS\(\)",html)

# Test routes and official Centenário remain in the application source.
for token in ("id:'centenario'","id:'teste-sr'","id:'teste-hf'",'Trajeto teste do SR','Trajecto teste do HF'):
    assert token in html, token

print('Route state regression: OK')
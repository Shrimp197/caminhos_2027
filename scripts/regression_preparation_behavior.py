from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

assert html.count('id="cpFinalShell"')==1
assert html.count('id="cpRouteSelect"')==1
assert html.count('id="cpStart"')==1
for kind in ('route','audio','orientation','pause','support','notes'):
    assert html.count(f'data-cp-detail="{kind}"')==1, kind

# The visible tiles must open canonical controls, not a separate implementation.
assert 'function openConfig(kind)' in html
assert "Object.keys(cards).forEach(function(k){if(cards[k])cards[k].style.display=k===kind?'block':'none'})" in html
for kind,title in [('route','PERCURSO'),('audio','ÁUDIO'),('orientation','ORIENTAÇÃO'),('pause','PAUSAS'),('support','APOIOS')]:
    assert f"if(title==='{title}')" in html or (kind=='pause' and "title.indexOf('PAUSAS')===0" in html) or (kind=='support' and "title.indexOf('APOIOS')===0" in html), kind

# Five tiles use the real controls; Notes uses the existing note action.
assert "prep.querySelectorAll('.prep>.card')" in html
assert "byId('addNoteBtn').click()" in html
assert "byId('startWalkBtn')" in html
assert "byId('cpStart').onclick=function(){legacyStart.click()};" in html

# Route selection goes through the canonical preparation select event.
assert "prepSelect.dispatchEvent(new Event('change',{bubbles:true}))" in html
assert 'finalSelect.onchange=function()' in html

# No second preparation grid is exposed.
assert '#functionGrid{display:none!important}' in html

print('Preparation behavior regression: OK')
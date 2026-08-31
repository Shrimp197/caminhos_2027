from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

assert html.count('id="cpFinalShell"')==1
assert html.count('id="cpRouteSelect"')==1
assert html.count('id="cpStart"')==1
for kind in ('route','audio','orientation','pause','support','notes'):
    assert html.count(f'data-cp-detail="{kind}"')==1, kind

# The six visible tiles must open the canonical controls, never a cloned copy.
assert 'function openConfig(kind)' in html
assert "Object.keys(cards).forEach(function(k){if(cards[k])cards[k].style.display=k===kind?'block':'none'})" in html
assert "prep.querySelectorAll('.prep>.card')" in html
assert 'detail.appendChild(cards[k])' in html

# The five configurable tiles use the canonical configuration cards.
for kind,title in [('route','PERCURSO'),('audio','ÁUDIO'),('orientation','ORIENTAÇÃO'),('pause','PAUSAS'),('support','APOIOS')]:
    assert kind in html and title in html, kind

# Notes delegates to the existing functional note control; Start delegates to the real start path.
assert "const n=byId('addNoteBtn');if(n)n.click();" in html
assert "byId('startWalkBtn')" in html
assert "byId('cpStart').onclick=function(){legacyStart.click()};" in html

# Route selection uses the canonical preparation selector and event path.
assert "prepSelect.dispatchEvent(new Event('change',{bubbles:true}))" in html
assert 'finalSelect.onchange=function()' in html

# No second preparation grid/controller is exposed.
assert '#functionGrid{display:none!important}' in html
assert 'function bindPreparationActions()' not in html
assert 'function normalizeSupportFilterUI()' not in html

print('Preparation behavior regression: OK')
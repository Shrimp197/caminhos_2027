from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

assert html.count('id="cpFinalShell"')==1
assert html.count('id="cpRouteSelect"')==1
assert html.count('id="cpStart"')==1
assert html.count('id="cp-final-interaction-controller"')==1
for kind in ('route','audio','orientation','pause','support','notes'):
    assert html.count(f'data-cp-detail="{kind}"')==1, kind

# The six shortcuts must open the purpose-built modal controller, not legacy cards.
assert 'function show(kind)' in html
assert 'function save(kind)' in html
assert "btn.onclick=function(){var kind=btn.dataset.cpDetail" in html
assert "if(kind==='notes'){if(note)note.click();return}show(kind)" in html
assert 'openConfig(kind)' not in html
assert 'detail.appendChild(cards[k])' not in html

# Start/end modal contains only stage controls and persists them through canonical controls.
assert "title='Início e fim'" in html
assert "id=" + '"cpModalStart"' + "" in html
assert "id=" + '"cpModalEnd"' + "" in html
assert "prepStart.value=byId('cpModalStart').value" in html
assert "prepEnd.value=byId('cpModalEnd').value" in html

# Audio and orientation write to their canonical selectors.
assert "id=" + '"cpModalAudio"' + "" in html
assert "audio.value=byId('cpModalAudio').value" in html
assert "id=" + '"cpModalOrientation"' + "" in html
assert "orientation.value=byId('cpModalOrientation').value" in html

# Pause modal writes time, distance and support-warning configuration.
assert "id=" + '"cpModalPauseTime"' + "" in html
assert "id=" + '"cpModalPauseDistance"' + "" in html
assert "id=" + '"cpModalPauseSupport"' + "" in html
assert "pauseTime.value=byId('cpModalPauseTime').value" in html
assert "pauseDistance.value=byId('cpModalPauseDistance').value" in html
assert "pauseSupport.checked=byId('cpModalPauseSupport').checked" in html
assert 'O valor personalizado por tempo é em minutos.' in html

# Support modal exposes all canonical categories and synchronizes changes back to the canonical controls.
for key in ('all','overnight','water','shower','health','fire','temporary','other'):
    assert f'data-filter="{key}"' in html, key
assert 'function buildSupportModal()' in html
assert 'function syncSupportModalVisual()' in html
assert 'function syncSupportCanonical()' in html
assert 'Canonical handler determines final All state' in html

# No legacy preparation cards may be re-used as the shortcut target.
assert 'detail.style.display=\'none\'' in html
assert 'cp-legacy-prep-card' in html

# Route bridge and notes behavior remain canonical.
assert "prepRoute.value=finalRoute.value;dispatch(prepRoute)" in html
assert "if(cpStart)cpStart.onclick=function(){if(legacyStart)legacyStart.click()};" in html

print('Preparation behavior regression: OK')
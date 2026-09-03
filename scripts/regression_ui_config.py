from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

assert html.count('id="cpFinalShell"')==1
assert html.count('id="cp-final-interaction-controller"')==1
for kind,label in [('route','Início e fim'),('audio','Áudio'),('orientation','Orientação'),('pause','Pausas'),('support','Apoios'),('notes','Notas')]:
    assert html.count(f'data-cp-detail="{kind}"')==1,kind
    assert label in html,label

# One canonical modal is expected. A modal is the single configuration surface,
# not a duplicate preparation menu.
assert html.count('id="cpConfigModal"')==1
assert 'function show(kind)' in html
assert 'function save(kind)' in html
assert 'function closeModal()' in html
assert 'cp-modal-backdrop' in html

# The six shortcuts use the dedicated controller and never move legacy cards into the detail area.
assert "if(kind==='notes'){if(note)note.click();return}show(kind)" in html
for title in ("title='Início e fim'","title='Áudio'","title='Orientação'","title='Pausas inteligentes'","title='Apoios & POI'"):
    assert title in html,title
assert 'detail.appendChild' not in html
assert 'openConfig(kind)' not in html

# Canonical controls remain the source of truth and are synchronized on save.
for token in ('prepStart','prepEnd','audioSelect','orientationSelect','pauseTime','pauseDistance','pauseSupport','supportFilters','addNoteBtn'):
    assert token in html, token
for token in ("prepStart.value=byId('cpModalStart').value","prepEnd.value=byId('cpModalEnd').value","audio.value=byId('cpModalAudio').value","orientation.value=byId('cpModalOrientation').value","pauseTime.value=byId('cpModalPauseTime').value","pauseDistance.value=byId('cpModalPauseDistance').value","pauseSupport.checked=byId('cpModalPauseSupport').checked"):
    assert token in html, token

# Support filter: all categories are present and bidirectional modal synchronization exists.
for key in ('all','overnight','water','shower','health','fire','temporary','other'):
    assert f'data-filter="{key}"' in html,key
assert 'function buildSupportModal()' in html
assert 'function syncSupportModalVisual()' in html
assert 'function syncSupportCanonical()' in html
assert 'others.every(function(i){return i.checked})' in html
assert 'all.checked=every' in html

# Legacy cards remain available as hidden internal controls, but cannot become the shortcut target.
assert 'cp-legacy-prep-card' in html
assert 'classList.add(\'cp-legacy-prep-card\')' in html

print('UI configuration regression: OK')

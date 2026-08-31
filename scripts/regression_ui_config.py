from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

# One visible preparation surface with six functional shortcuts.
assert html.count('id="cpFinalShell"')==1
for kind,label in [('route','Início e fim'),('audio','Áudio'),('orientation','Orientação'),('pause','Pausas'),('support','Apoios'),('notes','Notas')]:
    assert html.count(f'data-cp-detail="{kind}"')==1, kind
    assert label in html, label

# Each shortcut opens the corresponding canonical configuration, never a copy.
assert 'function openConfig(kind)' in html
assert "Object.keys(cards).forEach(function(k){if(cards[k])cards[k].style.display=k===kind?'block':'none'})" in html
assert "prep.querySelectorAll('.prep>.card')" in html
assert "detail.appendChild(cards[k])" in html

# Configuration controls retain their real event handlers and persistence.
for token in ["audioSelect').onchange", "orientationSelect').onchange", "pauseTime').onchange", "pauseDistance').onchange", "pauseSupport').onchange", "function saveSettings()", "function setAudio(v)", "function setOrientation(v)"]:
    assert token in html, token

# Notes shortcut delegates to the existing real note action.
assert "byId('addNoteBtn').click()" in html
assert 'function saveNote()' in html
assert 'localStorage.setItem(NOTES' in html

# Canonical stage preparation flows into navigation.
assert "stageStart=Number($('prepStart').value)" in html
assert "stageEnd=Number($('prepEnd').value)" in html
assert "$('navStart').value=String(stageStart)" in html
assert "$('navEnd').value=String(stageEnd)" in html

# Bidirectional support-filter bridge is present.
assert 'const sf=byId(\'supportFilters\')' in html
assert 'others.every(function(x){return x.checked})' in html
assert "all.checked=true" in html
assert "all.checked=false" in html

# Legacy duplicate grid/controller is not visible or re-injected.
assert '#functionGrid{display:none!important}' in html
assert 'function bindPreparationActions()' not in html
assert 'function normalizeSupportFilterUI()' not in html

print('UI configuration regression: OK')
from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

# Approved six-function preparation grid must exist and expose real targets/actions.
for action,label in [('stage','Início e fim'),('audio','Áudio'),('orientation','Orientação'),('pause','Pausas'),('support','Apoios'),('notes','Notas')]:
    assert f'data-action="{action}"' in html, action
    assert label in html, label
assert 'function bindPreparationActions()' in html
assert 'scrollIntoView({behavior:' in html
assert "if(action==='notes'){openNote();return}" in html

# Configuration controls must be wired to state persistence / runtime behaviour.
for token in ["audioSelect').onchange", "orientationSelect').onchange", "pauseTime').onchange", "pauseDistance').onchange", "pauseSupport').onchange", "function saveSettings()", "function setAudio(v)", "function setOrientation(v)"]:
    assert token in html, token

# All filter means all categories are selected in the UI, not just the all flag.
assert 'function normalizeSupportFilterUI()' in html
assert "all.checked){\n      supportFilters=new Set(['all']);" in html
assert 'others.forEach(function(i){i.checked=true' in html
assert "all.parentElement.classList.add('selected')" in html

# Notes action reaches the real note modal/storage implementation.
assert 'function openNote()' in html
assert 'function saveNote()' in html
assert "localStorage.setItem(NOTES" in html

# Stage controls are used to create the navigation stage.
assert 'function applyPrep()' in html
assert "stageStart=Number($('prepStart').value)" in html
assert "stageEnd=Number($('prepEnd').value)" in html
assert "$('navStart').value=String(stageStart)" in html
assert "$('navEnd').value=String(stageEnd)" in html

print('UI configuration regression: OK')

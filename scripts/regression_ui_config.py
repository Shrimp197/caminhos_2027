from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

# Six visible configuration actions must exist and be wired to concrete targets/actions.
for action,label in [('stage','Início e fim'),('audio','Áudio'),('orientation','Orientação'),('pause','Pausas'),('support','Apoios'),('notes','Notas')]:
    assert f'data-action="{action}"' in html, action
    assert label in html, label
assert 'function bindPreparationActions()' in html
assert 'scrollIntoView({behavior:' in html
assert "if(action==='notes'){openNote();return}" in html
assert "const targets={stage:'prepStart',audio:'audioSelect',orientation:'orientationSelect',pause:'pauseTime',support:'supportFilters',notes:'addNoteBtn'}" in html

# Configuration controls must be wired to state persistence / runtime behaviour.
for token in ["audioSelect').onchange", "orientationSelect').onchange", "pauseTime').onchange", "pauseDistance').onchange", "pauseSupport').onchange", "function saveSettings()", "function setAudio(v)", "function setOrientation(v)"]:
    assert token in html, token

# Pause-distance markup must be structurally valid: select closes before its wrapper.
assert re.search(r'id="pauseDistance"[^>]*>.*?</select>\s*<div id="pauseDistanceWrap"',html,re.S)
assert 'id="pauseDistanceWrap"' in html

# All means every concrete category is checked and rendered, not just an 'all' flag.
assert 'function normalizeSupportFilterUI()' in html
assert "supportFilters=new Set(others.map(function(i){return i.dataset.filter})" in html
assert 'others.forEach(function(i){i.checked=true;i.parentElement.classList.add(\'selected\')})' in html
assert "all.checked=false" in html and "others.every(function(x){return x.checked})" in html
assert "if(typeof renderSupports==='function')renderSupports();" in html

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

# Preparation tiles must be initialized after the DOM and original handlers.
assert 'bindControls();bindPreparationActions();normalizeSupportFilterUI();' in html

print('UI configuration regression: OK')

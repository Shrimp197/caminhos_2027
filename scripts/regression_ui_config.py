from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

assert html.count('id="cpFinalShell"')==1
for kind,label in [('route','Início e fim'),('audio','Áudio'),('orientation','Orientação'),('pause','Pausas'),('support','Apoios'),('notes','Notas')]:
    assert html.count(f'data-cp-detail="{kind}"')==1,kind
    assert label in html,label

assert 'function openConfig(kind)' in html
assert "Object.keys(cards).forEach(function(k){if(cards[k])cards[k].style.display=k===kind?'block':'none'})" in html
assert "prep.querySelectorAll('.prep>.card')" in html
assert 'detail.appendChild(cards[k])' in html

# Real configuration handlers and persistence remain in the canonical app IIFE.
for token in ["audioSelect').onchange", "orientationSelect').onchange", "pauseTime').onchange", "pauseDistance').onchange", "pauseSupport').onchange", "function saveSettings()", "function setAudio(v)", "function setOrientation(v)"]:
    assert token in html,token

# Notes delegates to the existing real note control.
assert "const n=byId('addNoteBtn');if(n)n.click();" in html
assert 'function saveNote()' in html
assert 'localStorage.setItem(NOTES' in html

# Stage selection flows into navigation.
assert "stageStart=Number($('prepStart').value)" in html
assert "stageEnd=Number($('prepEnd').value)" in html
assert "$('navStart').value=String(stageStart)" in html
assert "$('navEnd').value=String(stageEnd)" in html

# Support filter is controlled centrally and exposes both directions.
assert 'function setSupportFilter(key,checked)' in html
assert 'const everySelected=' in html
assert 'if(!supportFilters.size||everySelected)' in html
assert "i.checked=isAll?supportFilters.has('all'):supportFilters.has('all')||supportFilters.has(i.dataset.filter)" in html

# No duplicate preparation controller.
assert '#functionGrid{display:none!important}' in html
assert 'function bindPreparationActions()' not in html
assert 'function normalizeSupportFilterUI()' not in html

print('UI configuration regression: OK')
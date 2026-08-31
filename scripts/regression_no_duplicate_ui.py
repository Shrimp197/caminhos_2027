from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

# Every id must be unique. Duplicate IDs are a common source of null/incorrect handlers.
ids=re.findall(r'id=["\']([^"\']+)',html)
seen=set(); dup=[]
for ident in ids:
    if ident in seen: dup.append(ident)
    seen.add(ident)
assert not dup, 'Duplicate DOM ids: '+', '.join(sorted(set(dup)))

# Exactly one visible preparation shell and one global menu controller.
assert html.count('id="cpFinalShell"')==1
assert html.count('id="cpMenuPanel"')<=1
assert html.count('id="cpMenuBtn"')<=1
assert html.count('id="cpRouteSelect"')==1
assert html.count('id="cpStart"')==1

# Legacy helper grid may exist in source for compatibility but must not be visible.
assert '#functionGrid{display:none!important}' in html

# No second preparation controller may be injected by the new architecture.
assert html.count('id="cp-ui-runtime-v115"')==1
assert html.count('function boot(){')==1 or html.count('function boot(){')==2

# The six visible tiles are the only preparation shortcuts.
assert html.count('data-cp-detail=')==6

# Configuration cards are moved into the single detail container instead of copied.
assert "prep.querySelectorAll('.prep>.card')" in html
assert 'detail.appendChild(cards[k])' in html

print('No-duplicate-UI regression: OK')
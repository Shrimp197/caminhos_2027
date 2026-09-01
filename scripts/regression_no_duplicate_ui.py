from pathlib import Path
from html.parser import HTMLParser

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

class IdParser(HTMLParser):
    def __init__(self):
        super().__init__(); self.ids=[]
    def handle_starttag(self, tag, attrs):
        attrs=dict(attrs)
        if 'id' in attrs and attrs['id']:
            self.ids.append(attrs['id'])

p=IdParser(); p.feed(html)
seen=set(); dup=[]
for ident in p.ids:
    if ident in seen: dup.append(ident)
    seen.add(ident)
assert not dup, 'Duplicate DOM ids: '+', '.join(sorted(set(dup)))

# Canonical preparation structure: one visible shell, one controller, six shortcuts.
assert html.count('id="cpFinalShell"')==1
assert html.count('id="cp-final-interaction-controller"')==1
assert html.count('id="cp-ui-runtime-v115"')==0
assert html.count('data-cp-detail=')==6
for kind in ('route','audio','orientation','pause','support','notes'):
    assert html.count(f'data-cp-detail="{kind}"')==1,kind

# Legacy controls may remain as hidden integration sources, but cannot become a visible duplicate.
assert 'cp-legacy-prep-card' in html
assert '#functionGrid{display:none!important}' in html

# Exactly one canonical configuration surface is allowed. The v2 architecture
# intentionally owns a single static modal; this is not a duplicate UI.
assert html.count('id="cpConfigModal"')==1
assert 'cp-modal' in html
assert 'cp-modal-sheet' in html
assert 'cpModalSave' in html
assert 'cpModalCancel' in html
assert 'function openModal(' not in html or html.count('function openModal(')<=1
assert 'detail.appendChild' not in html
assert 'openConfig(kind)' not in html

# The global menu is singular in the canonical preparation UI.
assert 'id="cpGlobalDrawer"' in html
for dest in ('routes','walk','supports','diary','settings','help','contact','about'):
    assert html.count(f'data-cp-dest="{dest}"')==1,dest

print('No-duplicate-UI regression: OK')

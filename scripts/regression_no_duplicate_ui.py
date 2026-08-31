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

# Modal is a single DOM component; its controls may be instantiated/reused by the canonical controller.
assert 'id="cpConfigModal"' in html
assert 'function show(kind)' in html
assert 'function closeModal()' in html
assert 'function openModal(' in html
assert 'detail.appendChild' not in html
assert 'openConfig(kind)' not in html

# The global menu is singular in the canonical preparation UI.
assert 'id="cpGlobalDrawer"' in html
assert html.count('data-cp-dest="routes"')==1
assert html.count('data-cp-dest="walk"')==1
assert html.count('data-cp-dest="supports"')==1
assert html.count('data-cp-dest="diary"')==1
assert html.count('data-cp-dest="settings"')==1
assert html.count('data-cp-dest="help"')==1
assert html.count('data-cp-dest="contact"')==1
assert html.count('data-cp-dest="about"')==1

print('No-duplicate-UI regression: OK')
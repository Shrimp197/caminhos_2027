from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
PATH = ROOT / 'app/src/main/assets/index.html'
html = PATH.read_text(encoding='utf-8')

# Catalog/test routes must resolve from the correct asset directory.
old = "async function loadCatalogRoute(r){const t=await fetch('data/routes/'+r.file).then(x=>{if(!x.ok)throw Error('Percurso não encontrado');return x.text()});"
new = "async function loadCatalogRoute(r){const base=r.test?'data/':'data/routes/';const t=await fetch(base+r.file).then(x=>{if(!x.ok)throw Error('Percurso não encontrado: '+r.name);return x.text()});"
if old in html:
    html = html.replace(old,new,1)

# The approved preparation UI is a visual front-end over the existing controls.
# Its selector must synchronize the canonical preparation selector through the
# existing selectRoute() state path before the legacy start action is invoked.
assert 'id="cpRouteSelect"' in html, 'approved route selector missing'
assert 'id="cpStart"' in html, 'approved start control missing'
assert 'id="prepRoute"' in html, 'canonical preparation route selector missing'

final_change = re.search(r"finalSelect\.addEventListener\(['\"]change['\"],async function\(\)\{", html)
assert final_change, 'approved route selector change handler missing'
change_block = html[final_change.start():]
assert re.search(r"const chosen=this\.value", change_block), 'route choice is not captured'
assert re.search(r"selectRoute\(chosen\)", change_block), 'route choice does not update canonical route state'
assert re.search(r"prepSelect\.value=chosen", change_block), 'route choice does not synchronize preparation selector'

# The visible start button must invoke the real application start path rather
# than maintaining a second independent walking implementation.
assert re.search(r"getElementById\(['\"]cpStart['\"]\)\.addEventListener\(['\"]click['\"],\(\)=>legacyStart\.click\(\)\)", html), 'approved start control is not wired to canonical start action'

# The canonical start action must still contain the real navigation/GPS flow.
start = re.search(r"(?:\$\(['\"]startWalkBtn['\"]\)|getElementById\(['\"]startWalkBtn['\"]\))\.onclick\s*=", html)
if start:
    block = html[start.start():]
    assert re.search(r'applyPrep\s*\(', block), 'canonical start does not apply preparation'
    assert re.search(r'showNav\s*\(', block), 'canonical start does not enter navigation'
    assert re.search(r'startGPS\s*\(', block), 'canonical start does not start GPS'

# Preparation and navigation controls must remain present and connected.
for element_id in ('prepStart','prepEnd','navStart','navEnd'):
    assert f'id="{element_id}"' in html, f'{element_id} control missing'

# The redundant header route label remains hidden.
assert ('id="headerRoute" style="display:none"' in html
        or '.top .brand small{display:none!important}' in html
        or '#headerRoute{display:none' in html)

# Route synchronization helper from the approved UI must exist.
assert 'syncRoutesWhenReady' in html

print('Route-selection hardening: OK')

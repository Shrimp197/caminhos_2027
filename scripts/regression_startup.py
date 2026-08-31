from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
html = (ROOT / 'app/src/main/assets/index.html').read_text(encoding='utf-8')

required = [
    'id="prepScreen"','id="cpFinalShell"','id="cpRouteSelect"','id="cpStart"',
    'id="startWalkBtn"','id="prepRoute"','id="headerRoute"'
]
for token in required:
    assert token in html, token

# The visible preparation shell must be present and explicitly visible.
assert 'id="cpFinalShell" class="cp-shell"' in html
assert '.cp-shell{display:block!important;visibility:visible!important;opacity:1!important}' in html

# The legacy start button may be hidden only because the compact shell delegates
# to its original click handler; it must remain in the DOM and be callable.
assert "const legacyStart=start;" in html
assert "document.getElementById('cpStart').addEventListener('click',()=>legacyStart.click())" in html

# The top-bar route subtitle is deliberately hidden, while the route itself remains selectable.
assert '<small id="headerRoute" style="display:none">' in html
assert 'Caminho do Centenário' in html

# No obsolete code may hide the complete preparation container.
for bad in ("legacyPrep.style.display='none'", "prep.style.display='none'", "originalStart.style.display='none'"):
    assert bad not in html, bad

# DOM ids referenced by the compact $ helper must exist in the source document.
dom_ids=set(re.findall(r'id=[\"\']([^\"\']+)',html))
used_ids=set(re.findall(r"\$\('([^']+)'\)",html))
missing=sorted(x for x in used_ids if x not in dom_ids)
assert not missing, 'missing DOM ids: '+', '.join(missing)

# Route selector synchronization must survive asynchronous route loading.
assert 'function syncRoutesWhenReady()' in html
assert 'const routeSyncTimer=setInterval' in html
assert "finalSelect.addEventListener('change',async function(){" in html
assert "if(typeof selectRoute==='function')await selectRoute(chosen)" in html

print('Startup regression: OK')

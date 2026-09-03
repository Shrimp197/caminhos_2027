from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
path = ROOT / 'app/src/main/assets/index.html'
html = path.read_text(encoding='utf-8')

required = [
    'id="prepScreen"',
    'id="cpFinalShell"',
    'id="startWalkBtn"',
    'id="prepRoute"',
    'id="cpRouteSelect"',
    'id="headerRoute"',
    'id="cpStart"',
]
missing = [token for token in required if token not in html]
if missing:
    raise SystemExit('Startup UI required elements missing: ' + ', '.join(missing))

assert '.cp-shell{display:block!important;visibility:visible!important;opacity:1!important}' in html
assert "legacyStart.style.display='none'" in html
assert "document.getElementById('cpStart').addEventListener('click',()=>legacyStart.click())" in html
assert '<small id="headerRoute" style="display:none"></small>' in html

for bad in ("legacyPrep.style.display='none'", "prep.style.display='none'", "originalStart.style.display='none'"):
    if bad in html:
        raise SystemExit('Obsolete startup-hiding rule found: ' + bad)

# Validate event-handler targets only. Other helper lookups can legitimately
# address dynamic/list elements created by the application at runtime.
dom_ids = set(re.findall(r'id=[\"\']([^\"\']+)', html))
for element_id in re.findall(r"\$\(['\"]([^'\"]+)['\"]\)\.onclick", html):
    if element_id not in dom_ids:
        raise SystemExit('Referenced onclick target missing from HTML: ' + element_id)

print('Startup UI hardening: OK')

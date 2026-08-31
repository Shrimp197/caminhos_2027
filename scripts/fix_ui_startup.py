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

# The shell must not be hidden by the refresh CSS. Hiding the legacy start
# button is intentional: the shell forwards to its existing handler.
assert '.cp-shell{display:block!important;visibility:visible!important;opacity:1!important}' in html
assert "legacyStart.style.display='none'" in html
assert "document.getElementById('cpStart').addEventListener('click',()=>legacyStart.click())" in html

# The redundant route subtitle is kept only as an internal state mirror and is
# never rendered in the top bar.
assert '<small id="headerRoute" style="display:none">' in html

# There must be no old rule that hides the entire preparation shell/container.
for bad in ("legacyPrep.style.display='none'", "prep.style.display='none'", "originalStart.style.display='none'"):
    if bad in html:
        raise SystemExit('Obsolete startup-hiding rule found: ' + bad)

# Basic DOM integrity: every direct $('id') reference must have a matching id.
dom_ids = set(re.findall(r'id=[\"\']([^\"\']+)', html))
used_ids = set(re.findall(r"\$\('([^']+)'\)", html))
missing_dom = sorted(x for x in used_ids if x not in dom_ids)
if missing_dom:
    raise SystemExit('Referenced DOM ids missing from HTML: ' + ', '.join(missing_dom))

print('Startup UI hardening: OK')

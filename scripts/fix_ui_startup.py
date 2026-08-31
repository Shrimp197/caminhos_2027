from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
path = ROOT / 'app/src/main/assets/index.html'
html = path.read_text(encoding='utf-8')

# The approved UI refresh may change the exact runtime block shape. The old
# hardening script depended on that exact shape and therefore failed before
# it could remove the rule that hid the preparation screen.
# Remove any runtime statements that hide the preparation content or start
# button, regardless of surrounding formatting.
for pattern in (
    r"^\s*legacyPrep\.style\.display\s*=\s*['\"]none['\"]\s*;\s*$",
    r"^\s*prep\.style\.display\s*=\s*['\"]none['\"]\s*;\s*$",
    r"^\s*originalStart\.style\.display\s*=\s*['\"]none['\"]\s*;\s*$",
):
    html = re.sub(pattern, "", html, flags=re.MULTILINE)

# The route name is a selection state, not a permanent header subtitle.
# Keep the official Centenário route in the route selector/library, but hide
# the redundant subtitle under the product name.
html = html.replace(
    '<small id="headerRoute">Caminho do Centenário</small>',
    '<small id="headerRoute" style="display:none">Caminho do Centenário</small>',
    1,
)

# Hard assertions for the startup regression we are fixing.
required = [
    'id="prepScreen"',
    'id="startWalkBtn"',
    'id="prepRoute"',
    'id="headerRoute"',
]
missing = [token for token in required if token not in html]
if missing:
    raise SystemExit('Startup UI required elements missing: ' + ', '.join(missing))

if re.search(r"(?:legacyPrep|prep|originalStart)\.style\.display\s*=\s*['\"]none['\"]", html):
    raise SystemExit('Startup UI still hides preparation content')

path.write_text(html, encoding='utf-8')
print('Startup UI hardening: OK')

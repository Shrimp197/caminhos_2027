from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
html = (ROOT / 'app/src/main/assets/index.html').read_text(encoding='utf-8')

required = [
    'prepScreen', 'prepRoute', 'prepStart', 'prepEnd', 'startWalkBtn',
    'manageBtn', 'navScreen', 'navStart', 'navEnd', 'menuBtn', 'gpsBtn',
    'headerRoute'
]
dom_ids = set(re.findall(r'id=[\"\']([^\"\']+)', html))
for element_id in required:
    assert element_id in dom_ids, f'missing startup element: {element_id}'

# Initial state: preparation visible, navigation hidden.
assert re.search(r'<section[^>]*id=[\"\']prepScreen[\"\'][^>]*class=[\"\'][^\"\']*screen', html)
assert re.search(r'<section[^>]*id=[\"\']navScreen[\"\'][^>]*class=[\"\'][^\"\']*hidden', html)

# Redundant route subtitle is hidden in the top bar.
assert ('id="headerRoute" style="display:none"' in html
        or '.top .brand small{display:none!important}' in html
        or '#headerRoute{display:none' in html)

# The legacy start control must have a handler. Accept either the $ helper or
# native DOM APIs; tests must validate the contract, not source formatting.
start_patterns = [
    r"\$\(['\"]startWalkBtn['\"]\)\.onclick\s*=",
    r"getElementById\(['\"]startWalkBtn['\"]\)\.onclick\s*=",
    r"querySelector\(['\"]#startWalkBtn['\"]\)\.onclick\s*="
]
assert any(re.search(p, html) for p in start_patterns), 'startWalkBtn handler missing'

# The start handler must connect preparation state to navigation/GPS.
handler_pos = min([m.start() for p in start_patterns for m in re.finditer(p, html)] or [0])
# Look through the remainder of the script; this avoids depending on the exact
# boundaries or formatting of one function.
script_tail = html[handler_pos:]
assert re.search(r'applyPrep\s*\(', script_tail), 'start handler does not apply preparation'
assert re.search(r'showNav\s*\(', script_tail), 'start flow does not enter navigation'
assert re.search(r'startGPS\s*\(', script_tail), 'start flow does not start GPS'

# Preparation start/end values must be transferred to navigation state. Accept
# either helper-based or native DOM assignment.
prep_start = re.search(r"(?:\$\(['\"]prepStart['\"]\)|getElementById\(['\"]prepStart['\"]\)|querySelector\(['\"]#prepStart['\"]\))\.value", html)
prep_end = re.search(r"(?:\$\(['\"]prepEnd['\"]\)|getElementById\(['\"]prepEnd['\"]\)|querySelector\(['\"]#prepEnd['\"]\))\.value", html)
nav_start = re.search(r"(?:\$\(['\"]navStart['\"]\)|getElementById\(['\"]navStart['\"]\)|querySelector\(['\"]#navStart['\"]\))\.value", html)
nav_end = re.search(r"(?:\$\(['\"]navEnd['\"]\)|getElementById\(['\"]navEnd['\"]\)|querySelector\(['\"]#navEnd['\"]\))\.value", html)
assert prep_start and prep_end and nav_start and nav_end, 'preparation/navigation stage controls are not wired'

# Route selector must have an explicit asynchronous synchronization path.
assert 'syncRoutesWhenReady' in html or 'fillRouteSelectors' in html

# Every direct helper-based onclick assignment must reference an existing DOM id.
for element_id in re.findall(r"\$\(['\"]([^'\"]+)['\"]\)\.onclick", html):
    assert element_id in dom_ids, f'missing onclick target: {element_id}'

# Startup must not eagerly start map/GPS services before navigation begins.
init_match = re.search(r'function\s+init\s*\(\)\s*\{', html)
apply_match = re.search(r'function\s+applyPrep\s*\(', html)
if init_match and apply_match:
    init_block = html[init_match.end():apply_match.start()]
    assert not re.search(r'ensureMap\s*\(', init_block)
    assert not re.search(r'startGPS\s*\(', init_block)

print('Startup regression: OK')

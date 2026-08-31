from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
html = (ROOT / 'app/src/main/assets/index.html').read_text(encoding='utf-8')

# Core startup/preparation UI must exist.
required = [
    'prepScreen', 'prepRoute', 'prepStart', 'prepEnd', 'startWalkBtn',
    'manageBtn', 'navScreen', 'navStart', 'navEnd', 'menuBtn', 'gpsBtn',
    'headerRoute'
]
dom_ids = set(re.findall(r'id=[\"\']([^\"\']+)', html))
for element_id in required:
    assert element_id in dom_ids, f'missing startup element: {element_id}'

# The preparation screen must be the initial screen; navigation must be hidden.
assert re.search(r'<section[^>]*id=[\"\']prepScreen[\"\'][^>]*class=[\"\'][^\"\']*screen', html)
assert re.search(r'<section[^>]*id=[\"\']navScreen[\"\'][^>]*class=[\"\'][^\"\']*hidden', html)

# The redundant route subtitle must not be presented in the top bar.
assert ('id="headerRoute" style="display:none"' in html
        or '.top .brand small{display:none!important}' in html
        or '#headerRoute{display:none' in html)

# Start-walk handler must exist and apply preparation state before navigation/GPS.
start = re.search(r"\$\(['\"]startWalkBtn['\"]\)\.onclick\s*=\s*function\s*\(\)", html)
assert start, 'startWalkBtn handler missing'
next_handler = re.search(r"\$\(['\"](?:resumeBtn|stopWalkBtn|menuBtn)['\"]\)\.onclick", html[start.end():])
end = start.end() + next_handler.start() if next_handler else len(html)
block = html[start.start():end]
assert re.search(r'applyPrep\s*\(', block), 'startWalkBtn does not apply preparation state'
assert re.search(r'showNav\s*\(', block), 'startWalkBtn does not enter navigation'
assert re.search(r'startGPS\s*\(', block), 'startWalkBtn does not start GPS'

# Preparation state must feed the navigation state.
assert re.search(r"stageStart\s*=\s*Number\(\$\(['\"]prepStart['\"]\)\.value\)", html)
assert re.search(r"stageEnd\s*=\s*Number\(\$\(['\"]prepEnd['\"]\)\.value\)", html)
assert re.search(r"\$\(['\"]navStart['\"]\)\.value\s*=\s*String\(stageStart\)", html)
assert re.search(r"\$\(['\"]navEnd['\"]\)\.value\s*=\s*String\(stageEnd\)", html)

# Route selector must have an explicit synchronization path.
assert 'syncRoutesWhenReady' in html or 'fillRouteSelectors' in html

# Every direct $().onclick assignment must reference a DOM id present in the document.
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

from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
html = (ROOT / 'app/src/main/assets/index.html').read_text(encoding='utf-8')

# Core startup/preparation UI must exist.
required = [
    'id="prepScreen"', 'id="prepRoute"', 'id="prepStart"', 'id="prepEnd"',
    'id="startWalkBtn"', 'id="manageBtn"', 'id="navScreen"', 'id="navStart"',
    'id="navEnd"', 'id="menuBtn"', 'id="gpsBtn"', 'id="headerRoute"'
]
for token in required:
    assert token in html, f'missing startup element: {token}'

# The preparation screen is the real startup screen and is not hidden in markup.
assert '<section id="prepScreen" class="screen">' in html
assert '<section id="navScreen" class="nav hidden">' in html

# The redundant route subtitle exists only as an internal state mirror and must
# be hidden by the generated UI controller/CSS before the user sees the header.
assert 'id="headerRoute"' in html
assert ('id="headerRoute" style="display:none"' in html
        or '.top .brand small{display:none!important}' in html
        or '#headerRoute{display:none' in html)

# Starting a walk must apply the preparation selection before entering navigation.
start_match = re.search(r"\$\('startWalkBtn'\)\.onclick\s*=\s*function\s*\(\)", html)
resume_match = re.search(r"\$\('resumeBtn'\)\.onclick\s*=\s*function\s*\(\)", html)
assert start_match, 'startWalkBtn handler missing'
assert resume_match and resume_match.start() > start_match.start()
start_block = html[start_match.start():resume_match.start()]
assert 'applyPrep();' in start_block
assert 'showNav();' in start_block
assert 'startGPS();' in start_block

# applyPrep transfers the selected start/end stage into navigation state.
assert "stageStart=Number($('prepStart').value)" in html
assert "stageEnd=Number($('prepEnd').value)" in html
assert "$('navStart').value=String(stageStart)" in html
assert "$('navEnd').value=String(stageEnd)" in html

# Route selector changes must be wired and asynchronous route loading must be handled.
assert 'function syncRoutesWhenReady()' in html or 'function fillRouteSelectors()' in html
assert re.search(r"\$\('menuBtn'\)\.onclick\s*=\s*function", html)

# Every direct $().onclick assignment must target an existing DOM id.
dom_ids = set(re.findall(r'id=[\"\']([^\"\']+)', html))
for element_id in re.findall(r"\$\('([^']+)'\)\.onclick", html):
    assert element_id in dom_ids, f'missing onclick target: {element_id}'

# No startup code may attempt to access the map before the user starts navigation.
init_start = html[html.find('function init()'):html.find('function applyPrep()')]
assert 'ensureMap();' not in init_start
assert 'startGPS();' not in init_start

print('Startup regression: OK')

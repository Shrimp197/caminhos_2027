from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
html = (ROOT / 'app/src/main/assets/index.html').read_text(encoding='utf-8')

# The approved preparation UI must keep the legacy start button available to
# the original application handler, which remains the single navigation entry point.
assert "const originalStart=start;" in html
assert "originalStart.style.display='none'" in html
assert "id=\"startWalkBtn\"" in html

# The preparation shell must remain visible. The previous regression hid the
# whole .prep container after moving the legacy cards, leaving only the header.
assert "legacyPrep.style.display='none'" not in html
assert "id=\"cpFinalShell\"" in html
assert "const legacyPrep=prep.querySelector(':scope > .prep')||prep;" in html
assert "const oldCards=[...legacyPrep.querySelectorAll(':scope > .card')];" in html

# The redundant route subtitle in the top bar must not be visible.
assert "headerRoute.style.display='none'" in html

# Every static $('id').onclick target must still exist in the generated HTML.
ids = set(re.findall(r'id=[\"\']([^\"\']+)', html))
for element_id in re.findall(r"\$\('([^']+)'\)\.onclick", html):
    assert element_id in ids, f'missing onclick target: {element_id}'

# The final preparation selector must synchronize with the real route selector
# even when the asynchronous route data arrives after the UI runtime.
assert "function syncRoutesWhenReady()" in html
assert "const routeSyncTimer=setInterval" in html

print('Startup regression: OK')

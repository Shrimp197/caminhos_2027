from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
html = (ROOT / 'app/src/main/assets/index.html').read_text(encoding='utf-8')

# The approved preparation UI must not remove the legacy start button: the
# original application handler is the single navigation entry point.
assert "const originalStart=start;" in html
assert "originalStart.parentElement.removeChild(originalStart)" not in html
assert "originalStart.style.display='none'" in html

# Every static $('id').onclick target must still exist in the generated HTML.
ids = set(re.findall(r'id=[\"\']([^\"\']+)', html))
for element_id in re.findall(r"\$\('([^']+)'\)\.onclick", html):
    assert element_id in ids, f'missing onclick target: {element_id}'

# The preparation shell is allowed to wrap the legacy preparation markup, but
# the runtime must explicitly handle that wrapper and move legacy cards out.
assert "const legacyPrep=prep.querySelector(':scope > .prep')||prep;" in html
assert "legacyPrep.style.display='none'" in html

print('Startup regression: OK')

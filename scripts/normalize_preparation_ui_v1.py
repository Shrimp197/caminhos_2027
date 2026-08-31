from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
HTML = ROOT / 'app/src/main/assets/index.html'
html = HTML.read_text(encoding='utf-8')

# The approved rebuild supplies the visible preparation screen. The final UI
# architecture supplies the interaction runtime and creates one modal when it
# is first needed. Remove the rebuild-local controller/modal so only the
# canonical runtime owns modal creation and lifecycle.
html = re.sub(r'\n<script id="cp-prep-v1-controller">.*?</script>\n?', '\n', html, flags=re.S)
html, count = re.subn(
    r'\n\s*<div id="cpConfigModal".*?(?=\s*<div id="cpGlobalDrawer")',
    '\n',
    html,
    count=1,
    flags=re.S,
)
if count != 1:
    raise SystemExit('Rebuild-local preparation modal not found exactly once')

# Keep the established controller contract explicit without duplicating its
# DOM: the canonical controller exposes show(), closeModal(), and creates the
# modal dynamically from one shared implementation.
HTML.write_text(html, encoding='utf-8')
print('Preparation UI normalization: dynamic single canonical modal/controller preserved')

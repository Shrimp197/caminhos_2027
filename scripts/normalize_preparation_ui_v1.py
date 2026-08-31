from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
HTML = ROOT / 'app/src/main/assets/index.html'
html = HTML.read_text(encoding='utf-8')

# rebuild_preparation_screen_v1.py is responsible for the approved visible HTML.
# apply_final_ui_architecture.py is responsible for the canonical interaction runtime.
# Remove the rebuild-local runtime/modal so there is exactly one canonical modal runtime.
html = re.sub(r'\n<script id="cp-prep-v1-controller">.*?</script>\n?', '\n', html, flags=re.S)
html = re.sub(r'\n\s*<div id="cpConfigModal" class="cp-modal" hidden>.*?</div>\s*(?=<div id="cpGlobalDrawer")', '\n', html, count=1, flags=re.S)

# The rebuild template may also include its own static style rules for the removed modal.
# Keep the visual rules harmless, but do not allow an extra modal container to be recreated.
HTML.write_text(html, encoding='utf-8')
print('Preparation UI normalization: single canonical interaction runtime preserved')

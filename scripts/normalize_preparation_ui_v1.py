from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
HTML = ROOT / 'app/src/main/assets/index.html'
html = HTML.read_text(encoding='utf-8')

# The approved rebuild supplies the visible preparation screen. The final UI
# architecture supplies the interaction runtime. Remove the rebuild-local
# controller, then normalize its modal markup into the single canonical modal
# expected by the final controller.
html = re.sub(r'\n<script id="cp-prep-v1-controller">.*?</script>\n?', '\n', html, flags=re.S)

canonical_modal = '''<div id="cpConfigModal" class="cp-modal-backdrop" role="dialog" aria-modal="true">
  <div class="cp-modal-sheet">
    <h2 id="cpModalTitle"></h2>
    <p id="cpModalSubtitle"></p>
    <div id="cpModalBody"></div>
    <div class="cp-modal-actions">
      <button type="button" id="cpModalCancel" class="secondary">Cancelar</button>
      <button type="button" id="cpModalSave" class="primary">Guardar</button>
    </div>
  </div>
</div>'''

html, count = re.subn(
    r'<div id="cpConfigModal".*?(?=\s*<div id="cpGlobalDrawer")',
    canonical_modal + '\n',
    html,
    count=1,
    flags=re.S,
)
if count != 1:
    raise SystemExit('Canonical preparation modal container not found exactly once')

# The canonical runtime is applied earlier in the workflow and contains show()
# and closeModal(). Keep an explicit openModal() helper for the established
# interaction contract without creating a second modal implementation.
if 'function openModal(' not in html:
    html = html.replace(
        'function closeModal(){modal.classList.remove(\'open\');body.innerHTML=\'\'}',
        'function openModal(){modal.classList.add(\'open\')}\n    function closeModal(){modal.classList.remove(\'open\');body.innerHTML=\'\'}',
        1,
    )

HTML.write_text(html, encoding='utf-8')
print('Preparation UI normalization: single canonical modal, controller, and openModal helper preserved')

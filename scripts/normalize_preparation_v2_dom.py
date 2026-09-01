from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
HTML=ROOT/'app/src/main/assets/index.html'
s=HTML.read_text(encoding='utf-8')
# v2 owns the preparation header; remove every older header and every older
# menu button before restoring exactly one canonical right-aligned menu button.
s=re.sub(r'<header class="top".*?</header>\s*','',s,flags=re.S)
s=re.sub(r'<button id="cpTopMenuBtn"[^>]*>.*?</button>\s*','',s,flags=re.S)
needle='<header class="cp-topbar">'
replacement=needle+'\n      <button id="cpTopMenuBtn" class="cp-menu-btn" type="button" aria-label="Abrir menu">☰</button>'
# Place the button after the canonical brand container, not before it.
brand_end='</div>\n    </header>'
if brand_end not in s: raise SystemExit('Canonical preparation header not found')
s=s.replace(brand_end,'</div>\n      <button id="cpTopMenuBtn" class="cp-menu-btn" type="button" aria-label="Abrir menu">☰</button>\n    </header>',1)
# Remove any duplicate addNoteBtn integration anchors and retain one hidden anchor.
s=re.sub(r'<button id="addNoteBtn"[^>]*>.*?</button>\s*','',s,flags=re.S)
marker='      <div id="cpLegacySource" class="cp-legacy-prep-card" aria-hidden="true">'
if marker not in s: raise SystemExit('Legacy integration source not found')
s=s.replace(marker,marker+'\n        <button id="addNoteBtn" type="button"></button>',1)
HTML.write_text(s,encoding='utf-8')
print('Preparation v2 DOM normalization: exactly one canonical menu and note integration id')

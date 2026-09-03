from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
HTML=ROOT/'app/src/main/assets/index.html'
s=HTML.read_text(encoding='utf-8')

# This step must normalize the already-built canonical v2 screen without
# assuming a particular indentation/layout produced by earlier generators.
# Remove legacy top headers and duplicate menu buttons, but never remove the
# canonical v2 header itself.
s=re.sub(r'<header class="top"[^>]*>.*?</header>\s*','',s,flags=re.S)
s=re.sub(r'<button id="cpTopMenuBtn"[^>]*>.*?</button>\s*','',s,flags=re.S)

# Restore exactly one canonical menu button inside the canonical v2 topbar.
header=re.search(r'<header class="cp-topbar">.*?</header>',s,flags=re.S)
if not header:
    raise SystemExit('Canonical preparation header not found')
block=header.group(0)
block=re.sub(r'\s*<button id="cpTopMenuBtn"[^>]*>.*?</button>','',block,flags=re.S)
block=block.replace('</header>', '\n      <button id="cpTopMenuBtn" class="cp-menu-btn" type="button" aria-label="Abrir menu">☰</button>\n    </header>', 1)
s=s[:header.start()]+block+s[header.end():]

# Keep one hidden compatibility anchor for the existing Notes runtime.
s=re.sub(r'<button id="addNoteBtn"[^>]*>.*?</button>\s*','',s,flags=re.S)
marker='      <div id="cpLegacySource" class="cp-legacy-prep-card" aria-hidden="true">'
if marker not in s:
    raise SystemExit('Legacy integration source not found')
s=s.replace(marker,marker+'\n        <button id="addNoteBtn" type="button"></button>',1)

HTML.write_text(s,encoding='utf-8')
print('Preparation v2 DOM normalization: exactly one canonical menu and note integration id')

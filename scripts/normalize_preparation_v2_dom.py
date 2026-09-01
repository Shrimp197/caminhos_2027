from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
HTML=ROOT/'app/src/main/assets/index.html'
s=HTML.read_text(encoding='utf-8')
# v2 owns the preparation header; remove any older header that survived earlier transforms.
s,n=re.subn(r'<header class="top".*?</header>\s*','',s,count=1,flags=re.S)
if n not in (0,1): raise SystemExit('Unexpected legacy header count')
# addNoteBtn is an existing navigation integration anchor; do not duplicate it in the hidden prep bridge.
legacy=s.find('id="cpLegacySource"')
if legacy>=0:
    end=s.find('</div>',legacy)
    block=s[legacy:end]
    block=block.replace('<button id="addNoteBtn" type="button"></button>','')
    s=s[:legacy]+block+s[end:]
HTML.write_text(s,encoding='utf-8')
print('Preparation v2 DOM normalization: unique top menu and integration ids')

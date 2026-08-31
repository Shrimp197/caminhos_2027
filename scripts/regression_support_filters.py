from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

assert 'id="supportFilters"' in html
assert "const all=sf.querySelector('input[data-filter=\"all\"]')" in html
assert 'const complete=others.length>0 && others.every(function(x){return x.checked})' in html
assert "all.checked=true;all.dispatchEvent(new Event('change',{bubbles:true}))" in html
assert "all.checked=false;all.dispatchEvent(new Event('change',{bubbles:true}))" in html

# Canonical handler must still exist and be the one that updates support state/rendering.
assert 'function setSupportFilter(key,checked)' in html
assert 'renderSupports()' in html

# All category controls must be present.
for key in ('all','overnight','water','shower','health','fire','temporary','other'):
    assert f'data-filter="{key}"' in html, key

print('Support filter regression: OK')
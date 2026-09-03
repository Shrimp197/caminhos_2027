from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

assert 'id="supportFilters"' in html
for key in ('all','overnight','water','shower','health','fire','temporary','other'):
    assert f'data-filter="{key}"' in html,key

# The final modal controller mirrors canonical support controls instead of cloning them.
assert 'function buildSupportModal()' in html
assert 'function syncSupportModalVisual()' in html
assert 'function syncSupportCanonical()' in html
assert 'var every=others.length>0&&others.every(function(i){return i.checked})' in html
assert 'all.checked=every' in html
assert 'src.forEach(function(si)' in html
assert "si.checked!==mi.checked" in html

# There is still one canonical support state handler in the application.
assert 'function setSupportFilter(key,checked)' in html
assert 'renderSupports()' in html
assert 'function normalizeSupportFilterUI()' not in html

print('Support filter regression: OK')
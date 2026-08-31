from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')

assert 'id="supportFilters"' in html
assert 'data-filter="all"' in html
assert 'const all=' in html and 'data-filter=\\\"all\\\"' in html
assert 'const everySelected=' in html
assert 'if(!supportFilters.size||everySelected)' in html
assert 'i.checked=isAll?supportFilters.has(\'all\'):supportFilters.has(\'all\')||supportFilters.has(i.dataset.filter)' in html

# Canonical handler must be the only implementation that changes the filter state.
assert 'function setSupportFilter(key,checked)' in html
assert 'renderSupports()' in html
assert 'function normalizeSupportFilterUI()' not in html

# Every concrete category must exist.
for key in ('all','overnight','water','shower','health','fire','temporary','other'):
    assert f'data-filter="{key}"' in html,key

print('Support filter regression: OK')
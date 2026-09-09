from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
path = ROOT / 'app/src/main/assets/index.html'
html = path.read_text(encoding='utf-8')

# The GPX parser must close its function after returning the feature collection.
needle = "return{type:'FeatureCollection',features:[{type:'Feature',properties:{},geometry:{type:'LineString',coordinates:c}}]}\nfunction saveImported"
replacement = "return{type:'FeatureCollection',features:[{type:'Feature',properties:{},geometry:{type:'LineString',coordinates:c}}]}\n}\nfunction saveImported"
if needle in html:
    html = html.replace(needle, replacement, 1)
elif "function parseGpx" in html and "function saveImported" in html:
    # Fail loudly rather than silently changing an unexpected implementation.
    raise SystemExit('parseGpx/saveImported boundary found but expected parser text was not recognised')

# Keep the approved user-facing wording explicit in the menu.
html = html.replace('🛏️ Pernoitas na etapa', '🛏️ Onde dormir? — Pernoitas na etapa', 1)

# The preparation tile is also required to expose the wording directly.
if 'data-cp-detail="support"' in html and 'Onde dormir?' not in html:
    raise SystemExit('Onde dormir? UI contract missing after UI refresh')

path.write_text(html, encoding='utf-8')
print('Index runtime fix: OK')

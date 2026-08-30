from pathlib import Path
import json, re, xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
html_path = root / 'app/src/main/assets/index.html'
manifest_path = root / 'app/src/main/AndroidManifest.xml'
java_path = root / 'app/src/main/java/com/caminhos2027/MainActivity.java'
html = html_path.read_text(encoding='utf-8')
manifest = manifest_path.read_text(encoding='utf-8')
java = java_path.read_text(encoding='utf-8')

required_html = [
    'Trajeto teste do SR', 'Trajecto teste do HF', 'Caminho do Centenário',
    'notificationBtn', 'Próximos 10 km', 'Onde dormir?', 'Carregar KML/GPX',
]
missing = [item for item in required_html if item not in html]
if missing:
    raise SystemExit('Missing required generated UI/features: ' + ', '.join(missing))

if 'android.permission.POST_NOTIFICATIONS' not in manifest:
    raise SystemExit('POST_NOTIFICATIONS permission missing')
for item in ['installWebCompat', 'notifyUser', 'requestNotificationPermission', 'notificationsGranted', 'openNotificationSettings']:
    if item not in java:
        raise SystemExit(f'Missing Android notification bridge capability: {item}')

required_files = [
    root / 'app/src/main/assets/data/apoios-2026.json',
    root / 'app/src/main/assets/data/percurso-teste-casa-trabalho.gpx',
    root / 'app/src/main/assets/data/percurso-teste-hf.gpx',
]
for path in required_files:
    if not path.exists() or path.stat().st_size < 100:
        raise SystemExit(f'Missing/empty required test asset: {path}')

json.loads((root / 'app/src/main/assets/data/apoios-2026.json').read_text(encoding='utf-8'))
ET.parse(manifest_path)

for gpx in required_files[1:]:
    tree = ET.parse(gpx)
    pts = tree.getroot().findall('.//{http://www.topografix.com/GPX/1/1}trkpt')
    if len(pts) < 2:
        raise SystemExit(f'GPX has fewer than 2 track points: {gpx}')

route_files = [
    'caminho-tejo.gpx', 'caminho-norte.gpx', 'caminho-nazare.kml',
    'caminho-candeeiros.kml', 'medio-tejo-tomar.gpx', 'medio-tejo-serta.gpx',
    'medio-tejo-abrantes.gpx', 'rota-carmelita.kml'
]
for name in route_files:
    path = root / 'app/src/main/assets/data/routes' / name
    if not path.exists() or path.stat().st_size < 100:
        raise SystemExit(f'Missing/empty official route asset: {name}')

# Catch broken DOM references before Android compilation.
dom_ids = set(re.findall(r'id="([^"]+)"', html))
used_ids = set(re.findall(r"\$\('([^']+)'\)", html))
missing_dom = sorted(x for x in used_ids if x not in dom_ids)
if missing_dom:
    raise SystemExit('Referenced DOM ids missing from HTML: ' + ', '.join(missing_dom))

js = '\n'.join(re.findall(r'<script[^>]*>(.*?)</script>', html, re.S))
Path('/tmp/index-app.js').write_text(js, encoding='utf-8')
print('Release validation: OK')

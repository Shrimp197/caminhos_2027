from pathlib import Path
import json
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
HTML_PATH = ROOT / 'app/src/main/assets/index.html'
MANIFEST_PATH = ROOT / 'app/src/main/AndroidManifest.xml'
JAVA_PATH = ROOT / 'app/src/main/java/com/caminhos2027/MainActivity.java'
ROUTE_MANIFEST_PATH = ROOT / 'data/routes/route-source-manifest.json'

html = HTML_PATH.read_text(encoding='utf-8')
manifest = MANIFEST_PATH.read_text(encoding='utf-8')
java = JAVA_PATH.read_text(encoding='utf-8')
route_manifest = json.loads(ROUTE_MANIFEST_PATH.read_text(encoding='utf-8'))

required_html = [
    'Caminho do Centenário',
    'Carregar KML/GPX',
    'Próximos 10 km',
    'Onde dormir?',
]
missing = [item for item in required_html if item not in html]
if missing:
    raise SystemExit('Missing committed V1 UI/features: ' + ', '.join(missing))

if 'android.permission.POST_NOTIFICATIONS' not in manifest:
    raise SystemExit('POST_NOTIFICATIONS permission missing')
for item in ['installWebCompat', 'notifyUser', 'requestNotificationPermission', 'notificationsGranted', 'openNotificationSettings']:
    if item not in java:
        raise SystemExit(f'Missing Android notification bridge capability: {item}')

# Provenance is intentionally validated from the route manifest, while the original
# binary remains outside the repository until an explicit ingestion step is completed.
expected_sha = '1159c88bc316f0b73257e2c4d89cf3911ddf2191106609de43763a0bf2999266'
expected_status = 'official_gpx_provenance_verified_pending_repository_ingestion'
if route_manifest.get('status') != expected_status:
    raise SystemExit(f'Unexpected route manifest status: {route_manifest.get("status")}')

official_sources = route_manifest.get('official_source_files', [])
candidate = next((item for item in official_sources if item.get('format') == 'gpx'), None)
if candidate is None:
    raise SystemExit('Official GPX source entry missing from route manifest')
if candidate.get('sha256') != expected_sha:
    raise SystemExit('Official GPX SHA-256 does not match the recorded provenance')
if candidate.get('capture_status') != 'official_url_bytes_verified_by_ci':
    raise SystemExit('Official GPX capture status is not the verified CI state')
if candidate.get('allowed_for_production') is not False:
    raise SystemExit('Official GPX candidate must remain outside production until semantic validation')

required_test_assets = [
    ROOT / 'app/src/main/assets/data/percurso-teste-casa-trabalho.gpx',
    ROOT / 'app/src/main/assets/data/percurso-teste-hf.gpx',
]
for path in required_test_assets:
    if not path.exists() or path.stat().st_size < 100:
        raise SystemExit(f'Missing/empty required test asset: {path}')
    tree = ET.parse(path)
    points = tree.getroot().findall('.//{http://www.topografix.com/GPX/1/1}trkpt')
    if len(points) < 2:
        raise SystemExit(f'GPX has fewer than 2 track points: {path}')

json.loads((ROOT / 'app/src/main/assets/data/apoios-2026.json').read_text(encoding='utf-8'))
ET.parse(MANIFEST_PATH)

# Catch broken DOM references before Android compilation.
dom_ids = set(re.findall(r'id="([^"]+)"', html))
used_ids = set(re.findall(r"\$\('([^']+)'\)", html))
missing_dom = sorted(x for x in used_ids if x not in dom_ids)
if missing_dom:
    raise SystemExit('Referenced DOM ids missing from HTML: ' + ', '.join(missing_dom))

js = '\n'.join(re.findall(r'<script[^>]*>(.*?)</script>', html, re.S))
Path('/tmp/index-app.js').write_text(js, encoding='utf-8')
print('Release validation: OK')

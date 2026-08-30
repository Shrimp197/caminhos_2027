from pathlib import Path
import json
import re
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
html_path = root / 'app/src/main/assets/index.html'
manifest_path = root / 'app/src/main/AndroidManifest.xml'
html = html_path.read_text(encoding='utf-8')
manifest = manifest_path.read_text(encoding='utf-8')

required = [
    'Trajeto teste do SR',
    'Trajecto teste do HF',
    'Caminho do Centenário',
    'notificationBtn',
    'notificationsGranted',
    'requestNotificationPermission',
    'openNotificationSettings',
    'percurso-teste-casa-trabalho.gpx',
    'percurso-teste-hf.gpx',
    'Próximos 10 km',
    'Onde dormir?',
    'Carregar KML/GPX',
]
missing = [item for item in required if item not in html]
if missing:
    raise SystemExit('Missing required UI/features: ' + ', '.join(missing))

if 'android.permission.POST_NOTIFICATIONS' not in manifest:
    raise SystemExit('POST_NOTIFICATIONS permission missing')

required_files = [
    root / 'app/src/main/assets/data/apoios-2026.json',
    root / 'app/src/main/assets/data/percurso-teste-casa-trabalho.gpx',
    root / 'app/src/main/assets/data/percurso-teste-hf.gpx',
]
for path in required_files:
    if not path.exists() or path.stat().st_size < 100:
        raise SystemExit(f'Missing/empty required asset: {path}')

json.loads((root / 'app/src/main/assets/data/apoios-2026.json').read_text(encoding='utf-8'))
ET.parse(manifest_path)

js = '\n'.join(re.findall(r'<script[^>]*>(.*?)</script>', html, re.S))
Path('/tmp/index-app.js').write_text(js, encoding='utf-8')
if js.count('{') != js.count('}'):
    raise SystemExit('Unbalanced JavaScript braces')

print('Release validation: OK')

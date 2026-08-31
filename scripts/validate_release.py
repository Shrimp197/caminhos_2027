from pathlib import Path
import json,re,xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
HTML=ROOT/'app/src/main/assets/index.html'
MANIFEST=ROOT/'app/src/main/AndroidManifest.xml'
JAVA=ROOT/'app/src/main/java/com/caminhos2027/MainActivity.java'
html=HTML.read_text(encoding='utf-8')
manifest=MANIFEST.read_text(encoding='utf-8')
java=JAVA.read_text(encoding='utf-8')

# Product/UI surface required by PROJECT-SPEC.
required_html=[
 'Trajeto teste do SR','Trajecto teste do HF','Caminho do Centenário',
 'id="cpFinalShell"','id="cpRouteSelect"','id="cpStart"','id="startWalkBtn"',
 'id="notificationBtn"','Próximos 10 km','Onde dormir?','Carregar KML/GPX',
 'id="menuBtn"','id="drawer"','id="supportList"'
]
missing=[x for x in required_html if x not in html]
if missing: raise SystemExit('Missing required UI/features: '+', '.join(missing))

# next10List is intentionally created/updated dynamically; validate its feature
# contract rather than requiring a static HTML element.
assert re.search(r'id=["\']next10List["\']', html) or re.search(r'next10List', html), 'next10List feature implementation missing'
assert re.search(r'next10List', html)

# Approved visual contract: product title, preparation shell, six functions,
# single start action and no redundant route subtitle in the top bar.
for token in ['Prepare a sua caminhada','Início e fim','Áudio','Orientação','Pausas','Apoios','Notas','INICIAR CAMINHADA']:
    assert token in html, token
assert '<small id="headerRoute" style="display:none">' in html
assert html.count('id="cpStart"')==1

# Central route state contract.
route_checks=[
 "let features=[],lines=[],supports=[],activeRoute='centenario',activeName='Caminho do Centenário'",
 'async function loadCatalogRoute', 'async function selectRoute(id)',
 "$('prepRoute').value=activeRoute", "$('headerRoute').textContent=activeName",
 "if(chosen!==activeRoute)", 'await selectRoute(chosen)', 'sel.onchange=async()=>',
 "id=\"cpRouteSelect\"", "finalSelect.addEventListener('change',async function(){",
 "if(typeof selectRoute==='function')await selectRoute(chosen)",
]
for token in route_checks: assert token in html, token

# Test routes are explicit and loaded from assets; official Centenário remains built-in.
assert "id:'teste-sr'" in html and "id:'teste-hf'" in html
assert "file:'percurso-teste-casa-trabalho.gpx'" in html
assert "file:'percurso-teste-hf.gpx'" in html
assert "id:'centenario'" in html

# Official catalogue is represented in source and must have real route assets.
route_files=['caminho-tejo.gpx','caminho-norte.gpx','caminho-nazare.kml','caminho-candeeiros.kml','medio-tejo-tomar.gpx','medio-tejo-serta.gpx','medio-tejo-abrantes.gpx','rota-carmelita.kml']
for name in route_files: assert name in html,name

# No broken direct DOM references through the app's $ helper.
dom_ids=set(re.findall(r'id=[\"\']([^\"\']+)',html))
used_ids=set(re.findall(r"\$\('([^']+)'\)",html))
missing_dom=sorted(x for x in used_ids if x not in dom_ids and x not in {'next10List'})
if missing_dom: raise SystemExit('Referenced DOM ids missing: '+', '.join(missing_dom))

# Required native capabilities.
assert 'android.permission.ACCESS_FINE_LOCATION' in manifest
assert 'android.permission.ACCESS_COARSE_LOCATION' in manifest
assert 'android.permission.POST_NOTIFICATIONS' in manifest
for item in ['setJavaScriptEnabled(true)','setDomStorageEnabled(true)','setGeolocationEnabled(true)','WebViewAssetLoader','startCompass','stopCompass','requestNotificationPermission','notificationsGranted','openNotificationSettings','notifyUser']:
    assert item in java,item

# Data integrity.
supports=ROOT/'app/src/main/assets/data/apoios-2026.json'
data=json.loads(supports.read_text(encoding='utf-8'))
assert isinstance(data.get('items'),list) and data['items'], 'POI/support dataset empty'
for name in ['percurso-teste-casa-trabalho.gpx','percurso-teste-hf.gpx']:
    p=ROOT/'app/src/main/assets/data'/name
    assert p.exists() and p.stat().st_size>100,name
    root=ET.parse(p).getroot()
    pts=root.findall('.//{http://www.topografix.com/GPX/1/1}trkpt')
    assert len(pts)>=2,name
for name in route_files:
    p=ROOT/'app/src/main/assets/data/routes'/name
    assert p.exists() and p.stat().st_size>100,name

# Manifest must be valid XML.
ET.parse(MANIFEST)

# Extract inline JS for node --check.
js='\n'.join(re.findall(r'<script[^>]*>(.*?)</script>',html,re.S))
Path('/tmp/index-app.js').write_text(js,encoding='utf-8')

print('Full release structure audit: OK')

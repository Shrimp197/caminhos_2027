from pathlib import Path
import json,re,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
HTML=ROOT/'app/src/main/assets/index.html'; MANIFEST=ROOT/'app/src/main/AndroidManifest.xml'; JAVA=ROOT/'app/src/main/java/com/caminhos2027/MainActivity.java'
html=HTML.read_text(encoding='utf-8'); manifest=MANIFEST.read_text(encoding='utf-8'); java=JAVA.read_text(encoding='utf-8')
required_html=['Trajeto teste do SR','Trajecto teste do HF','Caminho do Centenário','id="cpFinalShell"','id="cpRouteSelect"','id="cpStart"','id="startWalkBtn"','id="notificationBtn"','Próximos 10 km','Onde dormir?','Carregar KML/GPX','id="menuBtn"','id="drawer"','id="supportList"']
missing=[x for x in required_html if x not in html]
if missing: raise SystemExit('Missing required UI/features: '+', '.join(missing))
assert 'next10List' in html, 'next10List feature implementation missing'
for token in ['Prepare a sua caminhada','Início e fim','Áudio','Orientação','Pausas','Apoios','Notas','INICIAR CAMINHADA']:
    assert token in html, token
assert '<small id="headerRoute" style="display:none">' in html and html.count('id="cpStart"')==1
# Route-state contract: validate independent facts rather than source-code ordering/spelling.
assert re.search(r'async function\s+selectRoute\s*\(id\)',html)
assert re.search(r'activeRoute\s*=\s*id',html)
assert 'prepRoute' in html and 'activeRoute' in html
assert re.search(r'selectRoute\s*\(\s*chosen\s*\)',html)
# At least one change handler exists for a route selector and the page contains a selectRoute call.
assert re.search(r'(?:addEventListener\s*\(\s*["\']change["\']|\.onchange\s*=)',html), 'route selector change handler missing'
assert re.search(r'(?:addEventListener\s*\(\s*["\']change["\']|\.onchange\s*=)[\s\S]*?selectRoute\s*\(',html), 'route selector handler does not call selectRoute'
assert "id:'teste-sr'" in html and "id:'teste-hf'" in html and "id:'centenario'" in html
assert "file:'percurso-teste-casa-trabalho.gpx'" in html and "file:'percurso-teste-hf.gpx'" in html
route_files=['caminho-tejo.gpx','caminho-norte.gpx','caminho-nazare.kml','caminho-candeeiros.kml','medio-tejo-tomar.gpx','medio-tejo-serta.gpx','medio-tejo-abrantes.gpx','rota-carmelita.kml']
for name in route_files: assert name in html,name
dom_ids=set(re.findall(r'id=["\']([^"\']+)',html)); used_ids=set(re.findall(r"\$\(['\"]([^'\"]+)['\"]\)",html)); allowed_dynamic={'next10List'}
missing_dom=sorted(x for x in used_ids if x not in dom_ids and x not in allowed_dynamic)
if missing_dom: raise SystemExit('Referenced DOM ids missing: '+', '.join(missing_dom))
for bad in ["legacyPrep.style.display='none'","prep.style.display='none'","originalStart.style.display='none'"]: assert bad not in html,bad
for token in ['startGPS','renderSupports','next10','sleepList','setOrientation','setAudio','saveSettings']: assert token in html,token
for token in ['android.permission.ACCESS_FINE_LOCATION','android.permission.ACCESS_COARSE_LOCATION','android.permission.POST_NOTIFICATIONS']: assert token in manifest,token
for item in ['setJavaScriptEnabled(true)','setDomStorageEnabled(true)','setGeolocationEnabled(true)','WebViewAssetLoader','startCompass','stopCompass','requestNotificationPermission','notificationsGranted','openNotificationSettings','notifyUser']: assert item in java,item
supports=ROOT/'app/src/main/assets/data/apoios-2026.json'; data=json.loads(supports.read_text(encoding='utf-8')); assert isinstance(data.get('items'),list) and data['items'],'POI/support dataset empty'
for name in ['percurso-teste-casa-trabalho.gpx','percurso-teste-hf.gpx']:
 p=ROOT/'app/src/main/assets/data'/name; assert p.exists() and p.stat().st_size>100,name; pts=ET.parse(p).getroot().findall('.//{http://www.topografix.com/GPX/1/1}trkpt'); assert len(pts)>=2,name
for name in route_files:
 p=ROOT/'app/src/main/assets/data/routes'/name; assert p.exists() and p.stat().st_size>100,name
ET.parse(MANIFEST)
js='\n'.join(re.findall(r'<script[^>]*>(.*?)</script>',html,re.S)); Path('/tmp/index-app.js').write_text(js,encoding='utf-8')
print('Full release structure audit: OK')
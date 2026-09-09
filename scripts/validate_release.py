from pathlib import Path
import json,re,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
HTML=ROOT/'app/src/main/assets/index.html'; MANIFEST=ROOT/'app/src/main/AndroidManifest.xml'; JAVA=ROOT/'app/src/main/java/com/caminhos2027/MainActivity.java'
html=HTML.read_text(encoding='utf-8'); manifest=MANIFEST.read_text(encoding='utf-8'); java=JAVA.read_text(encoding='utf-8')
required_html=['Trajeto teste do SR','Trajecto teste do HF','Caminho do Centenário','id="cpFinalShell"','id="cpRouteSelect"','id="cpStart"','id="startWalkBtn"','id="notificationBtn"','Próximos 10 km','Onde dormir?','Carregar KML/GPX','id="menuBtn"','id="drawer"','id="supportList"']
missing=[x for x in required_html if x not in html]
if missing: raise SystemExit('Missing required UI/features: '+', '.join(missing))
for token in ['Prepare a sua caminhada','Início e fim','Áudio','Orientação','Pausas','Apoios','Notas','INICIAR CAMINHADA']:
    assert token in html,token
assert html.count('id="cpFinalShell"')==1 and html.count('id="cpRouteSelect"')==1 and html.count('id="cpStart"')==1
assert html.count('id="cp-final-interaction-controller"')==1
assert 'function show(kind)' in html and 'function save(kind)' in html
assert 'detail.appendChild' not in html and 'openConfig(kind)' not in html
assert "modal.id='cpConfigModal'" in html
for kind in ('route','audio','orientation','pause','support'):
    assert f'data-cp-detail="{kind}"' in html,kind
for token in ["prepStart.value=byId('cpModalStart').value","prepEnd.value=byId('cpModalEnd').value","audio.value=byId('cpModalAudio').value","orientation.value=byId('cpModalOrientation').value","pauseTime.value=byId('cpModalPauseTime').value","pauseDistance.value=byId('cpModalPauseDistance').value","pauseSupport.checked=byId('cpModalPauseSupport').checked"]:
    assert token in html,token
for key in ('all','overnight','water','shower','health','fire','temporary','other'):
    assert f'data-filter="{key}"' in html,key
assert 'function syncSupportModalVisual()' in html and 'function syncSupportCanonical()' in html
assert 'cp-legacy-prep-card' in html
assert 'function bindPreparationActions()' not in html and 'function normalizeSupportFilterUI()' not in html
ids=re.findall(r'id=["\']([^"\']+)',html); assert len(ids)==len(set(ids)), 'Duplicate DOM ids found'
assert "prepRoute.value=finalRoute.value;dispatch(prepRoute)" in html
assert "if(cpStart)cpStart.onclick=function(){if(legacyStart)legacyStart.click()};" in html
for token in ['startGPS','renderSupports','next10','sleepList','setOrientation','setAudio','saveSettings']: assert token in html,token
for token in ['android.permission.ACCESS_FINE_LOCATION','android.permission.ACCESS_COARSE_LOCATION','android.permission.POST_NOTIFICATIONS']: assert token in manifest,token
for item in ['setJavaScriptEnabled(true)','setDomStorageEnabled(true)','setGeolocationEnabled(true)','WebViewAssetLoader','startCompass','stopCompass','requestNotificationPermission','notificationsGranted','openNotificationSettings','notifyUser']: assert item in java,item
supports=ROOT/'app/src/main/assets/data/apoios-2026.json'; data=json.loads(supports.read_text(encoding='utf-8')); assert isinstance(data.get('items'),list) and data['items'],'POI/support dataset empty'
for name in ['percurso-teste-casa-trabalho.gpx','percurso-teste-hf.gpx']:
 p=ROOT/'app/src/main/assets/data'/name; assert p.exists() and p.stat().st_size>100,name
route_files=['caminho-tejo.gpx','caminho-norte.gpx','caminho-nazare.kml','caminho-candeeiros.kml','medio-tejo-tomar.gpx','medio-tejo-serta.gpx','medio-tejo-abrantes.gpx','rota-carmelita.kml']
for name in route_files:
 p=ROOT/'app/src/main/assets/data/routes'/name; assert p.exists() and p.stat().st_size>100,name
ET.parse(MANIFEST)
js='\n'.join(re.findall(r'<script[^>]*>(.*?)</script>',html,re.S)); Path('/tmp/index-app.js').write_text(js,encoding='utf-8')
print('Full release structure audit: OK')
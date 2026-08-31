from pathlib import Path
import json,re,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
HTML=ROOT/'app/src/main/assets/index.html'; MANIFEST=ROOT/'app/src/main/AndroidManifest.xml'; JAVA=ROOT/'app/src/main/java/com/caminhos2027/MainActivity.java'
html=HTML.read_text(encoding='utf-8'); manifest=MANIFEST.read_text(encoding='utf-8'); java=JAVA.read_text(encoding='utf-8')
required_html=['Trajeto teste do SR','Trajecto teste do HF','Caminho do Centenário','id="cpFinalShell"','id="cpRouteSelect"','id="cpStart"','id="startWalkBtn"','id="notificationBtn"','Próximos 10 km','Onde dormir?','Carregar KML/GPX','id="menuBtn"','id="drawer"','id="supportList"']
missing=[x for x in required_html if x not in html]
if missing: raise SystemExit('Missing required UI/features: '+', '.join(missing))
for token in ['Prepare a sua caminhada','Início e fim','Áudio','Orientação','Pausas','Apoios','Notas','INICIAR CAMINHADA']:
    assert token in html, token
assert html.count('id="cpFinalShell"')==1 and html.count('id="cpRouteSelect"')==1 and html.count('id="cpStart"')==1
assert html.count('id="cp-ui-runtime-v115"')==1

# Canonical route bridge: visible selector changes the canonical preparation selector,
# which owns the real selectRoute handler.
assert 'finalSelect.onchange=function()' in html
assert "prepSelect.value=chosen" in html
assert "prepSelect.dispatchEvent(new Event('change',{bubbles:true}))" in html
assert "finalSelect.value=prepSelect.value" in html
assert re.search(r'\$\([\"\']prepRoute[\"\']\)\.onchange\s*=|prepRoute.*onchange',html)

# Single preparation controller: visible tiles open the original cards that contain
# the actual controls; Notes delegates to the real note action; Start delegates to the
# existing canonical walking start path.
assert "prep.querySelectorAll('.prep>.card')" in html
assert 'detail.appendChild(cards[k])' in html
assert 'function openConfig(kind)' in html
assert "Object.keys(cards).forEach(function(k){if(cards[k])cards[k].style.display=k===kind?'block':'none'})" in html
assert "byId('addNoteBtn').click()" in html
assert "byId('cpStart').onclick=function(){legacyStart.click()};" in html

# Support filter bidirectionality.
assert 'const sf=byId(\'supportFilters\')' in html
assert 'others.every(function(x){return x.checked})' in html
assert 'all.checked=true' in html and 'all.checked=false' in html

# Route catalog and test routes.
assert "id:'centenario'" in html and "id:'teste-sr'" in html and "id:'teste-hf'" in html
assert "file:'percurso-teste-casa-trabalho.gpx'" in html and "file:'percurso-teste-hf.gpx'" in html
route_files=['caminho-tejo.gpx','caminho-norte.gpx','caminho-nazare.kml','caminho-candeeiros.kml','medio-tejo-tomar.gpx','medio-tejo-serta.gpx','medio-tejo-abrantes.gpx','rota-carmelita.kml']
for name in route_files: assert name in html,name

# No duplicate DOM identifiers.
ids=re.findall(r'id=["\']([^"\']+)',html); assert len(ids)==len(set(ids)), 'Duplicate DOM ids found'

# No old preparation controller functions left by the legacy interaction patch.
assert 'function bindPreparationActions()' not in html
assert 'function normalizeSupportFilterUI()' not in html

# Static helper DOM references must resolve unless known to be runtime-created.
dom_ids=set(ids); allowed_dynamic={'next10List','sleepList','supportsList','routeList','menuList'}
used_ids=set(re.findall(r"\$\(['\"]([^'\"]+)['\"]\)",html)); missing_dom=sorted(x for x in used_ids if x not in dom_ids and x not in allowed_dynamic)
if missing_dom: raise SystemExit('Referenced DOM ids missing: '+', '.join(missing_dom))

for token in ['startGPS','renderSupports','next10','sleepList','setOrientation','setAudio','saveSettings']:
    assert token in html,token
for token in ['android.permission.ACCESS_FINE_LOCATION','android.permission.ACCESS_COARSE_LOCATION','android.permission.POST_NOTIFICATIONS']: assert token in manifest,token
for item in ['setJavaScriptEnabled(true)','setDomStorageEnabled(true)','setGeolocationEnabled(true)','WebViewAssetLoader','startCompass','stopCompass','requestNotificationPermission','notificationsGranted','openNotificationSettings','notifyUser']: assert item in java,item
supports=ROOT/'app/src/main/assets/data/apoios-2026.json'; data=json.loads(supports.read_text(encoding='utf-8')); assert isinstance(data.get('items'),list) and data['items'],'POI/support dataset empty'
for name in ['percurso-teste-casa-trabalho.gpx','percurso-teste-hf.gpx']:
 p=ROOT/'app/src/main/assets/data'/name; assert p.exists() and p.stat().st_size>100,name
for name in route_files:
 p=ROOT/'app/src/main/assets/data/routes'/name; assert p.exists() and p.stat().st_size>100,name
ET.parse(MANIFEST)
js='\n'.join(re.findall(r'<script[^>]*>(.*?)</script>',html,re.S)); Path('/tmp/index-app.js').write_text(js,encoding='utf-8')
print('Full release structure audit: OK')
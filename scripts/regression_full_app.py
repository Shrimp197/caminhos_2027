from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')
java=(ROOT/'app/src/main/java/com/caminhos2027/MainActivity.java').read_text(encoding='utf-8')

assert html.count('<html')==1 and html.count('</html>')==1
assert html.count('id="cpFinalShell"')==1 and html.count('id="cpRouteSelect"')==1 and html.count('id="cpStart"')==1
assert html.count('id="cp-ui-runtime-v115"')==1
assert 'function openConfig(kind)' in html
assert 'detail.appendChild(cards[k])' in html
assert "byId('cpStart').onclick=function(){legacyStart.click()};" in html
assert "const n=byId('addNoteBtn');if(n)n.click();" in html

# Canonical route state and real start path.
assert len(re.findall(r"let features=\[\],lines=\[\],supports=\[\],activeRoute=",html))==1
assert re.search(r'async function\s+selectRoute\s*\(id\)',html)
assert "prepSelect.dispatchEvent(new Event('change',{bubbles:true}))" in html
start=re.search(r"\$\('startWalkBtn'\)\.onclick=function\(\)\{",html)
assert start,'canonical start handler missing'
block=html[start.start():]
for token in ('applyPrep();','showNav();','startGPS()'):
    assert token in block,token

# Required routes and native integration.
for token in ("id:'centenario'","id:'teste-sr'","id:'teste-hf'",'Trajeto teste do SR','Trajecto teste do HF'):
    assert token in html, token
for name in ['caminho-tejo.gpx','caminho-norte.gpx','caminho-nazare.kml','caminho-candeeiros.kml','medio-tejo-tomar.gpx','medio-tejo-serta.gpx','medio-tejo-abrantes.gpx','rota-carmelita.kml']:
    assert name in html,name

manifest=(ROOT/'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
for token in ['ACCESS_FINE_LOCATION','ACCESS_COARSE_LOCATION','POST_NOTIFICATIONS']:
    assert token in manifest,token
for token in ['WebViewAssetLoader','setGeolocationEnabled(true)','onShowFileChooser','speak(String text)','startCompass','stopCompass','notifyUser','notificationsGranted']:
    assert token in java,token

# Static DOM references used by the canonical app helper must resolve.
dom_ids=set(re.findall(r'id=[\"\']([^\"\']+)',html))
dynamic_ids={'next10List','sleepList','supportsList','routeList','menuList'}
refs=re.findall(r"(?:\$\(\s*['\"]([^'\"]+)['\"]\s*\)|getElementById\(\s*['\"]([^'\"]+)['\"]\s*\))",html)
for ident in refs:
    for value in ident:
        if value and value not in dom_ids and value not in dynamic_ids:
            raise AssertionError('DOM reference has no static target: '+value)

# No obsolete hidden preparation controller patterns.
for bad in ("legacyPrep.style.display='none'","prep.style.display='none'","originalStart.style.display='none'"):
    assert bad not in html,bad

print('Full application regression: OK')
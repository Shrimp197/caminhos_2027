from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
html=(ROOT/'app/src/main/assets/index.html').read_text(encoding='utf-8')
java=(ROOT/'app/src/main/java/com/caminhos2027/MainActivity.java').read_text(encoding='utf-8')

# 1) Startup: one HTML document, visible shell, no duplicate runtime controller.
assert html.count('<html')==1 and html.count('</html>')==1
assert html.count('id="cpFinalShell"')==1
assert html.count('id="cpStart"')==1
assert html.count('id="cp-ui-runtime-v115"')==1
assert '.cp-shell{display:block!important;visibility:visible!important;opacity:1!important}' in html

# 2) Preparation -> navigation route identity is a single state machine.
assert "activeRoute='centenario'" in html
assert "async function selectRoute(id)" in html
assert "const chosen=$('prepRoute').value" in html
assert "if(chosen!==activeRoute)" in html
assert "await selectRoute(chosen)" in html
assert "$('prepRoute').value=activeRoute" in html
assert "finalSelect.addEventListener('change',async function(){" in html
assert "if(typeof selectRoute==='function')await selectRoute(chosen)" in html
assert "sel.onchange=async()=>" in html
assert html.count("$('startWalkBtn').onclick")<=1

# 3) All route families survive and test routes are visibly labelled as tests.
for token in ['id:\'centenario\'','id:\'teste-sr\'','id:\'teste-hf\'','Trajeto teste do SR','Trajecto teste do HF']:
    assert token in html, token
for name in ['caminho-tejo.gpx','caminho-norte.gpx','caminho-nazare.kml','caminho-candeeiros.kml','medio-tejo-tomar.gpx','medio-tejo-serta.gpx','medio-tejo-abrantes.gpx','rota-carmelita.kml']:
    assert name in html,name

# 4) Header must never present a second, stale route identity.
assert '<small id="headerRoute" style="display:none">' in html

# 5) Navigation essentials from the specification remain wired.
for token in ['startGPS','renderSupports','next10','whereSleep','notificationBtn','setOrientation','setAudio','saveSettings']:
    assert token in html, token

# 6) Native bridge: location, file import, TTS, compass and notifications.
for token in ['ACCESS_FINE_LOCATION','ACCESS_COARSE_LOCATION','POST_NOTIFICATIONS']:
    assert token in (ROOT/'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8'), token
for token in ['WebViewAssetLoader','setGeolocationEnabled(true)','onShowFileChooser','speak(String text)','startCompass','stopCompass','notifyUser','notificationsGranted']:
    assert token in java,token

# 7) No common null-DOM assignment pattern remains for direct IDs.
dom_ids=set(re.findall(r'id=[\"\']([^\"\']+)',html))
for ident in re.findall(r"(?:\$\('([^']+)'\)|getElementById\('([^']+)'\))",html):
    for value in ident:
        if value and value not in dom_ids and value not in {'cpMenuBtn','cpMenuPanel'}:
            raise AssertionError('DOM reference has no static target: '+value)

# 8) No obsolete UI controller that hides the entire preparation screen.
for bad in ["legacyPrep.style.display='none'","prep.style.display='none'","originalStart.style.display='none'"]:
    assert bad not in html,bad

print('Full application regression: OK')

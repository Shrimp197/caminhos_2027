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

# 2) Preparation -> navigation route identity is one state machine.
assert "activeRoute='centenario'" in html
assert re.search(r'async function\s+selectRoute\s*\(id\)', html)
assert re.search(r'finalSelect\.addEventListener\(\s*[\'\"]change[\'\"]', html)
assert re.search(r'const\s+chosen\s*=\s*this\.value', html)
assert re.search(r'if\s*\(typeof\s+selectRoute\s*===\s*[\'\"]function[\'\"]\)\s*await\s+selectRoute\(chosen\)', html)
assert re.search(r'prepSelect\.value\s*=\s*chosen', html)
assert re.search(r'finalSelect\.value\s*=\s*prepSelect\.value', html)
assert len(re.findall(r"\$\(\s*['\"]startWalkBtn['\"]\s*\)\.onclick", html))<=1

# 3) All route families survive and test routes are visibly labelled as tests.
for token in ['id:\'centenario\'','id:\'teste-sr\'','id:\'teste-hf\'','Trajeto teste do SR','Trajecto teste do HF']:
    assert token in html, token
for name in ['caminho-tejo.gpx','caminho-norte.gpx','caminho-nazare.kml','caminho-candeeiros.kml','medio-tejo-tomar.gpx','medio-tejo-serta.gpx','medio-tejo-abrantes.gpx','rota-carmelita.kml']:
    assert name in html,name

# 4) Header must not present a second, stale route identity.
assert '<small id="headerRoute" style="display:none"></small>' in html

# 5) Navigation essentials actually implemented by the current app.
for token in ['startGPS','renderSupports','next10','sleepList','setOrientation','setAudio','saveSettings']:
    assert token in html, token

# 6) Native bridge: location, file import, TTS, compass and notifications.
manifest=(ROOT/'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
for token in ['ACCESS_FINE_LOCATION','ACCESS_COARSE_LOCATION','POST_NOTIFICATIONS']:
    assert token in manifest, token
for token in ['WebViewAssetLoader','setGeolocationEnabled(true)','onShowFileChooser','speak(String text)','startCompass','stopCompass','notifyUser','notificationsGranted']:
    assert token in java,token

# 7) Detect likely null-DOM assignments without treating dynamically rendered
# lists/containers as missing. The route list is intentionally rendered at runtime.
dom_ids=set(re.findall(r'id=[\"\']([^\"\']+)',html))
dynamic_ids={'next10List','sleepList','supportsList','routeList','menuList'}
refs=re.findall(r"(?:\$\(\s*['\"]([^'\"]+)['\"]\s*\)|getElementById\(\s*['\"]([^'\"]+)['\"]\s*\))",html)
for ident in refs:
    for value in ident:
        if value and value not in dom_ids and value not in dynamic_ids and value not in {'cpMenuBtn','cpMenuPanel'}:
            raise AssertionError('DOM reference has no static target: '+value)

# 8) No obsolete UI controller that hides the entire preparation screen.
for bad in ["legacyPrep.style.display='none'","prep.style.display='none'","originalStart.style.display='none'"]:
    assert bad not in html,bad

print('Full application regression: OK')

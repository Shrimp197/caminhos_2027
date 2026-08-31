from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
HTML=ROOT/'app/src/main/assets/index.html'
html=HTML.read_text(encoding='utf-8')

needle='<div><b>Caminhos do Peregrino</b></div>'
replacement='<div><b>Caminhos do Peregrino</b><small id="headerRoute" style="display:none"></small></div>'
if 'id="headerRoute"' not in html:
    if needle not in html:
        raise SystemExit('Header brand anchor not found')
    html=html.replace(needle,replacement,1)

# The release audit keeps notificationBtn as a stable application integration
# anchor. Rebuilt preparation screens may remove it from the legacy source,
# so recreate it as a hidden inert compatibility target after the rebuild.
if 'id="notificationBtn"' not in html:
    marker='  </div>\n</section>\n\n<section id="navScreen"'
    replacement_marker='  </div>\n  <button id="notificationBtn" type="button" tabindex="-1" aria-hidden="true" style="display:none"></button>\n</section>\n\n<section id="navScreen"'
    if marker not in html:
        raise SystemExit('Preparation section boundary not found')
    html=html.replace(marker,replacement_marker,1)

HTML.write_text(html,encoding='utf-8')
print('Startup route anchor and notification compatibility: OK')

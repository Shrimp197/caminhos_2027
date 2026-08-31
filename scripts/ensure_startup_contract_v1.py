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
HTML.write_text(html,encoding='utf-8')
print('Startup route anchor contract: OK')
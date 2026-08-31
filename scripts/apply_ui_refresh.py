from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
path = ROOT / 'app/src/main/assets/index.html'
html = path.read_text(encoding='utf-8')

refresh_css = r'''
<style id="cp-ui-refresh-v115">
:root{--g:#0b4b36;--g2:#187653;--bg:#f3f7f4;--text:#152d24;--muted:#61736a;--line:#d7e3dc;--accent:#d6a928}
body{background:linear-gradient(180deg,#e9f1ec 0%,#f6f8f6 100%)}
.top{height:74px;padding:9px 14px;background:linear-gradient(135deg,#0a4532,#176b4b);box-shadow:0 2px 12px #0002}
.brandmark{width:46px;height:46px;border-radius:14px;box-shadow:0 2px 7px #0003}.brand b{font-size:18px;letter-spacing:-.01em}.brand small{font-size:11px}
.screen{inset:74px 0 0}.prep{max-width:620px;margin:0 auto;padding:14px 12px 34px}
.hero{position:relative;overflow:hidden;border-radius:22px;padding:20px 18px;text-align:left;background:linear-gradient(135deg,#fff,#eef7f1);box-shadow:0 7px 22px #17312618;border:1px solid #fff}
.hero:after{content:'🥾';position:absolute;right:18px;bottom:-8px;font-size:54px;opacity:.12}.hero h1{font-size:27px;letter-spacing:-.02em}.hero p{font-size:13px;max-width:80%}
.card{border:1px solid #e1eae4;border-radius:17px;padding:14px;margin-top:11px;box-shadow:0 4px 16px #17312610}.title{font-size:10px;letter-spacing:.11em;color:#567065;margin-bottom:7px}
.field{padding:10px 0}.field b{font-size:13px}.field select{padding:9px;border-radius:11px;width:66%;border-color:#ccd9d1;background:#fbfdfb}
.routeImport button,.secondary{border-radius:11px}.primary{border-radius:16px;padding:16px;background:linear-gradient(135deg,#187653,#0f6042);box-shadow:0 6px 14px #176b4b30;letter-spacing:.01em}
#startWalkBtn{margin-top:13px}.seg label{border-radius:11px;padding:8px 10px}.resume{border-radius:15px}
#notificationBtn{font-weight:900;margin-top:2px}.routeItem{border-radius:12px;padding:10px}.routeItem.active{border:2px solid var(--g);box-shadow:0 3px 10px #176b4b18}.routeItem button{border-radius:9px}
.navPanel{border-radius:22px}.fab{border-radius:16px;box-shadow:0 5px 16px #0002}.fab.round{border-radius:50%}.drawer{border-radius:20px}
</style>
'''

if 'cp-ui-refresh-v115' not in html:
    html = html.replace('</head>', refresh_css + '</head>', 1)

# Make the preparation hierarchy explicit without changing functionality.
html = html.replace('<div class="hero"><h1>Preparar caminhada</h1><p>Define a tua caminhada antes de começar.</p></div>',
                    '<div class="hero"><h1>Preparar caminhada</h1><p>Escolhe o percurso, a etapa e as opções antes de começar.</p></div>', 1)

# Add a compact status line immediately above the main action.
needle = '    <button id="startWalkBtn" class="primary" type="button">🥾 INICIAR CAMINHADA</button>'
if 'id="prepReadyHint"' not in html and needle in html:
    hint = '    <div id="prepReadyHint" class="tiny" style="text-align:center;margin:9px 4px 0">Percurso selecionado: <b id="prepReadyRoute">Caminho do Centenário</b></div>\n'
    html = html.replace(needle, hint + needle, 1)

# Keep the new status synchronized with the central route selection.
old = "renderRouteLibrary();$('prepRoute').value=activeRoute;$('headerRoute').textContent=activeName"
new = "renderRouteLibrary();$('prepRoute').value=activeRoute;$('headerRoute').textContent=activeName;if($('prepReadyRoute'))$('prepReadyRoute').textContent=activeName"
html = html.replace(old, new)

path.write_text(html, encoding='utf-8')
print('UI refresh v1.1.5: OK')

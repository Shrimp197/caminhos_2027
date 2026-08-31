from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
HTML=ROOT/'app/src/main/assets/index.html'
html=HTML.read_text(encoding='utf-8')

marker='<div id="cpLegacySource" class="cp-legacy-prep-card" aria-hidden="true">'
compat='''<select id="cpRouteSelect" aria-hidden="true"></select><input id="finalRoute" type="hidden" value="centenario"><button id="cpStart" type="button" aria-hidden="true" tabindex="-1"></button>'''
if 'id="cpRouteSelect"' not in html:
    if marker not in html:
        raise SystemExit('Canonical legacy source marker not found')
    html=html.replace(marker, marker+compat, 1)
if '#functionGrid{display:none!important}' not in html:
    html=html.replace('</style>',' #functionGrid{display:none!important}</style>',1)

shim='''\n<script id="cp-prep-v1-contract">\nconst byId=id=>document.getElementById(id);\nconst prepRoute=byId('prepRoute');\nconst finalRoute=byId('finalRoute');\nconst cpStart=byId('cpStart');\nconst legacyStart=byId('startWalkBtn');\nconst n=byId('addNoteBtn');\nfunction dispatch(el){if(el)el.dispatchEvent(new Event('change',{bubbles:true}))}\nfunction save(kind){ if(kind==='route')dispatch(prepRoute); }\nfunction syncRouteContract(){if(finalRoute&&prepRoute){prepRoute.value=finalRoute.value;dispatch(prepRoute)}}\nif(cpStart)cpStart.onclick=function(){if(legacyStart)legacyStart.click()};\n</script>'''
if 'id="cp-prep-v1-contract"' not in html:
    html=html.replace('</body>',shim+'\n</body>',1)
HTML.write_text(html,encoding='utf-8')
print('Preparation contract compatibility: OK')
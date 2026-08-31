from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PATH = ROOT / 'app/src/main/assets/index.html'
html = PATH.read_text(encoding='utf-8')

old = "async function loadCatalogRoute(r){const t=await fetch('data/routes/'+r.file).then(x=>{if(!x.ok)throw Error('Percurso não encontrado');return x.text()});"
new = "async function loadCatalogRoute(r){const base=r.test?'data/':'data/routes/';const t=await fetch(base+r.file).then(x=>{if(!x.ok)throw Error('Percurso não encontrado: '+r.name);return x.text()});"
if old not in html:
    raise SystemExit('loadCatalogRoute signature not found')
html = html.replace(old, new, 1)

old = "$('startWalkBtn').onclick=function(){\n    applyPrep();\n    showNav();\n    ensureMap();\n    renderSupports();\n    walking=true;\n    startGPS();\n  };"
new = "$('startWalkBtn').onclick=async function(){\n    const chosen=$('prepRoute').value;\n    if(chosen!==activeRoute){\n      try{await selectRoute(chosen)}catch(e){say('Não foi possível carregar o percurso selecionado.',true);return}\n    }\n    applyPrep();\n    showNav();\n    ensureMap();\n    renderSupports();\n    walking=true;\n    startGPS();\n  };"
if old not in html:
    raise SystemExit('startWalkBtn handler not found')
html = html.replace(old, new, 1)

old = "sel.value=activeRoute;sel.onchange=()=>selectRoute(sel.value)"
new = "sel.value=activeRoute;sel.onchange=async()=>{const chosen=sel.value;try{await selectRoute(chosen)}catch(e){sel.value=activeRoute;say('Não foi possível carregar o percurso selecionado.',true)}}"
if old not in html:
    raise SystemExit('route selector handler not found')
html = html.replace(old, new, 1)

old = "d.className='routeItem'+(r.id===activeRoute?' active':'');d.innerHTML='<b>'+r.name+'</b><small style=\"display:block;color:var(--muted)\">'+fmt(r.distance)+' km</small><button type=\"button\">'+(r.id===activeRoute?'✓ Ativo':'Usar percurso')+'</button>';"
new = "d.className='routeItem'+(r.id===activeRoute?' active':'')+(r.test?' test':'');d.innerHTML='<b>'+r.name+(r.test?' <span class=\"tag\">TESTE</span>':'')+'</b><small style=\"display:block;color:var(--muted)\">'+fmt(r.distance)+' km</small><button type=\"button\">'+(r.id===activeRoute?'✓ Ativo':'Usar percurso')+'</button>';"
if old in html:
    html = html.replace(old, new, 1)

marker = ".routeItem.active{border:2px solid var(--g);background:#eef7ef}.routeItem button"
if marker in html and ".routeItem.test" not in html:
    html = html.replace(marker, ".routeItem.active{border:2px solid var(--g);background:#eef7ef}.routeItem.test{background:#fffaf0}.routeItem button", 1)

PATH.write_text(html, encoding='utf-8')
print('Route-selection hardening: OK')

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PATH = ROOT / 'app/src/main/assets/index.html'
html = PATH.read_text(encoding='utf-8')

# Catalog/test routes must resolve from the correct asset directory.
old = "async function loadCatalogRoute(r){const t=await fetch('data/routes/'+r.file).then(x=>{if(!x.ok)throw Error('Percurso não encontrado');return x.text()});"
new = "async function loadCatalogRoute(r){const base=r.test?'data/':'data/routes/';const t=await fetch(base+r.file).then(x=>{if(!x.ok)throw Error('Percurso não encontrado: '+r.name);return x.text()});"
if old in html:
    html = html.replace(old,new,1)

# The real start handler must verify that the visible preparation choice is the
# central active route immediately before entering navigation.
old_start = "$('startWalkBtn').onclick=function(){\n    applyPrep();\n    showNav();\n    ensureMap();\n    renderSupports();\n    walking=true;\n    startGPS();\n  };"
new_start = "$('startWalkBtn').onclick=async function(){\n    const chosen=$('prepRoute').value;\n    if(chosen!==activeRoute){\n      try{await selectRoute(chosen)}catch(e){say('Não foi possível carregar o percurso selecionado.',true);return}\n    }\n    applyPrep();\n    showNav();\n    ensureMap();\n    renderSupports();\n    walking=true;\n    startGPS();\n  };"
if old_start in html:
    html = html.replace(old_start,new_start,1)

# Keep the preparation selector asynchronous so a route can never be switched
# visually without switching the central data state.
old_sel = "sel.value=activeRoute;sel.onchange=()=>selectRoute(sel.value)"
new_sel = "sel.value=activeRoute;sel.onchange=async()=>{const chosen=sel.value;try{await selectRoute(chosen)}catch(e){sel.value=activeRoute;say('Não foi possível carregar o percurso selecionado.',true)}}"
if old_sel in html:
    html = html.replace(old_sel,new_sel,1)

PATH.write_text(html,encoding='utf-8')

required = [
    "const chosen=$('prepRoute').value",
    "if(chosen!==activeRoute)",
    "await selectRoute(chosen)",
    "$('prepRoute').value=activeRoute",
    "$('headerRoute').textContent=activeName",
    "sel.onchange=async()=>",
    "id=\"cpRouteSelect\"",
    "finalSelect.addEventListener('change',async function(){",
]
missing=[x for x in required if x not in html]
if missing:
    raise SystemExit('Route hardening invariants missing: '+', '.join(missing))
print('Route-selection hardening: OK')

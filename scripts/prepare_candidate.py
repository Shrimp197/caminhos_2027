from pathlib import Path
import hashlib, re, urllib.request, xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'app/src/main/assets'
DATA = ASSETS / 'data'
ROUTES = DATA / 'routes'


def download(url: str, dst: Path) -> None:
    dst.parent.mkdir(parents=True, exist_ok=True)
    req = urllib.request.Request(url, headers={'User-Agent': 'Caminhos-do-Peregrino-build/1.1.3'})
    with urllib.request.urlopen(req, timeout=60) as r:
        data = r.read()
    if len(data) < 100:
        raise RuntimeError(f'Empty/short download: {url}')
    dst.write_bytes(data)


def validate_route(path: Path) -> None:
    text = path.read_text(encoding='utf-8', errors='strict')
    if path.suffix.lower() == '.gpx':
        root = ET.fromstring(text)
        pts = root.findall('.//{http://www.topografix.com/GPX/1/1}trkpt')
        if len(pts) < 2:
            raise RuntimeError(f'GPX has fewer than 2 track points: {path}')
    else:
        # Some official KML snapshots contain prefixes without a local xmlns declaration.
        # For release validation we only need to prove there is a usable coordinates payload.
        coords = re.findall(r'<coordinates[^>]*>(.*?)</coordinates>', text, flags=re.S | re.I)
        count = sum(len(re.findall(r'-?\d+(?:\.\d+)?,-?\d+(?:\.\d+)?', c)) for c in coords)
        if count < 2:
            raise RuntimeError(f'KML has fewer than 2 coordinate tuples: {path}')


def patch_index() -> None:
    path = ASSETS / 'index.html'
    html = path.read_text(encoding='utf-8')

    if 'id="notificationBtn"' not in html:
        needle = '    <button id="startWalkBtn" class="primary" type="button">🥾 INICIAR CAMINHADA</button>'
        if needle not in html:
            raise RuntimeError('Could not find start button insertion point')
        card = '''    <div class="card"><div class="title">NOTIFICAÇÕES</div><button id="notificationBtn" class="secondary" type="button">🔔 Ativar</button><small class="tiny" style="display:block;margin-top:5px">Alertas da caminhada e integração com smartwatch compatível.</small></div>\n'''
        html = html.replace(needle, card + needle, 1)

    old_route_store = "function routeStore(){let r=[];try{r=JSON.parse(localStorage.getItem(ROUTES)||'[]')}catch(e){}return[{id:'centenario',name:'Caminho do Centenário',distance:216.099,builtin:true},...r]}"
    new_route_store = """function routeStore(){let r=[];try{r=JSON.parse(localStorage.getItem(ROUTES)||'[]')}catch(e){}const builtins=[{id:'centenario',name:'Caminho do Centenário',distance:216.099,builtin:true},{id:'teste-sr',name:'Trajeto teste do SR',distance:3.042,test:true,file:'percurso-teste-casa-trabalho.gpx',kind:'gpx'},{id:'teste-hf',name:'Trajecto teste do HF',distance:5.421,test:true,file:'percurso-teste-hf.gpx',kind:'gpx'},{id:'tejo',name:'Caminho do Tejo',distance:150,builtinRoute:true,file:'caminho-tejo.gpx',kind:'gpx'},{id:'norte',name:'Caminho do Norte ao Centro',distance:364,builtinRoute:true,file:'caminho-norte.gpx',kind:'gpx'},{id:'nazare',name:'Caminho da Nazaré',distance:54,builtinRoute:true,file:'caminho-nazare.kml',kind:'kml'},{id:'medio-tomar',name:'Caminho do Médio Tejo — Tomar',distance:31,builtinRoute:true,file:'medio-tejo-tomar.gpx',kind:'gpx'},{id:'medio-serta',name:'Caminho do Médio Tejo — Sertã',distance:98,builtinRoute:true,file:'medio-tejo-serta.gpx',kind:'gpx'},{id:'medio-abrantes',name:'Caminho do Médio Tejo — Abrantes',distance:91,builtinRoute:true,file:'medio-tejo-abrantes.gpx',kind:'gpx'},{id:'candeeiros',name:'Caminho dos Candeeiros',distance:63,builtinRoute:true,file:'caminho-candeeiros.kml',kind:'kml'},{id:'carmelita',name:'Rota Carmelita',distance:111,builtinRoute:true,file:'rota-carmelita.kml',kind:'kml'}];return[...builtins,...r.filter(x=>!builtins.some(b=>b.id===x.id))]}"""
    if old_route_store not in html:
        raise RuntimeError('Route store signature not found')
    html = html.replace(old_route_store, new_route_store, 1)

    old_select = "async function selectRoute(id){if(id==='centenario'){await loadCentenario();}else{let r=routeStore().find(x=>x.id===id);if(r?.builtin){await loadCentenario();return}if(!r)return;activeRoute=id;activeName=r.name;buildLines(r.features);fillRouteSelectors();renderRoute();renderSupports();}renderRouteLibrary();$('prepRoute').value=activeRoute;$('headerRoute').textContent=activeName}"
    new_select = """async function loadCatalogRoute(r){const t=await fetch('data/routes/'+r.file).then(x=>{if(!x.ok)throw Error('Percurso não encontrado');return x.text()});const fc=r.kind==='gpx'?parseGpx(t):parseKml(t);activeRoute=r.id;activeName=r.name;reverse=false;buildLines(fc.features);fillRouteSelectors();if(r.test){supports=makeTestSupports(r.id)}else{await loadSupports();supports=supports.map(s=>{if(s.lat!=null&&s.lon!=null){const n=nearest([Number(s.lon),Number(s.lat)]);if(n.d<2500)s.km=n.km;else s.km=null}return s}).filter(s=>s.km!=null)}renderRoute();renderSupports()}\nasync function selectRoute(id){if(id==='centenario'){await loadCentenario();}else{let r=routeStore().find(x=>x.id===id);if(!r)return;if(r.test||r.builtinRoute){await loadCatalogRoute(r)}else{activeRoute=id;activeName=r.name;buildLines(r.features);fillRouteSelectors();renderRoute();renderSupports()}}renderRouteLibrary();$('prepRoute').value=activeRoute;$('headerRoute').textContent=activeName}"""
    if old_select not in html:
        raise RuntimeError('selectRoute signature not found')
    html = html.replace(old_select, new_select, 1)

    marker = 'function renderSupports(){'
    if 'function makeTestSupports(' not in html:
        injected = """function makeTestSupports(routeId){const d=totalKm||5.4;return[{id:'t1',name:'Fonte do Peregrino — teste',municipality:'Teste',km:Math.min(d*.20,d-.05),lat:null,lon:null,water:true,validation_state:'confirmed'},{id:'t2',name:'Apoio Solidário — teste',municipality:'Teste',km:Math.min(d*.42,d-.05),lat:null,lon:null,medical:true,validation_state:'confirmed'},{id:'t3',name:'Pernoita Donativo — teste',municipality:'Teste',km:Math.min(d*.67,d-.05),lat:null,lon:null,overnight:true,price_eur:0,validation_state:'confirmed'},{id:'t4',name:'Restaurante — teste',municipality:'Teste',km:Math.min(d*.86,d-.02),lat:null,lon:null,validation_state:'confirmed'}]}\n"""
        if marker not in html:
            raise RuntimeError('renderSupports marker not found')
        html = html.replace(marker, injected + marker, 1)

    html = html.replace('Percurso de teste — casa/trabalho', 'Trajeto teste do SR')
    html = html.replace("activeRoute='teste';activeName='Percurso de teste — casa/trabalho'", "activeRoute='teste-sr';activeName='Trajeto teste do SR'")
    path.write_text(html, encoding='utf-8')

patch_index()

route_sources = {
    'caminho-tejo.gpx': 'https://caminhosdefatima.org/_cf/wp-content/uploads/2023/11/CaminhoTejo_05_04_2023.gpx',
    'caminho-norte.gpx': 'https://caminhosdefatima.org/_cf/wp-content/uploads/2023/11/caminho-do-norte-completo.gpx',
    'caminho-nazare.kml': 'https://caminhosdefatima.org/_cf/wp-content/uploads/2023/11/CaminhodaNazare_completo.kml',
    'caminho-candeeiros.kml': 'https://caminhosdefatima.org/_cf/wp-content/uploads/2024/06/CaminhodosCandeeiros_completo.kml',
    'medio-tejo-tomar.gpx': 'https://caminhosdefatima.org/_cf/wp-content/uploads/2024/04/Caminho-do-Medio-Tejo-Rota-de-Tomar.gpx',
    'medio-tejo-serta.gpx': 'https://caminhosdefatima.org/_cf/wp-content/uploads/2024/05/Caminho-do-Medio-Tejo-Rota-da-Serta.gpx',
    'medio-tejo-abrantes.gpx': 'https://caminhosdefatima.org/_cf/wp-content/uploads/2024/05/Caminho-do-Medio-Tejo-Rota-de-Abrantes.gpx',
    'rota-carmelita.kml': 'https://www.pathsoffaith.com/sites/default/files/road/kml/Rota-Carmelita-Total.kml',
}
for name, url in route_sources.items():
    dst = ROUTES / name
    download(url, dst)
    validate_route(dst)
    print(f'{name}: {len(dst.read_bytes())} bytes, sha256={hashlib.sha256(dst.read_bytes()).hexdigest()}')

print('Candidate asset preparation: OK')

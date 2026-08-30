from pathlib import Path
import base64, gzip, hashlib, urllib.request, xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'app/src/main/assets'
DATA = ASSETS / 'data'
ROUTES = DATA / 'routes'


def decode_gz_b64(src: Path, dst: Path) -> None:
    raw = base64.b64decode(src.read_text(encoding='utf-8'))
    dst.parent.mkdir(parents=True, exist_ok=True)
    dst.write_bytes(gzip.decompress(raw))


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
        root = ET.fromstring(text)
        coords = root.findall('.//{http://www.opengis.net/kml/2.2}coordinates')
        if not coords or sum(len((c.text or '').split()) for c in coords) < 2:
            raise RuntimeError(f'KML has fewer than 2 coordinate tuples: {path}')


decode_gz_b64(ROOT / 'scripts/assets/index.html.gz.b64', ASSETS / 'index.html')
decode_gz_b64(ROOT / 'scripts/assets/hf.gpx.gz.b64', DATA / 'percurso-teste-hf.gpx')

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

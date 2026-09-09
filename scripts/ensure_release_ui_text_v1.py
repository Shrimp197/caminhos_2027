from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
HTML=ROOT/'app/src/main/assets/index.html'
html=HTML.read_text(encoding='utf-8')

html=html.replace('<h1>Preparar a sua caminhada</h1>', '<h1>Prepare a sua caminhada</h1>', 1)
HTML.write_text(html, encoding='utf-8')
print('Release UI text contract: OK')

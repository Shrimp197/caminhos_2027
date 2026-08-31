from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
p=ROOT/'app/src/main/assets/index.html'
s=p.read_text(encoding='utf-8')

# Only repair the markup issue discovered in the previous generated UI.
bad='<select id="pauseDistance"><option value="0">Sem aviso</option><option value="5">A cada 5 km</option><option value="8" selected>A cada 8 km</option><option value="10">A cada 10 km</option><option value="custom">Personalizar…</option></div>'
good='<select id="pauseDistance"><option value="0">Sem aviso</option><option value="5">A cada 5 km</option><option value="8" selected>A cada 8 km</option><option value="10">A cada 10 km</option><option value="custom">Personalizar…</option></select><div id="pauseDistanceWrap" class="customWrap"><div class="unit"><input id="pauseDistanceCustom" value="0" inputmode="decimal"><span>km</span></div></div></div>'
if bad in s: s=s.replace(bad,good,1)

# Do not add a second six-tile grid or a second controller here. The final
# architecture is applied in a dedicated step after all legacy transforms.
if 'id="cpFinalShell"' not in s:
    raise SystemExit('Approved preparation shell not present after UI refresh')

print('Legacy preparation interaction patch disabled; canonical controller will be applied later')
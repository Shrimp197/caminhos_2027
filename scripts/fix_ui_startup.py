from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
path = ROOT / 'app/src/main/assets/index.html'
html = path.read_text(encoding='utf-8')

old = "const prep=document.querySelector('#prepScreen > .prep');\n    const shell=document.getElementById('cpFinalShell');\n    const detail=document.getElementById('cpDetail');\n    const notes=document.getElementById('cpNotes');\n    const legacyPrep=prep.querySelector(':scope > .prep')||prep;\n    const oldCards=[...legacyPrep.querySelectorAll(':scope > .card')];\n    const start=document.getElementById('startWalkBtn');"
new = "const prep=document.querySelector('#prepScreen > .prep');\n    const shell=document.getElementById('cpFinalShell');\n    const detail=document.getElementById('cpDetail');\n    const notes=document.getElementById('cpNotes');\n    const oldCards=[...prep.querySelectorAll(':scope > .card')];\n    const start=document.getElementById('startWalkBtn');"
if old not in html:
    raise SystemExit('UI runtime preparation block not found')
html = html.replace(old, new, 1)

old = "    oldCards.forEach(c=>detail.appendChild(c));\n    if(manage)detail.appendChild(manage);\n    const originalStart=start;\n    originalStart.style.display='none';\n    legacyPrep.style.display='none';"
new = "    oldCards.forEach(c=>detail.appendChild(c));\n    if(manage)detail.appendChild(manage);\n    const originalStart=start;\n    originalStart.style.display='none';"
if old not in html:
    raise SystemExit('UI runtime visibility block not found')
html = html.replace(old, new, 1)

# Keep the approved shell visible and remove the redundant route subtitle from the top bar.
needle = "    copyRoutes();\n\n    function openDetail(kind){"
replacement = "    function syncRoutesWhenReady(){\n      if(finalSelect.options.length || prepSelect.options.length){copyRoutes();return true}\n      return false\n    }\n    syncRoutesWhenReady();\n    const routeSyncTimer=setInterval(function(){if(syncRoutesWhenReady())clearInterval(routeSyncTimer)},100);\n    const headerRoute=document.getElementById('headerRoute');\n    if(headerRoute)headerRoute.style.display='none';\n\n    function openDetail(kind){"
if needle not in html:
    raise SystemExit('route synchronization insertion point not found')
html = html.replace(needle, replacement, 1)

path.write_text(html, encoding='utf-8')
print('Startup UI hardening: OK')

from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
p=ROOT/'app/src/main/assets/index.html'
s=p.read_text(encoding='utf-8')

# Repair malformed pause-distance markup produced by earlier UI transforms.
bad='<select id="pauseDistance"><option value="0">Sem aviso</option><option value="5">A cada 5 km</option><option value="8" selected>A cada 8 km</option><option value="10">A cada 10 km</option><option value="custom">Personalizar…</option></div>'
good='<select id="pauseDistance"><option value="0">Sem aviso</option><option value="5">A cada 5 km</option><option value="8" selected>A cada 8 km</option><option value="10">A cada 10 km</option><option value="custom">Personalizar…</option></select><div id="pauseDistanceWrap" class="customWrap"><div class="unit"><input id="pauseDistanceCustom" value="0" inputmode="decimal"><span>km</span></div></div></div>'
if bad in s:
    s=s.replace(bad,good,1)

# Approved preparation UX: six primary configuration actions.
needle='    <button id="startWalkBtn" class="primary" type="button">🥾 INICIAR CAMINHADA</button>'
grid='''    <div id="functionGrid" class="card" aria-label="Funções da preparação">\n      <div class="title">CONFIGURAÇÃO</div>\n      <div class="functionGrid">\n        <button type="button" data-action="stage" class="functionTile"><strong>📍</strong><span>Início e fim</span></button>\n        <button type="button" data-action="audio" class="functionTile"><strong>🔊</strong><span>Áudio</span></button>\n        <button type="button" data-action="orientation" class="functionTile"><strong>🧭</strong><span>Orientação</span></button>\n        <button type="button" data-action="pause" class="functionTile"><strong>⏸️</strong><span>Pausas</span></button>\n        <button type="button" data-action="support" class="functionTile"><strong>⛺</strong><span>Apoios</span></button>\n        <button type="button" data-action="notes" class="functionTile"><strong>📝</strong><span>Notas</span></button>\n      </div>\n    </div>\n\n'''+needle
if 'id="functionGrid"' not in s:
    if needle not in s: raise SystemExit('start button anchor not found')
    s=s.replace(needle,grid,1)

# Make the approved visual title explicit.
s=s.replace('<div class="hero"><h1>Preparar caminhada</h1><p>Define a tua caminhada antes de começar.</p></div>', '<div class="hero"><h1>Prepare a sua caminhada</h1><p>Defina o percurso, a etapa e as opções antes de começar.</p></div>',1)

css='''\n.functionGrid{display:grid;grid-template-columns:repeat(3,1fr);gap:7px}\n.functionTile{min-height:72px;border:1px solid #d5ded8;background:#fff;color:var(--g);border-radius:12px;padding:9px 5px;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:4px;font-weight:850;cursor:pointer}.functionTile strong{font-size:20px;line-height:1}.functionTile span{font-size:11px}.functionTile:active{transform:scale(.98)}\n@media(max-width:380px){.functionGrid{grid-template-columns:repeat(2,1fr)}}\n'''
if '.functionGrid{' not in s:
    s=s.replace('@media(max-width:480px){',css+'@media(max-width:480px){',1)

# Bind the six tiles to the actual configuration controls, not no-op placeholders.
marker='function bindControls(){'
interaction='''function bindPreparationActions(){\n  const targets={stage:'prepStart',audio:'audioSelect',orientation:'orientationSelect',pause:'pauseTime',support:'supportFilters',notes:'addNoteBtn'};\n  document.querySelectorAll('#functionGrid [data-action]').forEach(function(btn){\n    btn.onclick=function(){\n      const action=btn.dataset.action;\n      if(action==='notes'){openNote();return}\n      const el=$(targets[action]);\n      if(!el)return;\n      el.scrollIntoView({behavior:'smooth',block:'center'});\n      if(action==='support'){const all=el.querySelector('input[data-filter="all"]');if(all){all.checked=true;all.dispatchEvent(new Event('change',{bubbles:true}))}}\n      else {try{el.focus()}catch(e){}}\n    };\n  });\n}\n\nfunction normalizeSupportFilterUI(){\n  const root=document.querySelector('#supportFilters');\n  const all=root&&root.querySelector('input[data-filter="all"]');\n  const others=root?Array.from(root.querySelectorAll('input:not([data-filter="all"])')):[];\n  if(!all)return;\n  function sync(){\n    if(all.checked){\n      supportFilters=new Set(others.map(function(i){return i.dataset.filter}).filter(Boolean));\n      others.forEach(function(i){i.checked=true;i.parentElement.classList.add('selected')});\n      all.parentElement.classList.add('selected');\n    }else{\n      supportFilters=new Set(others.filter(function(i){return i.checked}).map(function(i){return i.dataset.filter}));\n      all.parentElement.classList.remove('selected');\n    }\n    if(typeof renderSupports==='function')renderSupports();\n  }\n  all.addEventListener('change',sync);\n  others.forEach(function(i){i.addEventListener('change',function(){\n    if(!i.checked)all.checked=false;\n    if(others.length && others.every(function(x){return x.checked}))all.checked=true;\n    sync();\n  })});\n  sync();\n}\n\n'''
if 'function bindPreparationActions()' not in s:
    if marker not in s: raise SystemExit('bindControls anchor not found')
    s=s.replace(marker,interaction+marker,1)

# Ensure initialization binds the new UI after all controls exist.
s=s.replace('renderRouteLibrary();bindControls();const st=settings();', 'renderRouteLibrary();bindControls();bindPreparationActions();normalizeSupportFilterUI();const st=settings();',1)
p.write_text(s,encoding='utf-8')
print('Preparation interaction layer applied')

from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
HTML = ROOT / 'app/src/main/assets/index.html'
html = HTML.read_text(encoding='utf-8')

HEADER = '''<header class="top cp-topbar">
  <div class="brand">
    <img class="brandmark" src="res_icon/ic_launcher_source.png" alt="Caminhos do Peregrino">
    <div><b>Caminhos do Peregrino</b></div>
  </div>
  <button id="cpTopMenuBtn" class="cp-icon-btn" type="button" aria-label="Abrir menu">☰</button>
</header>'''

PREP = '''<section id="prepScreen" class="screen cp-prep-screen">
  <div id="cpFinalShell" class="cp-shell">
    <section class="cp-hero">
      <h1>Preparar a sua caminhada</h1>
      <p>Configure os detalhes e inicie a sua jornada</p>
    </section>

    <section class="cp-route-card">
      <div class="cp-section-row"><span>Percurso selecionado</span><button id="cpFavBtn" class="cp-fav" type="button" aria-label="Adicionar aos favoritos">☆</button></div>
      <div id="cpRoutePhoto" class="cp-route-photo" role="img" aria-label="Imagem do percurso"></div>
      <div class="cp-route-info">
        <div><b id="cpRouteName">Caminho do Centenário</b><small id="cpRouteRange">Porto → Fátima</small></div>
        <strong id="cpRouteDistance">216 km</strong>
      </div>
    </section>

    <div class="cp-grid" id="cpPrepActions">
      <button class="cp-action" data-cp-detail="route" type="button"><span>♙</span><b>Início e fim</b><small>Definir etapas</small></button>
      <button class="cp-action" data-cp-detail="audio" type="button"><span>🔊</span><b>Áudio</b><small>Guias e alertas</small></button>
      <button class="cp-action" data-cp-detail="orientation" type="button"><span>⌖</span><b>Orientação</b><small>Mapa e bússola</small></button>
      <button class="cp-action" data-cp-detail="pause" type="button"><span>◷</span><b>Pausas</b><small>Pausas inteligentes</small></button>
      <button class="cp-action" data-cp-detail="support" type="button"><span>♧</span><b>Apoios</b><small>POI e serviços</small></button>
      <button class="cp-action" data-cp-detail="notes" type="button"><span>▤</span><b>Notas</b><small>As suas anotações</small></button>
    </div>

    <button id="cpStartWalkBtn" class="cp-primary" type="button">🥾 INICIAR CAMINHADA</button>

    <nav class="cp-bottom-nav" aria-label="Navegação principal">
      <button type="button" class="active" data-cp-nav="routes"><span>▦</span><small>Percursos</small></button>
      <button type="button" data-cp-nav="walk"><span>♟</span><small>Caminhada</small></button>
      <button type="button" data-cp-nav="diary"><span>▤</span><small>Diário</small></button>
      <button type="button" data-cp-nav="supports"><span>♧</span><small>Apoios</small></button>
      <button type="button" data-cp-nav="menu"><span>☰</span><small>Menu</small></button>
    </nav>

    <div id="cpLegacySource" class="cp-legacy-prep-card" aria-hidden="true">
      <select id="prepRoute"></select><button id="importPrepBtn" type="button"></button><input id="routeFilePrep" type="file">
      <select id="prepStart"></select><select id="prepEnd"></select><input id="reverseRoute" type="checkbox">
      <select id="audioSelect"></select><select id="orientationSelect"></select>
      <select id="pauseTime"></select><select id="pauseDistance"></select><input id="pauseTimeCustom"><input id="pauseDistanceCustom"><input id="pauseSupport" type="checkbox">
      <div id="supportFilters"></div><button id="startWalkBtn" type="button"></button><button id="manageBtn" type="button"></button>
      <div id="resumeCard"><span id="resumeText"></span><button id="resumeBtn" type="button"></button></div><small id="routeHint"></small>
    </div>
  </div>

  <div id="cpConfigModal" class="cp-modal" hidden>
    <div class="cp-modal-backdrop" data-cp-close="1"></div>
    <section class="cp-modal-sheet" role="dialog" aria-modal="true">
      <button id="cpModalClose" class="cp-modal-close" type="button" aria-label="Fechar">×</button>
      <div id="cpModalBody"></div>
    </section>
  </div>
  <div id="cpGlobalDrawer" class="cp-global-drawer" hidden>
    <div class="cp-drawer-backdrop" data-cp-close-menu="1"></div>
    <aside class="cp-drawer-panel">
      <div class="cp-drawer-head"><b>Menu</b><button id="cpDrawerClose" type="button">×</button></div>
      <button data-cp-dest="routes">Percursos</button><button data-cp-dest="walk">Caminhada</button><button data-cp-dest="supports">Apoios / POI</button><button data-cp-dest="diary">Diário</button><button data-cp-dest="settings">Definições</button><button data-cp-dest="help">Ajuda</button><button data-cp-dest="contact">Contacto</button><button data-cp-dest="about">Sobre</button>
    </aside>
  </div>
</section>'''

CSS = '''
<style id="cp-prep-v1-style">
.cp-topbar{justify-content:space-between}.cp-icon-btn{width:48px;height:48px;border:0;background:transparent;color:#fff;font-size:28px;line-height:1}.cp-prep-screen{background:#f6f2e9}.cp-shell{max-width:520px;margin:0 auto;padding:14px 9px 72px}.cp-hero{background:#fff;text-align:center;border-radius:16px;padding:17px 12px;box-shadow:0 4px 15px #0001}.cp-hero h1{margin:0 0 5px;font-size:22px}.cp-hero p{margin:0;color:var(--muted);font-size:13px}.cp-route-card{background:#fff;border-radius:15px;margin-top:10px;overflow:hidden;box-shadow:0 3px 13px #0001}.cp-section-row{display:flex;justify-content:space-between;align-items:center;padding:10px 12px 7px;font-size:12px;font-weight:900}.cp-fav{border:0;background:transparent;font-size:24px;color:var(--g);cursor:pointer}.cp-route-photo{height:150px;background:linear-gradient(165deg,#9ec5d2 0%,#d8e9dd 42%,#8bb47b 43%,#4c7e55 72%,#d9bd83 73%,#8e6f4a 100%);position:relative}.cp-route-photo:after{content:'';position:absolute;left:16%;right:14%;bottom:20px;height:18px;border-radius:50%;background:#e8c987;transform:skewX(-22deg) rotate(-8deg);box-shadow:0 10px 20px #0002}.cp-route-info{display:flex;justify-content:space-between;align-items:center;padding:10px 12px 13px}.cp-route-info b{display:block;font-size:16px}.cp-route-info small{display:block;color:var(--muted);font-size:11px;margin-top:2px}.cp-route-info strong{color:var(--g);font-size:15px}.cp-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:7px;margin-top:10px}.cp-action{min-height:82px;border:0;border-radius:12px;background:#fff;box-shadow:0 3px 10px #0001;color:var(--text);display:flex;flex-direction:column;align-items:center;justify-content:center;padding:7px 4px;cursor:pointer}.cp-action span{font-size:22px;line-height:1;margin-bottom:5px}.cp-action b{font-size:12px}.cp-action small{font-size:9px;color:var(--muted);margin-top:2px}.cp-primary{width:100%;border:0;background:var(--g2);color:#fff;border-radius:12px;padding:14px;font-size:14px;font-weight:900;margin-top:10px;box-shadow:0 5px 13px #0002}.cp-bottom-nav{position:fixed;left:0;right:0;bottom:0;height:58px;background:#fff;border-top:1px solid #e1e6e2;display:grid;grid-template-columns:repeat(5,1fr);z-index:40}.cp-bottom-nav button{border:0;background:#fff;color:#5d6e66;display:flex;flex-direction:column;align-items:center;justify-content:center;font-size:16px;cursor:pointer}.cp-bottom-nav button.active{color:var(--g);font-weight:900}.cp-bottom-nav small{font-size:9px;margin-top:2px}.cp-legacy-prep-card{display:none!important}.cp-modal{position:fixed;inset:0;z-index:2000}.cp-modal[hidden],.cp-global-drawer[hidden]{display:none}.cp-modal-backdrop,.cp-drawer-backdrop{position:absolute;inset:0;background:#0006}.cp-modal-sheet{position:absolute;left:0;right:0;bottom:0;max-height:86vh;overflow:auto;background:#fff;border-radius:20px 20px 0 0;padding:18px 14px 22px;box-shadow:0 -8px 30px #0002}.cp-modal-close{position:absolute;right:11px;top:8px;border:0;background:#edf3ef;color:var(--g);border-radius:50%;width:34px;height:34px;font-size:24px}.cp-modal-sheet h2{margin:0 42px 4px 0;font-size:20px}.cp-modal-sheet p{margin:0 0 12px;color:var(--muted);font-size:12px}.cp-modal-grid{display:grid;gap:9px}.cp-modal-grid label{font-size:12px;font-weight:800}.cp-modal-grid select,.cp-modal-grid input{width:100%;padding:10px;border:1px solid #d5ded8;border-radius:10px;background:#fff}.cp-modal-actions{display:flex;gap:8px;margin-top:12px}.cp-modal-actions button{flex:1;border:0;border-radius:10px;padding:11px;font-weight:900}.cp-modal-save{background:var(--g2);color:#fff}.cp-modal-cancel{background:#edf3ef;color:var(--g)}.cp-route-list{display:grid;gap:7px}.cp-route-option{display:flex;justify-content:space-between;align-items:center;border:1px solid #dce5de;border-radius:12px;padding:10px;background:#fff}.cp-route-option.active{border:2px solid var(--g);background:#eef7ef}.cp-route-option button{border:0;background:transparent;color:var(--g);font-size:22px}.cp-global-drawer{position:fixed;inset:0;z-index:2100}.cp-drawer-panel{position:absolute;right:0;top:0;bottom:0;width:min(86vw,360px);background:#fff;padding:18px 14px;box-shadow:-8px 0 25px #0003;display:flex;flex-direction:column;gap:7px}.cp-drawer-head{display:flex;align-items:center;justify-content:space-between;font-size:20px;margin-bottom:8px}.cp-drawer-head button{border:0;background:#edf3ef;color:var(--g);border-radius:50%;width:34px;height:34px;font-size:22px}.cp-drawer-panel>button:not(.cp-drawer-head button){border:1px solid #d9e2dc;background:#fff;border-radius:11px;padding:12px;text-align:left;color:var(--g);font-weight:850;font-size:14px}
@media(max-width:380px){.cp-action{min-height:78px}.cp-hero h1{font-size:20px}}
</style>'''

# Remove any previous canonical preparation shell and replace the header/prep region.
html = re.sub(r'<header class="top".*?</header>', HEADER, html, count=1, flags=re.S)
html = re.sub(r'<section id="prepScreen".*?</section>\n\n<section id="navScreen"', PREP + '\n\n<section id="navScreen"', html, count=1, flags=re.S)
if 'id="cp-prep-v1-style"' not in html:
    html = html.replace('</head>', CSS + '\n</head>', 1)

CTRL = '''\n<script id="cp-prep-v1-controller">\n(function(){\n  'use strict';\n  const byId=id=>document.getElementById(id);\n  const modal=byId('cpConfigModal'), body=byId('cpModalBody'), drawer=byId('cpGlobalDrawer');\n  const state={favorites:new Set(JSON.parse(localStorage.getItem('cp.favorites.v1')||'[]'))};\n  const closeModal=()=>{if(modal)modal.hidden=true};\n  const closeMenu=()=>{if(drawer)drawer.hidden=true};\n  const persistFav=()=>localStorage.setItem('cp.favorites.v1',JSON.stringify([...state.favorites]));\n  const currentRoute=()=>byId('prepRoute')?.value||'centenario';\n  const routeLabel=()=>byId('prepRoute')?.selectedOptions?.[0]?.textContent?.split(' · ')[0]||'Caminho do Centenário';\n  const openModal=(title,html,save)=>{body.innerHTML='<h2>'+title+'</h2>'+html+'<div class="cp-modal-actions"><button class="cp-modal-cancel" type="button" id="cpModalCancel">Cancelar</button><button class="cp-modal-save" type="button" id="cpModalSave">Guardar</button></div>';modal.hidden=false;byId('cpModalCancel').onclick=closeModal;byId('cpModalSave').onclick=save;};\n  const syncRouteCard=()=>{const name=routeLabel();byId('cpRouteName').textContent=name;byId('cpRouteDistance').textContent=(byId('prepRoute')?.selectedOptions?.[0]?.textContent?.match(/([0-9]+(?:[.,][0-9]+)?) km/)||[])[1]?((byId('prepRoute').selectedOptions[0].textContent.match(/([0-9]+(?:[.,][0-9]+)?) km/)||[])[1]+' km'):'216 km';byId('cpRouteRange').textContent=name==='Caminho do Centenário'?'Porto → Fátima':'Percurso selecionado';byId('cpFavBtn').textContent=state.favorites.has(currentRoute())?'★':'☆';};\n  function routePanel(){\n    const sel=byId('prepRoute'); const opts=[...sel.options].map(o=>'<div class="cp-route-option '+(o.value===sel.value?'active':'')+'"><span><b>'+o.textContent+'</b></span><button type="button" data-route="'+o.value+'">'+(state.favorites.has(o.value)?'★':'☆')+'</button></div>').join('');\n    openModal('Selecionar percurso','<p>Escolha o percurso que deseja realizar.</p><div class="cp-route-list">'+opts+'</div>',()=>closeModal());\n    body.querySelectorAll('[data-route]').forEach(b=>b.onclick=()=>{sel.value=b.dataset.route;sel.dispatchEvent(new Event('change',{bubbles:true}));setTimeout(()=>{syncRouteCard();closeModal()},80)});\n  }\n  function stagePanel(){\n    const s=byId('prepStart'),e=byId('prepEnd');\n    openModal('Início e fim','<p>Defina o início e o fim da sua etapa.</p><div class="cp-modal-grid"><label>Início<select id="cpModalStart">'+s.innerHTML+'</select></label><label>Fim<select id="cpModalEnd">'+e.innerHTML+'</select></label></div>',()=>{s.value=byId('cpModalStart').value;e.value=byId('cpModalEnd').value;s.dispatchEvent(new Event('change',{bubbles:true}));e.dispatchEvent(new Event('change',{bubbles:true}));localStorage.setItem('cp.stage.v1',JSON.stringify({start:s.value,end:e.value}));closeModal()});\n    byId('cpModalStart').value=s.value;byId('cpModalEnd').value=e.value;\n  }\n  function audioPanel(){const s=byId('audioSelect');openModal('Áudio','<p>Escolha como pretende receber os guias e alertas.</p><div class="cp-modal-grid"><label>Modo<select id="cpModalAudio"><option value="normal">Normal</option><option value="immersive">Imersivo</option><option value="silent">Silencioso</option></select></label></div>',()=>{s.value=byId('cpModalAudio').value;s.dispatchEvent(new Event('change',{bubbles:true}));if(window.setAudio)try{window.setAudio(s.value)}catch(e){}closeModal()});byId('cpModalAudio').value=s.value||'immersive'}\n  function orientationPanel(){const s=byId('orientationSelect');openModal('Orientação','<p>Escolha a orientação do mapa e da direção.</p><div class="cp-modal-grid"><label>Modo<select id="cpModalOrientation"><option value="north">Norte</option><option value="heading">Direção da caminhada</option></select></label></div>',()=>{s.value=byId('cpModalOrientation').value;s.dispatchEvent(new Event('change',{bubbles:true}));if(window.setOrientation)try{window.setOrientation(s.value)}catch(e){}closeModal()});byId('cpModalOrientation').value=s.value||'north'}\n  function pausePanel(){const t=byId('pauseTime'),d=byId('pauseDistance'),sup=byId('pauseSupport');openModal('Pausas inteligentes','<p>Configure avisos por tempo, distância e locais adequados.</p><div class="cp-modal-grid"><label>Por tempo<select id="cpModalPauseTime">'+t.innerHTML+'</select></label><label>Por distância<select id="cpModalPauseDistance">'+d.innerHTML+'</select></label><label style="display:flex;gap:8px;align-items:center"><input id="cpModalPauseSupport" type="checkbox"> Avisar quando existir um local adequado para pausa</label></div>',()=>{t.value=byId('cpModalPauseTime').value;d.value=byId('cpModalPauseDistance').value;sup.checked=byId('cpModalPauseSupport').checked;t.dispatchEvent(new Event('change',{bubbles:true}));d.dispatchEvent(new Event('change',{bubbles:true}));if(window.saveSettings)try{window.saveSettings()}catch(e){}closeModal()});byId('cpModalPauseTime').value=t.value;byId('cpModalPauseDistance').value=d.value;byId('cpModalPauseSupport').checked=sup.checked;}\n  function supportPanel(){const src=byId('supportFilters');const keys=['all','overnight','water','shower','health','fire','temporary','other'];const labels=['Todos','Pernoita','Água','Banho','Saúde','Bombeiros','Temporários','Outros'];const inputs=keys.map((k,i)=>'<label style="display:flex;gap:8px;align-items:center"><input type="checkbox" data-support="'+k+'"> '+labels[i]+'</label>').join('');openModal('Apoios / POI','<p>Selecione as categorias a apresentar no percurso.</p><div class="cp-modal-grid">'+inputs+'</div>',()=>{const selected=[...body.querySelectorAll('[data-support]:checked')].map(x=>x.dataset.support);const all=body.querySelector('[data-support="all"]');let target=src.querySelectorAll('input[data-filter]');target.forEach(x=>x.checked=selected.includes(x.dataset.filter)||selected.includes('all'));src.querySelector('input[data-filter="all"]').checked=selected.length===0||selected.includes('all')||keys.slice(1).every(k=>selected.includes(k));src.querySelector('input[data-filter="all"]').dispatchEvent(new Event('change',{bubbles:true}));target.forEach(x=>x.dispatchEvent(new Event('change',{bubbles:true})));closeModal()});const chosen=[...src.querySelectorAll('input[data-filter]:checked')].map(x=>x.dataset.filter);body.querySelectorAll('[data-support]').forEach(x=>{x.checked=chosen.includes(x.dataset.support)||(x.dataset.support==='all'&&chosen.includes('all'))});}\n  function notesPanel(){const n=byId('addNoteBtn');if(n){closeModal();n.click();}else{openModal('Notas','<p>Registe uma anotação associada ao percurso.</p><div class="cp-modal-grid"><textarea id="cpNoteText" style="min-height:130px;padding:10px;border:1px solid #d5ded8;border-radius:10px"></textarea></div>',()=>{localStorage.setItem('cp.quicknote.v1',byId('cpNoteText').value);closeModal()})}}\n  function show(kind){({route:stagePanel,audio:audioPanel,orientation:orientationPanel,pause:pausePanel,support:supportPanel,notes:notesPanel}[kind]||routePanel)()}\n  function globalDest(k){closeMenu();if(k==='routes')routePanel();else if(k==='walk'){byId('cpStartWalkBtn').click()}else if(k==='supports')supportPanel();else if(k==='diary')notesPanel();else openModal(k==='settings'?'Definições':k==='help'?'Ajuda':k==='contact'?'Contacto':'Sobre','<p>Área de '+(k==='settings'?'definições':k==='help'?'ajuda':k==='contact'?'contacto':'informação')+'.</p>',closeModal);}\n  document.querySelectorAll('[data-cp-detail]').forEach(b=>b.onclick=()=>show(b.dataset.cpDetail));\n  byId('cpTopMenuBtn').onclick=()=>{drawer.hidden=false};byId('cpDrawerClose').onclick=closeMenu;document.querySelectorAll('[data-cp-close]').forEach(x=>x.onclick=closeModal);document.querySelectorAll('[data-cp-close-menu]').forEach(x=>x.onclick=closeMenu);document.querySelectorAll('[data-cp-dest]').forEach(x=>x.onclick=()=>globalDest(x.dataset.cpDest));\n  byId('cpFavBtn').onclick=()=>{const id=currentRoute();state.favorites.has(id)?state.favorites.delete(id):state.favorites.add(id);persistFav();syncRouteCard();routePanel();};\n  byId('cpStartWalkBtn').onclick=()=>{const b=byId('startWalkBtn');if(b)b.click();};\n  document.querySelectorAll('[data-cp-nav]').forEach(b=>b.onclick=()=>globalDest(b.dataset.cpNav==='routes'?'routes':b.dataset.cpNav==='walk'?'walk':b.dataset.cpNav==='supports'?'supports':b.dataset.cpNav==='diary'?'diary':'menu'));\n  byId('cpModalClose').onclick=closeModal;\n  const ro=byId('prepRoute'); if(ro){ro.addEventListener('change',()=>setTimeout(syncRouteCard,50));}\n  setTimeout(syncRouteCard,120);\n})();\n</script>'''

if 'id="cp-prep-v1-controller"' in html:
    html=re.sub(r'<script id="cp-prep-v1-controller">.*?</script>','',html,count=1,flags=re.S)
html=html.replace('</body>',CTRL+'\n</body>',1)
HTML.write_text(html,encoding='utf-8')
print('Canonical preparation screen v1 rebuilt from approved visual reference.')
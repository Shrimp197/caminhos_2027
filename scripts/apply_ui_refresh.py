from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
path = ROOT / 'app/src/main/assets/index.html'
html = path.read_text(encoding='utf-8')

# Approved v1.1.5 preparation screen: compact professional header, scenic route card,
# six functional preparation tiles, and a single prominent start action.
refresh_css = r'''
<style id="cp-ui-refresh-v115-final">
:root{--g:#075a42;--g2:#16a15f;--bg:#f2f7f3;--ink:#18352b;--muted:#65766e;--line:#d9e4dd;--card:#fff}
body{background:var(--bg);color:var(--ink)}
.top{height:76px;padding:8px 14px;background:linear-gradient(135deg,#07563f,#0d7755);box-shadow:0 2px 10px #0002;position:relative}
.top .brand{margin-left:48px;gap:10px}.top .brandmark{width:48px;height:48px;border-radius:12px}.top .brand b{font-size:18px;line-height:1.1}.top .brand small{font-size:12px}
.cp-menu{position:absolute;left:12px;top:15px;width:42px;height:42px;border:0;border-radius:12px;background:transparent;color:#fff;font-size:28px;line-height:1;z-index:5}
.screen{inset:76px 0 0;background:var(--bg)}
.prep{max-width:680px;margin:auto;padding:18px 14px 34px}
.prep>.hero,.prep>.card{display:none!important}
.cp-shell{display:block}
.cp-shell-title{font-size:31px;line-height:1.08;font-weight:850;letter-spacing:-.035em;margin:5px 4px 4px}.cp-shell-sub{font-size:16px;line-height:1.25;color:var(--muted);margin:0 4px 16px}
.cp-route-card{position:relative;overflow:hidden;border-radius:22px;min-height:220px;padding:21px;background:linear-gradient(180deg,#9fc8e1 0%,#c8dce5 42%,#6f8e73 43%,#315d46 63%,#173e31 100%);box-shadow:0 8px 24px #17332925;color:#fff}
.cp-route-card:before{content:'';position:absolute;inset:0;background:linear-gradient(180deg,#153e3920 0%,#17332968 100%)}
.cp-route-card:after{content:'☀   ⛪   🥾';position:absolute;left:20px;right:20px;bottom:62px;text-align:center;font-size:40px;letter-spacing:16px;opacity:.26;filter:grayscale(1)}
.cp-route-inner{position:relative;z-index:1}.cp-route-label{font-size:12px;font-weight:850;letter-spacing:.12em;text-transform:uppercase;opacity:.92}.cp-route-name{font-size:27px;font-weight:900;margin:7px 0 3px}.cp-route-meta{font-size:16px;font-weight:700;opacity:.98}
.cp-route-select{position:absolute;left:21px;right:21px;bottom:19px;width:calc(100% - 42px);padding:12px 13px;border:0;border-radius:12px;background:#fff;color:#173329;font-weight:750;font-size:15px;box-shadow:0 3px 12px #0002}
.cp-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:10px;margin-top:13px}.cp-tile{min-height:112px;border:1px solid #e0e9e3;background:#fff;border-radius:17px;box-shadow:0 4px 14px #17332912;padding:12px 8px;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:9px;color:var(--ink);font-weight:800;font-size:14px}.cp-tile .ico{font-size:31px;line-height:1}.cp-tile:active{transform:scale(.985)}
.cp-start{width:100%;border:0;border-radius:17px;margin-top:14px;padding:17px;background:linear-gradient(135deg,#1bb46d,#118451);color:#fff;font-size:18px;font-weight:900;box-shadow:0 7px 18px #11845135}.cp-status{text-align:center;font-size:12px;color:var(--muted);margin-top:9px}.cp-status b{color:var(--g)}
.cp-detail{display:none;margin-top:12px}.cp-detail.open{display:block}.cp-detail .card{display:block!important;margin-top:0}.cp-detail .title{font-size:11px}.cp-back{width:100%;border:1px solid var(--line);background:#fff;color:var(--g);border-radius:11px;padding:10px;font-weight:800;margin-bottom:10px}
.cp-notes{display:none;background:#fff;border:1px solid var(--line);border-radius:15px;padding:13px;margin-bottom:10px}.cp-notes.open{display:block}.cp-notes textarea{width:100%;min-height:100px;border:1px solid var(--line);border-radius:10px;padding:10px;resize:none}.cp-notes button{width:100%;margin-top:8px;border:0;border-radius:10px;padding:10px;background:var(--g);color:#fff;font-weight:850}
.cp-menu-panel{display:none;position:absolute;left:10px;right:10px;top:74px;z-index:3000;background:#fff;border-radius:18px;box-shadow:0 12px 32px #0003;padding:10px}.cp-menu-panel.open{display:block}.cp-menu-panel button{width:100%;border:0;background:#fff;text-align:left;padding:13px 12px;border-radius:10px;font-weight:750;color:var(--ink)}.cp-menu-panel button:hover{background:#f1f6f3}
@media(max-width:430px){.prep{padding:15px 10px 28px}.cp-shell-title{font-size:28px}.cp-shell-sub{font-size:15px}.cp-route-card{min-height:216px;padding:19px}.cp-route-name{font-size:25px}.cp-grid{gap:8px}.cp-tile{min-height:106px;font-size:13px}.cp-tile .ico{font-size:28px}}
</style>
'''

if 'cp-ui-refresh-v115-final' not in html:
    html = html.replace('</head>', refresh_css + '</head>', 1)

if 'id="cpFinalShell"' not in html:
    marker = '  <div class="prep">'
    shell = r'''  <div class="prep">
    <div id="cpFinalShell" class="cp-shell">
      <div class="cp-shell-title">Prepare a sua caminhada</div>
      <div class="cp-shell-sub">Escolha o percurso, a etapa e as opções antes de começar.</div>
      <div class="cp-route-card">
        <div class="cp-route-inner">
          <div class="cp-route-label">Percurso selecionado</div>
          <div id="cpRouteName" class="cp-route-name">Caminho do Centenário</div>
          <div id="cpRouteMeta" class="cp-route-meta">216 km · Porto → Fátima</div>
          <select id="cpRouteSelect" class="cp-route-select" aria-label="Selecionar percurso"></select>
        </div>
      </div>
      <div class="cp-grid">
        <button class="cp-tile" type="button" data-cp-detail="route"><span class="ico">📍</span><span>Início e fim</span></button>
        <button class="cp-tile" type="button" data-cp-detail="audio"><span class="ico">🔊</span><span>Áudio</span></button>
        <button class="cp-tile" type="button" data-cp-detail="orientation"><span class="ico">🧭</span><span>Orientação</span></button>
        <button class="cp-tile" type="button" data-cp-detail="pause"><span class="ico">⏸️</span><span>Pausas</span></button>
        <button class="cp-tile" type="button" data-cp-detail="support"><span class="ico">💙</span><span>Apoios</span></button>
        <button class="cp-tile" type="button" data-cp-detail="notes"><span class="ico">📝</span><span>Notas</span></button>
      </div>
      <div id="cpStatus" class="cp-status">Percurso selecionado: <b>Caminho do Centenário</b></div>
      <button id="cpStart" class="cp-start" type="button">▶ INICIAR CAMINHADA</button>
    </div>
    <div id="cpDetail" class="cp-detail"><button id="cpBack" class="cp-back" type="button">← Voltar à preparação</button></div>
    <div id="cpNotes" class="cp-notes"><b>Notas da preparação</b><textarea id="cpNotesInput" placeholder="Opcional: registe aqui uma nota antes da caminhada."></textarea><button id="cpNotesSave" type="button">Guardar nota</button></div>
'''
    if marker not in html:
        raise SystemExit('Preparation container marker not found')
    html = html.replace(marker, shell, 1)

    inject_js = r'''
<script id="cp-ui-runtime-v115">
(function(){
  function ready(fn){if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',fn);else fn()}
  ready(function(){
    const prep=document.querySelector('#prepScreen .prep');
    const shell=document.getElementById('cpFinalShell');
    const detail=document.getElementById('cpDetail');
    const notes=document.getElementById('cpNotes');
    const oldCards=[...prep.querySelectorAll(':scope > .card')];
    const start=document.getElementById('startWalkBtn');
    const manage=document.getElementById('manageBtn');
    const prepSelect=document.getElementById('prepRoute');
    const finalSelect=document.getElementById('cpRouteSelect');
    if(!shell||!detail||!prepSelect||!start)return;

    oldCards.forEach(c=>detail.appendChild(c));
    if(manage)detail.appendChild(manage);
    const originalStart=start;
    if(originalStart.parentElement)originalStart.parentElement.removeChild(originalStart);

    function copyRoutes(){
      finalSelect.innerHTML='';
      [...prepSelect.options].forEach(o=>{const n=document.createElement('option');n.value=o.value;n.textContent=o.textContent;finalSelect.appendChild(n)});
      finalSelect.value=prepSelect.value;
      syncRouteText();
    }
    function syncRouteText(){
      const o=finalSelect.options[finalSelect.selectedIndex];
      const name=o?o.textContent:'Caminho do Centenário';
      document.getElementById('cpRouteName').textContent=name;
      document.querySelector('#cpStatus b').textContent=name;
      const meta=document.getElementById('cpRouteMeta');
      let distance='Percurso selecionado';
      if(name.startsWith('Caminho do Centenário')) distance='216 km · Porto → Fátima';
      else if(name.includes('SR')) distance='Trajeto de teste · SR';
      else if(name.includes('HF')) distance='Trajecto de teste · HF';
      meta.textContent=distance;
    }
    finalSelect.addEventListener('change',async function(){
      const chosen=this.value;
      try{if(typeof selectRoute==='function')await selectRoute(chosen);else prepSelect.value=chosen;syncRouteText()}catch(e){this.value=prepSelect.value;}
    });
    copyRoutes();

    function openDetail(kind){
      shell.style.display='none';notes.classList.remove('open');detail.classList.add('open');
      const cards=[...detail.querySelectorAll('.card')];cards.forEach(c=>c.style.display='none');
      let target=null;
      if(kind==='route')target=cards.find(c=>(c.querySelector('.title')?.textContent||'').includes('PERCURSO'));
      if(kind==='audio')target=cards.find(c=>(c.querySelector('.title')?.textContent||'').includes('ÁUDIO'));
      if(kind==='orientation')target=cards.find(c=>(c.querySelector('.title')?.textContent||'').includes('ORIENTAÇÃO'));
      if(kind==='pause')target=cards.find(c=>(c.querySelector('.title')?.textContent||'').includes('PAUSAS'));
      if(kind==='support')target=cards.find(c=>(c.querySelector('.title')?.textContent||'').includes('APOIOS'));
      if(target){target.style.display='block';target.scrollIntoView({behavior:'smooth',block:'start'})}
      if(kind==='notes'){detail.classList.remove('open');notes.classList.add('open');notes.scrollIntoView({behavior:'smooth',block:'start'})}
    }
    document.querySelectorAll('[data-cp-detail]').forEach(b=>b.addEventListener('click',()=>openDetail(b.dataset.cpDetail)));
    document.getElementById('cpBack').addEventListener('click',()=>{detail.classList.remove('open');notes.classList.remove('open');shell.style.display='block';window.scrollTo({top:0,behavior:'smooth'})});
    document.getElementById('cpStart').addEventListener('click',()=>originalStart.click());
    document.getElementById('cpNotesSave').addEventListener('click',()=>{const v=document.getElementById('cpNotesInput').value.trim();if(v)localStorage.setItem('cp.prep.note',v);});
    const saved=localStorage.getItem('cp.prep.note');if(saved)document.getElementById('cpNotesInput').value=saved;

    const top=document.querySelector('.top');
    if(top&&!document.getElementById('cpMenuBtn')){
      const b=document.createElement('button');b.id='cpMenuBtn';b.className='cp-menu';b.type='button';b.textContent='☰';b.setAttribute('aria-label','Menu');top.appendChild(b);
      const panel=document.createElement('div');panel.id='cpMenuPanel';panel.className='cp-menu-panel';panel.innerHTML='<button type="button" data-menu="routes">Percursos</button><button type="button" data-menu="walk">Caminhada</button><button type="button" data-menu="support">Apoios / POI</button><button type="button" data-menu="diary">Diário</button><button type="button" data-menu="settings">Definições</button><button type="button" data-menu="help">Ajuda</button><button type="button" data-menu="contact">Contacto</button><button type="button" data-menu="about">Sobre</button>';
      document.getElementById('prepScreen').appendChild(panel);
      b.addEventListener('click',()=>panel.classList.toggle('open'));
      panel.addEventListener('click',e=>{if(e.target.tagName==='BUTTON'){panel.classList.remove('open');if(e.target.dataset.menu==='routes')openDetail('route');if(e.target.dataset.menu==='support')openDetail('support');if(e.target.dataset.menu==='walk')document.getElementById('cpStart').click();}});
    }
  });
})();
</script>
'''
    html = html.replace('</body>', inject_js + '</body>', 1)

path.write_text(html, encoding='utf-8')
print('Approved preparation UI v1.1.5: OK')

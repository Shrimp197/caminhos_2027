from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
path = ROOT / 'app/src/main/assets/index.html'
html = path.read_text(encoding='utf-8')

# v1.1.5 final preparation shell: implement the approved visual direction
# without duplicating the functional controls already used by the app.
refresh_css = r'''
<style id="cp-ui-refresh-v115-final">
:root{--g:#0b5a3f;--g2:#18a060;--bg:#f4f8f5;--ink:#173329;--muted:#63756d;--line:#dce7e1}
body{background:var(--bg);color:var(--ink)}
.top{height:76px;padding:8px 16px;background:linear-gradient(135deg,#075438,#0d7754);box-shadow:0 2px 10px #0002;position:relative}
.top .brand{margin-left:46px;gap:10px}.top .brandmark{width:48px;height:48px;border-radius:12px}.top .brand b{font-size:18px}.top .brand small{font-size:12px}
.cp-menu{position:absolute;left:12px;top:14px;width:44px;height:44px;border:0;border-radius:12px;background:transparent;color:#fff;font-size:27px;line-height:1}
.screen{inset:76px 0 0;background:var(--bg)}
.prep{max-width:680px;margin:auto;padding:18px 14px 34px}
.prep>.hero,.prep>.card{display:none!important}
.cp-shell{display:block}
.cp-shell-title{font-size:29px;line-height:1.08;font-weight:850;letter-spacing:-.03em;margin:6px 4px 4px}
.cp-shell-sub{font-size:15px;color:var(--muted);margin:0 4px 14px}
.cp-route-card{position:relative;overflow:hidden;border-radius:22px;min-height:190px;padding:22px;background:linear-gradient(135deg,#7ca5c8 0%,#b9d0df 43%,#4b725e 44%,#244d3e 100%);box-shadow:0 8px 24px #17332922;color:#fff}
.cp-route-card:before{content:'';position:absolute;inset:0;background:linear-gradient(180deg,#1a416322 0%,#17332955 100%)}
.cp-route-card:after{content:'✦  🥾  ✦';position:absolute;right:18px;bottom:18px;font-size:31px;opacity:.28}
.cp-route-inner{position:relative;z-index:1}.cp-route-label{font-size:12px;font-weight:800;letter-spacing:.12em;text-transform:uppercase;opacity:.9}.cp-route-name{font-size:26px;font-weight:850;margin:7px 0 3px}.cp-route-meta{font-size:15px;font-weight:650;opacity:.95}.cp-route-select{margin-top:17px;width:100%;padding:11px 13px;border:0;border-radius:12px;background:#fff;color:#173329;font-weight:750;font-size:15px}
.cp-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:10px;margin-top:12px}.cp-tile{min-height:112px;border:1px solid #e1e9e4;background:#fff;border-radius:17px;box-shadow:0 4px 14px #17332912;padding:12px 8px;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:8px;color:var(--ink);font-weight:800;font-size:14px}.cp-tile .ico{font-size:30px;line-height:1}.cp-tile:active{transform:scale(.98)}
.cp-start{width:100%;border:0;border-radius:17px;margin-top:13px;padding:17px;background:linear-gradient(135deg,#1bb46d,#118451);color:#fff;font-size:18px;font-weight:900;box-shadow:0 7px 18px #11845135}.cp-status{text-align:center;font-size:12px;color:var(--muted);margin-top:9px}.cp-status b{color:var(--g)}
.cp-detail{display:none;margin-top:12px}.cp-detail.open{display:block}.cp-detail .card{display:block!important;margin-top:0}
.cp-back{width:100%;border:1px solid var(--line);background:#fff;color:var(--g);border-radius:11px;padding:10px;font-weight:800;margin-bottom:10px}
.cp-menu-panel{display:none;position:absolute;left:10px;right:10px;top:72px;z-index:3000;background:#fff;border-radius:18px;box-shadow:0 12px 32px #0003;padding:10px}.cp-menu-panel.open{display:block}.cp-menu-panel button{width:100%;border:0;background:#fff;text-align:left;padding:13px 12px;border-radius:10px;font-weight:750;color:var(--ink)}.cp-menu-panel button:hover{background:#f1f6f3}
@media(max-width:430px){.prep{padding:14px 10px 28px}.cp-shell-title{font-size:27px}.cp-route-card{min-height:184px;padding:19px}.cp-route-name{font-size:24px}.cp-grid{gap:8px}.cp-tile{min-height:105px;font-size:13px}.cp-tile .ico{font-size:27px}}
</style>
'''

if 'cp-ui-refresh-v115-final' not in html:
    html = html.replace('</head>', refresh_css + '</head>', 1)

# Inject the final preparation UI once. Functional controls remain in the page,
# but are revealed contextually through the six tiles instead of being duplicated.
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

    <div id="cpDetail" class="cp-detail">
      <button id="cpBack" class="cp-back" type="button">← Voltar à preparação</button>
    </div>
'''
    if marker not in html:
        raise SystemExit('Preparation container marker not found')
    html = html.replace(marker, shell, 1)

    # Move the existing functional controls into the contextual detail area.
    # They keep their original IDs and event handlers.
    inject_js = r'''
<script id="cp-ui-runtime-v115">
(function(){
  function ready(fn){if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',fn);else fn()}
  ready(function(){
    const prep=document.querySelector('#prepScreen .prep');
    const shell=document.getElementById('cpFinalShell');
    const detail=document.getElementById('cpDetail');
    const oldCards=[...prep.querySelectorAll(':scope > .card')];
    const start=document.getElementById('startWalkBtn');
    const manage=document.getElementById('manageBtn');
    const prepSelect=document.getElementById('prepRoute');
    const finalSelect=document.getElementById('cpRouteSelect');
    if(!shell||!detail||!prepSelect||!start)return;

    // Keep all original functional controls alive, but show them contextually.
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
      const distance=(name==='Caminho do Centenário')?'216 km · Porto → Fátima':(name.includes('SR')?'Trajeto de teste · SR':name.includes('HF')?'Trajecto de teste · HF':'Percurso selecionado');
      meta.textContent=distance;
    }
    finalSelect.addEventListener('change',async function(){
      prepSelect.value=this.value;
      try{ if(typeof selectRoute==='function') await selectRoute(this.value); }catch(e){ this.value=prepSelect.value; return; }
      syncRouteText();
    });
    copyRoutes();

    function openDetail(kind){
      shell.style.display='none'; detail.classList.add('open');
      const cards=[...detail.querySelectorAll('.card')];
      cards.forEach(c=>c.style.display='none');
      let target=null;
      if(kind==='route') target=cards.find(c=>(c.querySelector('.title')?.textContent||'').includes('PERCURSO'));
      if(kind==='audio') target=cards.find(c=>(c.querySelector('.title')?.textContent||'').includes('ÁUDIO'));
      if(kind==='orientation') target=cards.find(c=>(c.querySelector('.title')?.textContent||'').includes('ORIENTAÇÃO'));
      if(kind==='pause') target=cards.find(c=>(c.querySelector('.title')?.textContent||'').includes('PAUSAS'));
      if(kind==='support') target=cards.find(c=>(c.querySelector('.title')?.textContent||'').includes('APOIOS'));
      if(kind==='notes') target=cards.find(c=>(c.querySelector('.title')?.textContent||'').includes('NOTAS'));
      if(target){target.style.display='block';target.scrollIntoView({behavior:'smooth',block:'start'})}
    }
    document.querySelectorAll('[data-cp-detail]').forEach(b=>b.addEventListener('click',()=>openDetail(b.dataset.cpDetail)));
    document.getElementById('cpBack').addEventListener('click',()=>{detail.classList.remove('open');shell.style.display='block';window.scrollTo({top:0,behavior:'smooth'})});
    document.getElementById('cpStart').addEventListener('click',()=>originalStart.click());

    const top=document.querySelector('.top');
    if(top&&!document.getElementById('cpMenuBtn')){
      const b=document.createElement('button');b.id='cpMenuBtn';b.className='cp-menu';b.type='button';b.textContent='☰';b.setAttribute('aria-label','Menu');top.appendChild(b);
      const panel=document.createElement('div');panel.id='cpMenuPanel';panel.className='cp-menu-panel';panel.innerHTML='<button type="button" data-menu="routes">Percursos</button><button type="button" data-menu="walk">Caminhada</button><button type="button" data-menu="support">Apoios / POI</button><button type="button" data-menu="diary">Diário</button><button type="button" data-menu="settings">Definições</button><button type="button" data-menu="help">Ajuda</button><button type="button" data-menu="contact">Contacto</button><button type="button" data-menu="about">Sobre</button>';
      document.getElementById('prepScreen').appendChild(panel);
      b.addEventListener('click',()=>panel.classList.toggle('open'));
      panel.addEventListener('click',e=>{if(e.target.tagName==='BUTTON'){panel.classList.remove('open');if(e.target.dataset.menu==='routes')openDetail('route');if(e.target.dataset.menu==='support')openDetail('support');}});
    }
  });
})();
</script>
'''
    html = html.replace('</body>', inject_js + '</body>', 1)

# Do not leave the old standalone hero visible after the new shell has been inserted.
# The original controls remain in cpDetail and continue to provide the functional state.
path.write_text(html, encoding='utf-8')
print('Final visual preparation UI v1.1.5: OK')

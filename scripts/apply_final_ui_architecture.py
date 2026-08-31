from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
p=ROOT/'app/src/main/assets/index.html'
s=p.read_text(encoding='utf-8')

required=['id="cpFinalShell"','id="cpDetail"','id="cpRouteSelect"','id="cpStart"','id="startWalkBtn"','id="prepRoute"','id="audioSelect"','id="orientationSelect"','id="pauseTime"','id="supportFilters"','id="addNoteBtn"']
missing=[x for x in required if x not in s]
if missing: raise SystemExit('Missing canonical preparation elements: '+', '.join(missing))

# Remove legacy interaction helpers. There must be one canonical preparation controller.
s=re.sub(r'function bindPreparationActions\(\)\{.*?\n\}\n\n', '', s, count=1, flags=re.S)
s=re.sub(r'function normalizeSupportFilterUI\(\)\{.*?\n\}\n\n', '', s, count=1, flags=re.S)
s=s.replace('bindPreparationActions();normalizeSupportFilterUI();','',1)
if '#functionGrid{display:none!important}' not in s:
    s=s.replace('</style>','\n#functionGrid{display:none!important}\n</style>',1)

# Normalize support state in the canonical application controller. The visible
# filter controls already have their canonical onchange handlers; this function
# makes their behavior symmetric without installing a second listener system.
start_filter=s.find('function setSupportFilter(key,checked){')
end_filter=s.find('function next10(){',start_filter)
if start_filter<0 or end_filter<0: raise SystemExit('Canonical support filter function not found')
filter_fn="""function setSupportFilter(key,checked){const root=$('supportFilters'),all=root&&root.querySelector('input[data-filter=\\\"all\\\"]'),others=root?Array.from(root.querySelectorAll('input:not([data-filter=\\\"all\\\"])')):[];const keys=others.map(i=>i.dataset.filter).filter(Boolean);if(key==='all'){supportFilters=checked?new Set(['all']):new Set()}else{if(supportFilters.has('all'))supportFilters=new Set(keys);checked?supportFilters.add(key):supportFilters.delete(key)}const everySelected=keys.length>0&&keys.every(k=>supportFilters.has(k));if(!supportFilters.size||everySelected)supportFilters=new Set(['all']);root?.querySelectorAll('input').forEach(i=>{const isAll=i.dataset.filter==='all';i.checked=isAll?supportFilters.has('all'):supportFilters.has('all')||supportFilters.has(i.dataset.filter);i.parentElement?.classList.toggle('selected',i.checked)});renderSupports()}\n"""
s=s[:start_filter]+filter_fn+s[end_filter:]

# Replace the previous final UI runtime with one controller. It moves the real
# configuration cards into one detail area instead of cloning functionality.
start=s.find('<script id="cp-ui-runtime-v115">')
if start<0: raise SystemExit('cp-ui-runtime-v115 marker not found')
end=s.find('</script>',start)
if end<0: raise SystemExit('Unclosed cp-ui-runtime-v115')

runtime=r'''<script id="cp-ui-runtime-v115">
(function(){
  function byId(id){return document.getElementById(id)}
  function boot(){
    const prep=byId('prepScreen'),shell=byId('cpFinalShell'),detail=byId('cpDetail'),notes=byId('cpNotes');
    const prepSelect=byId('prepRoute'),finalSelect=byId('cpRouteSelect'),legacyStart=byId('startWalkBtn');
    if(!prep||!shell||!detail||!notes||!prepSelect||!finalSelect||!legacyStart)return;

    legacyStart.style.display='none';legacyStart.setAttribute('aria-hidden','true');
    const manage=byId('manageBtn');if(manage)manage.style.display='none';

    const cards={};
    Array.from(prep.querySelectorAll('.prep>.card')).forEach(function(card){
      const title=(card.querySelector('.title')?.textContent||'').trim().toUpperCase();
      if(title==='PERCURSO')cards.route=card;
      else if(title==='ÁUDIO')cards.audio=card;
      else if(title==='ORIENTAÇÃO')cards.orientation=card;
      else if(title.indexOf('PAUSAS')===0)cards.pause=card;
      else if(title.indexOf('APOIOS')===0)cards.support=card;
      else if(title==='NOTIFICAÇÕES')cards.notifications=card;
    });
    Object.keys(cards).forEach(function(k){detail.appendChild(cards[k]);cards[k].dataset.cpConfig=k;cards[k].style.display='none'});

    function syncRouteView(){
      const o=finalSelect.options[finalSelect.selectedIndex],text=o?o.textContent:'Caminho do Centenário';
      const name=byId('cpRouteName'),status=byId('cpStatus')?.querySelector('b'),meta=byId('cpRouteMeta');
      if(name)name.textContent=text;if(status)status.textContent=text;
      if(meta)meta.textContent=text.indexOf('Centenário')>=0?'216 km · Porto → Fátima':text.indexOf('SR')>=0?'Trajeto de teste · SR':text.indexOf('HF')>=0?'Trajecto de teste · HF':'Percurso selecionado';
    }
    function syncRoutes(){
      if(!prepSelect.options.length)return false;
      if(finalSelect.options.length!==prepSelect.options.length){
        finalSelect.innerHTML='';Array.from(prepSelect.options).forEach(function(o){const n=document.createElement('option');n.value=o.value;n.textContent=o.textContent;finalSelect.appendChild(n)});
      }
      finalSelect.value=prepSelect.value;syncRouteView();return true;
    }
    syncRoutes();const timer=setInterval(function(){if(syncRoutes())clearInterval(timer)},150);setTimeout(function(){clearInterval(timer)},10000);

    // Route selection uses the canonical preparation select and its existing
    // route controller, so there is exactly one route state machine.
    finalSelect.onchange=function(){const chosen=this.value;prepSelect.value=chosen;prepSelect.dispatchEvent(new Event('change',{bubbles:true}));setTimeout(function(){finalSelect.value=prepSelect.value;syncRouteView()},150)};

    function openConfig(kind){
      shell.style.display='none';notes.classList.remove('open');detail.classList.add('open');
      Object.keys(cards).forEach(function(k){if(cards[k])cards[k].style.display=k===kind?'block':'none'});
      detail.scrollIntoView({behavior:'smooth',block:'start'});
    }
    document.querySelectorAll('#cpFinalShell [data-cp-detail]').forEach(function(btn){btn.onclick=function(){const kind=btn.dataset.cpDetail;if(kind==='notes'){const n=byId('addNoteBtn');if(n)n.click();return}openConfig(kind)}});
    byId('cpBack').onclick=function(){detail.classList.remove('open');shell.style.display='block';window.scrollTo(0,0)};
    byId('cpStart').onclick=function(){legacyStart.click()};

    // Support filters are governed solely by the canonical bindControls()
    // handlers in the main application IIFE. There is no second listener system.

    // One global menu. It never creates a second preparation configuration grid.
    const top=document.querySelector('.top');
    if(top&&!byId('cpMenuBtn')){
      const b=document.createElement('button');b.id='cpMenuBtn';b.className='cp-menu';b.type='button';b.textContent='☰';b.setAttribute('aria-label','Menu');top.appendChild(b);
      const panel=document.createElement('div');panel.id='cpMenuPanel';panel.className='cp-menu-panel';panel.innerHTML='<button type="button" data-menu="routes">Percursos</button><button type="button" data-menu="walk">Caminhada</button><button type="button" data-menu="support">Apoios / POI</button><button type="button" data-menu="diary">Diário</button><button type="button" data-menu="settings">Definições</button><button type="button" data-menu="help">Ajuda</button><button type="button" data-menu="contact">Contacto</button><button type="button" data-menu="about">Sobre</button>';
      prep.appendChild(panel);
      b.onclick=function(){panel.classList.toggle('open')};
      panel.addEventListener('click',function(e){const m=e.target.dataset.menu;if(!m)return;panel.classList.remove('open');if(m==='routes')openConfig('route');if(m==='support')openConfig('support');if(m==='walk')byId('cpStart').click();if(m==='diary'){const n=byId('addNoteBtn');if(n)n.click()}if(m==='settings'){openConfig('notifications')}if(m==='help')showInfo('Ajuda','Consulte a preparação, o GPS e os apoios antes de iniciar.');if(m==='contact')showInfo('Contacto','Use o formulário de contacto da aplicação.');if(m==='about')showInfo('Sobre','Caminhos do Peregrino — aplicação de apoio à caminhada.')});
    }
    function showInfo(title,text){let d=byId('cpInfo');if(!d){d=document.createElement('div');d.id='cpInfo';d.className='dialog';d.innerHTML='<div class="sheet"><h2></h2><p></p><button class="cancel" type="button">Fechar</button></div>';document.body.appendChild(d);d.querySelector('button').onclick=function(){d.classList.remove('open')}}d.querySelector('h2').textContent=title;d.querySelector('p').textContent=text;d.classList.add('open')}

    shell.style.display='block';shell.style.visibility='visible';shell.style.opacity='1';
  }
  if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',boot,{once:true});else boot();
})();
</script>'''
s=s[:start]+runtime+s[end+len('</script>'):]
p.write_text(s,encoding='utf-8')
print('Final preparation UI architecture applied')
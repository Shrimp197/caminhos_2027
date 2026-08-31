from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
p=ROOT/'app/src/main/assets/index.html'
s=p.read_text(encoding='utf-8')

# The current visual shell is only a front-end; the real controls already contain
# the application behavior. Move those real controls into one detail area instead
# of creating a second implementation outside the main application IIFE.
required=['id="cpFinalShell"','id="cpDetail"','id="prepRoute"','id="startWalkBtn"','id="audioSelect"','id="orientationSelect"','id="pauseTime"','id="supportFilters"','id="addNoteBtn"']
missing=[x for x in required if x not in s]
if missing: raise SystemExit('Missing canonical preparation elements: '+', '.join(missing))

# Hide the old helper grid inserted by legacy interaction scripts; the visible
# preparation has exactly one six-tile grid.
if '#functionGrid{display:none!important}' not in s:
    s=s.replace('</style>','\n#functionGrid{display:none!important}\n</style>',1)

# Replace the old runtime controller completely.
start=s.find('<script id="cp-ui-runtime-v115">')
if start<0: raise SystemExit('cp-ui-runtime-v115 not found')
end=s.find('</script>',start)
if end<0: raise SystemExit('Unclosed cp-ui-runtime-v115')

runtime=r'''<script id="cp-ui-runtime-v115">
(function(){
  function byId(id){return document.getElementById(id)}
  function boot(){
    const prep=byId('prepScreen'), shell=byId('cpFinalShell'), detail=byId('cpDetail'), notes=byId('cpNotes');
    const prepSelect=byId('prepRoute'), finalSelect=byId('cpRouteSelect'), legacyStart=byId('startWalkBtn');
    if(!prep||!shell||!detail||!notes||!prepSelect||!finalSelect||!legacyStart) return;

    shell.style.display='block'; shell.style.visibility='visible'; shell.style.opacity='1';
    legacyStart.style.display='none'; legacyStart.setAttribute('aria-hidden','true');
    const manage=byId('manageBtn'); if(manage) manage.style.display='none';

    // Move the canonical configuration cards into the single detail view.
    const configMap={route:'PERCURSO',audio:'ÁUDIO',orientation:'ORIENTAÇÃO',pause:'PAUSAS',support:'APOIOS'};
    const cards={};
    Array.from(prep.querySelectorAll('.prep>.card')).forEach(function(card){
      const title=(card.querySelector('.title')?.textContent||'').trim().toUpperCase();
      if(title==='PERCURSO'||title==='ÁUDIO'||title==='ORIENTAÇÃO'||title==='PAUSAS INTELIGENTES'||title==='APOIOS & POI'||title==='NOTIFICAÇÕES'){
        if(title==='PERCURSO') cards.route=card;
        else if(title==='ÁUDIO') cards.audio=card;
        else if(title==='ORIENTAÇÃO') cards.orientation=card;
        else if(title.indexOf('PAUSAS')===0) cards.pause=card;
        else if(title.indexOf('APOIOS')===0) cards.support=card;
        else if(title==='NOTIFICAÇÕES') cards.notifications=card;
      }
    });
    // Preserve functionality without duplicating it: use the real cards as the detail content.
    Object.keys(cards).forEach(function(k){detail.appendChild(cards[k]);cards[k].dataset.cpConfig=k;cards[k].style.display='none'});
    if(cards.notifications) cards.notifications.style.display='none';

    function syncRouteView(){
      const o=finalSelect.options[finalSelect.selectedIndex];
      const text=o?o.textContent:'Caminho do Centenário';
      const name=byId('cpRouteName'), status=prep.querySelector('#cpStatus b'), meta=byId('cpRouteMeta');
      if(name)name.textContent=text; if(status)status.textContent=text;
      if(meta)meta.textContent=text.indexOf('Centenário')>=0?'216 km · Porto → Fátima':text.indexOf('SR')>=0?'Trajeto de teste · SR':text.indexOf('HF')>=0?'Trajecto de teste · HF':'Percurso selecionado';
    }
    function syncRouteSelect(){
      if(!prepSelect.options.length)return false;
      finalSelect.innerHTML='';
      Array.from(prepSelect.options).forEach(function(o){const n=document.createElement('option');n.value=o.value;n.textContent=o.textContent;finalSelect.appendChild(n)});
      finalSelect.value=prepSelect.value; syncRouteView(); return true;
    }
    syncRouteSelect();
    const timer=setInterval(function(){if(syncRouteSelect())clearInterval(timer)},150); setTimeout(function(){clearInterval(timer)},10000);

    finalSelect.onchange=function(){
      const chosen=this.value;
      prepSelect.value=chosen;
      prepSelect.dispatchEvent(new Event('change',{bubbles:true}));
      setTimeout(function(){finalSelect.value=prepSelect.value;syncRouteView()},50);
    };

    function openConfig(kind){
      shell.style.display='none'; notes.classList.remove('open'); detail.classList.add('open');
      Object.keys(cards).forEach(function(k){if(cards[k])cards[k].style.display=k===kind?'block':'none'});
      detail.scrollIntoView({behavior:'smooth',block:'start'});
    }
    document.querySelectorAll('#cpFinalShell [data-cp-detail]').forEach(function(btn){btn.onclick=function(){
      const kind=btn.dataset.cpDetail;
      if(kind==='notes'){
        shell.style.display='none'; detail.classList.remove('open'); notes.classList.add('open'); byId('addNoteBtn').click(); return;
      }
      openConfig(kind);
    }});
    byId('cpBack').onclick=function(){detail.classList.remove('open');notes.classList.remove('open');shell.style.display='block';window.scrollTo(0,0)};
    byId('cpStart').onclick=function(){legacyStart.click()};

    // Make the support filter bidirectional using the canonical change handler.
    const sf=byId('supportFilters');
    if(sf){
      const all=sf.querySelector('input[data-filter="all"]');
      const others=Array.from(sf.querySelectorAll('input:not([data-filter="all"])'));
      others.forEach(function(i){i.addEventListener('change',function(){
        const complete=others.length>0 && others.every(function(x){return x.checked});
        if(complete && !all.checked){all.checked=true;all.dispatchEvent(new Event('change',{bubbles:true}))}
        else if(!complete && all.checked){all.checked=false;all.dispatchEvent(new Event('change',{bubbles:true}))}
      })});
    }

    // One global menu. It opens the same canonical controls; it does not create a second settings panel.
    const top=prep.querySelector('.top');
    if(top && !byId('cpMenuBtn')){
      const b=document.createElement('button'); b.id='cpMenuBtn'; b.className='cp-menu'; b.type='button'; b.textContent='☰'; b.setAttribute('aria-label','Menu'); top.appendChild(b);
      const panel=document.createElement('div'); panel.id='cpMenuPanel'; panel.className='cp-menu-panel';
      panel.innerHTML='<button type="button" data-menu="routes">Percursos</button><button type="button" data-menu="walk">Caminhada</button><button type="button" data-menu="support">Apoios / POI</button><button type="button" data-menu="diary">Diário</button><button type="button" data-menu="settings">Definições</button><button type="button" data-menu="help">Ajuda</button><button type="button" data-menu="contact">Contacto</button><button type="button" data-menu="about">Sobre</button>';
      prep.appendChild(panel);
      b.onclick=function(){panel.classList.toggle('open')};
      panel.addEventListener('click',function(e){const m=e.target.dataset.menu;if(!m)return;panel.classList.remove('open');if(m==='routes')openConfig('route');if(m==='support')openConfig('support');if(m==='walk')byId('cpStart').click()});
    }
  }
  if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',boot,{once:true}); else boot();
})();
</script>'''

s=s[:start]+runtime+s[end+len('</script>'):]
p.write_text(s,encoding='utf-8')
print('Final preparation UI architecture applied')
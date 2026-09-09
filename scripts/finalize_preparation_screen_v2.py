from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
HTML = ROOT / 'app/src/main/assets/index.html'
s = HTML.read_text(encoding='utf-8')

# Replace only the visual preparation shell; keep the existing application runtime
# and hidden compatibility controls as the integration bridge to navigation/GPS.
start = s.find('<section id="prepScreen"')
end = s.find('\n\n<section id="navScreen"', start)
if start < 0 or end < 0:
    raise SystemExit('Preparation section boundaries not found')

PREP = r'''<section id="prepScreen" class="screen cp-prep-screen">
  <div id="cpFinalShell" class="cp-shell">
    <header class="cp-topbar">
      <div class="cp-brand"><img src="res_icon/ic_launcher_source.png" alt="Caminhos do Peregrino"><b>Caminhos do Peregrino</b></div>
      <button id="cpTopMenuBtn" class="cp-menu-btn" type="button" aria-label="Abrir menu">☰</button>
    </header>
    <main class="cp-content">
      <section class="cp-hero"><h1>Preparar a sua caminhada</h1><p>Configure os detalhes e inicie a sua jornada</p></section>
      <section class="cp-route-card">
        <div class="cp-section-row"><span>Percurso selecionado</span><button id="cpFavBtn" class="cp-fav" type="button" aria-label="Adicionar percurso aos favoritos">☆</button></div>
        <button id="cpRouteOpen" class="cp-route-button" type="button">
          <div id="cpRoutePhoto" class="cp-route-photo" aria-label="Imagem do percurso"></div>
          <div class="cp-route-info"><div><b id="cpRouteName">Caminho do Centenário</b><small id="cpRouteRange">Porto → Fátima</small></div><strong id="cpRouteDistance">216 km</strong></div>
        </button>
      </section>
      <div class="cp-grid" id="cpPrepActions">
        <button class="cp-action" data-cp-detail="route" type="button"><span>📍</span><b>Início e fim</b><small>Definir etapas</small></button>
        <button class="cp-action" data-cp-detail="audio" type="button"><span>🔊</span><b>Áudio</b><small>Guias e alertas</small></button>
        <button class="cp-action" data-cp-detail="orientation" type="button"><span>🧭</span><b>Orientação</b><small>Mapa e bússola</small></button>
        <button class="cp-action" data-cp-detail="pause" type="button"><span>⏱️</span><b>Pausas</b><small>Pausas inteligentes</small></button>
        <button class="cp-action" data-cp-detail="support" type="button"><span>⛺</span><b>Apoios</b><small>POI e serviços</small></button>
        <button class="cp-action" data-cp-detail="notes" type="button"><span>📝</span><b>Notas</b><small>As suas anotações</small></button>
      </div>
      <button id="cpStartWalkBtn" class="cp-primary" type="button">🥾 INICIAR CAMINHADA</button>
      <nav class="cp-bottom-nav" aria-label="Navegação principal">
        <button type="button" class="active" data-cp-nav="routes"><span>🗺️</span><small>Percursos</small></button>
        <button type="button" data-cp-nav="walk"><span>🥾</span><small>Caminhada</small></button>
        <button type="button" data-cp-nav="diary"><span>📔</span><small>Diário</small></button>
        <button type="button" data-cp-nav="supports"><span>⛺</span><small>Apoios</small></button>
        <button type="button" data-cp-nav="menu"><span>☰</span><small>Menu</small></button>
      </nav>
      <div id="cpLegacySource" class="cp-legacy-prep-card" aria-hidden="true">
        <select id="cpRouteSelect"></select><input id="finalRoute" type="hidden" value="centenario"><button id="cpStart" type="button"></button>
        <select id="prepRoute"></select><button id="importPrepBtn" type="button"></button><input id="routeFilePrep" type="file">
        <select id="prepStart"></select><select id="prepEnd"></select><input id="reverseRoute" type="checkbox">
        <select id="audioSelect"></select><select id="orientationSelect"></select><select id="pauseTime"></select><select id="pauseDistance"></select>
        <div id="pauseTimeWrap" class="customWrap"></div><div id="pauseDistanceWrap" class="customWrap"></div><input id="pauseTimeCustom"><input id="pauseDistanceCustom"><input id="pauseSupport" type="checkbox">
        <div id="supportFilters"><input type="checkbox" data-filter="all" checked><input type="checkbox" data-filter="overnight"><input type="checkbox" data-filter="water"><input type="checkbox" data-filter="shower"><input type="checkbox" data-filter="health"><input type="checkbox" data-filter="fire"><input type="checkbox" data-filter="temporary"><input type="checkbox" data-filter="other"></div>
        <button id="startWalkBtn" type="button"></button><button id="manageBtn" type="button"></button><button id="addNoteBtn" type="button"></button>
        <div id="resumeCard"><span id="resumeText"></span><button id="resumeBtn" type="button"></button></div><small id="routeHint"></small><button id="notificationBtn" type="button"></button>
      </div>
    </main>
  </div>
  <div id="cpConfigModal" class="cp-modal" hidden><div class="cp-modal-backdrop" data-cp-close="1"></div><section class="cp-modal-sheet" role="dialog" aria-modal="true"><button id="cpModalClose" class="cp-modal-close" type="button">×</button><div id="cpModalBody"></div><div class="cp-modal-actions"><button id="cpModalCancel" class="cp-modal-cancel" type="button">Cancelar</button><button id="cpModalSave" class="cp-modal-save" type="button">Guardar</button></div></section></div>
  <div id="cpGlobalDrawer" class="cp-global-drawer" hidden><div class="cp-drawer-backdrop" data-cp-close-menu="1"></div><aside class="cp-drawer-panel"><div class="cp-drawer-head"><b>Menu</b><button id="cpDrawerClose" type="button">×</button></div><button data-cp-dest="routes">Percursos</button><button data-cp-dest="walk">Caminhada</button><button data-cp-dest="supports">Apoios / POI</button><button data-cp-dest="diary">Diário</button><button data-cp-dest="settings">Definições</button><button data-cp-dest="help">Ajuda</button><button data-cp-dest="contact">Contacto</button><button data-cp-dest="about">Sobre</button></aside></div>
</section>'''

CSS = r'''<style id="cp-prep-v2-style">
.cp-prep-screen{background:#f6f2e9;overflow:auto}.cp-shell{min-height:100%;padding-bottom:72px}.cp-topbar{height:68px;background:var(--g);color:#fff;display:flex;align-items:center;justify-content:space-between;padding:8px 12px}.cp-brand{display:flex;align-items:center;gap:9px}.cp-brand img{width:43px;height:43px;border-radius:11px;object-fit:cover}.cp-brand b{font-size:17px}.cp-menu-btn{width:48px;height:48px;border:0;background:transparent;color:#fff;font-size:28px}.cp-content{max-width:520px;margin:auto;padding:14px 9px}.cp-hero{background:#fff;text-align:center;border-radius:16px;padding:17px 12px;box-shadow:0 4px 15px #0001}.cp-hero h1{margin:0 0 5px;font-size:22px}.cp-hero p{margin:0;color:var(--muted);font-size:13px}.cp-route-card{background:#fff;border-radius:15px;margin-top:10px;overflow:hidden;box-shadow:0 3px 13px #0001}.cp-section-row{display:flex;justify-content:space-between;align-items:center;padding:10px 12px 7px;font-size:12px;font-weight:900}.cp-fav{border:0;background:transparent;color:var(--g);font-size:25px;line-height:1}.cp-route-button{width:100%;padding:0;border:0;background:#fff;text-align:left}.cp-route-photo{height:160px;background:linear-gradient(165deg,#9ec5d2 0%,#d8e9dd 42%,#8bb47b 43%,#4c7e55 72%,#d9bd83 73%,#8e6f4a 100%);position:relative}.cp-route-photo:after{content:'';position:absolute;left:16%;right:14%;bottom:20px;height:18px;border-radius:50%;background:#e8c987;transform:skewX(-22deg) rotate(-8deg);box-shadow:0 10px 20px #0002}.cp-route-info{display:flex;justify-content:space-between;align-items:center;padding:10px 12px 13px}.cp-route-info b{display:block;font-size:16px}.cp-route-info small{display:block;color:var(--muted);font-size:11px;margin-top:2px}.cp-route-info strong{color:var(--g);font-size:15px}.cp-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:7px;margin-top:10px}.cp-action{min-height:82px;border:0;border-radius:12px;background:#fff;box-shadow:0 3px 10px #0001;color:var(--text);display:flex;flex-direction:column;align-items:center;justify-content:center;padding:7px 4px}.cp-action span{font-size:22px;line-height:1;margin-bottom:5px}.cp-action b{font-size:12px}.cp-action small{font-size:9px;color:var(--muted);margin-top:2px}.cp-primary{width:100%;border:0;background:var(--g2);color:#fff;border-radius:12px;padding:14px;font-size:15px;font-weight:900;margin-top:10px}.cp-bottom-nav{position:fixed;left:0;right:0;bottom:0;height:58px;background:#fff;border-top:1px solid #e1e6e2;display:grid;grid-template-columns:repeat(5,1fr);z-index:1500}.cp-bottom-nav button{border:0;background:#fff;color:#5d6e66;display:flex;flex-direction:column;align-items:center;justify-content:center;font-size:17px}.cp-bottom-nav button.active{color:var(--g);font-weight:900}.cp-bottom-nav small{font-size:9px;margin-top:2px}.cp-legacy-prep-card{display:none!important}.cp-modal[hidden],.cp-global-drawer[hidden]{display:none!important}.cp-modal{position:fixed;inset:0;z-index:3000}.cp-modal-backdrop{position:absolute;inset:0;background:#0006}.cp-modal-sheet{position:absolute;left:0;right:0;bottom:0;max-height:88vh;overflow:auto;background:#fff;border-radius:20px 20px 0 0;padding:18px 14px 22px}.cp-modal-close{position:absolute;right:11px;top:9px;border:0;background:#edf3ef;color:var(--g);border-radius:50%;width:34px;height:34px;font-size:24px}.cp-modal-sheet h2{margin:0 42px 5px 0;font-size:20px}.cp-modal-sheet p{margin:0 0 12px;color:var(--muted);font-size:12px}.cp-modal-grid{display:grid;grid-template-columns:1fr 1fr;gap:10px}.cp-modal-grid .full{grid-column:1/-1}.cp-modal-field{display:flex;flex-direction:column;gap:5px}.cp-modal-field label{font-size:12px;font-weight:800}.cp-modal-field select,.cp-modal-field input,.cp-modal-field textarea{width:100%;padding:10px;border:1px solid #d5ded8;border-radius:10px;background:#fff}.cp-modal-options{display:flex;flex-wrap:wrap;gap:7px}.cp-modal-options label{border:1px solid #d5ded8;border-radius:10px;padding:9px;font-size:12px}.cp-modal-options label.selected{background:var(--g);color:#fff;border-color:var(--g)}.cp-modal-actions{display:flex;gap:8px;margin-top:14px}.cp-modal-actions button{flex:1;border:0;border-radius:10px;padding:12px;font-weight:900}.cp-modal-cancel{background:#edf3ef;color:var(--g)}.cp-modal-save{background:var(--g2);color:#fff}.cp-global-drawer{position:fixed;inset:0;z-index:3100}.cp-drawer-backdrop{position:absolute;inset:0;background:#0006}.cp-drawer-panel{position:absolute;right:0;top:0;bottom:0;width:min(86vw,360px);background:#fff;padding:18px 14px;box-shadow:-8px 0 25px #0003;display:flex;flex-direction:column;gap:7px}.cp-drawer-head{display:flex;justify-content:space-between;align-items:center;font-size:20px;margin-bottom:8px}.cp-drawer-head button{border:0;background:#edf3ef;color:var(--g);border-radius:50%;width:34px;height:34px;font-size:22px}.cp-drawer-panel>button:not(.cp-drawer-head button){border:1px solid #d9e2dc;background:#fff;border-radius:11px;padding:12px;text-align:left;color:var(--g);font-weight:850;font-size:14px}
@media(max-width:380px){.cp-grid{gap:5px}.cp-action{min-height:78px}.cp-hero h1{font-size:20px}}
</style>'''

# Remove prior v2 style/controller if rerun.
s = re.sub(r'<style id="cp-prep-v2-style">.*?</style>', '', s, flags=re.S)
s = re.sub(r'<script id="cp-prep-v2-controller">.*?</script>', '', s, flags=re.S)
s = s[:start] + PREP + s[end:]
s = s.replace('</head>', CSS + '\n</head>', 1)

CTRL = r'''<script id="cp-prep-v2-controller">
(function(){
'use strict';
function q(id){return document.getElementById(id)}
function esc(v){return String(v).replace(/[&<>"']/g,function(c){return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]})}
var favs=JSON.parse(localStorage.getItem('cp.favorites.v2')||'[]');
var modal,body,currentKind=null;
function openModal(kind,title,subtitle,content,save){
  modal=q('cpConfigModal');body=q('cpModalBody');currentKind=kind;
  body.innerHTML='<h2>'+title+'</h2><p>'+subtitle+'</p>'+content;
  modal.hidden=false;q('cpModalSave').onclick=save;q('cpModalCancel').onclick=closeModal;q('cpModalClose').onclick=closeModal;
}
function closeModal(){if(modal)modal.hidden=true;currentKind=null}
function syncRouteCard(){var s=q('prepRoute'),o=s&&s.options[s.selectedIndex],name=o?o.textContent.split(' · ')[0]:'Selecione um percurso';q('cpRouteName').textContent=name;q('cpRouteDistance').textContent=o&&o.textContent.match(/([0-9]+(?:[.,][0-9]+)?) km/)?o.textContent.match(/([0-9]+(?:[.,][0-9]+)?) km/)[1]+' km':'';q('cpRouteRange').textContent=name==='Caminho do Centenário'?'Porto → Fátima':'Percurso selecionado';q('cpFavBtn').textContent=favs.indexOf(s.value)>=0?'★':'☆';q('cpFavBtn').setAttribute('aria-label',favs.indexOf(s.value)>=0?'Remover dos favoritos':'Adicionar aos favoritos')}
function saveState(){localStorage.setItem('cp.settings.v1',JSON.stringify({audio:q('audioSelect').value,orientation:q('orientationSelect').value,pauseTimeMode:q('pauseTime').value,pauseDistanceMode:q('pauseDistance').value,pauseTimeCustom:q('pauseTimeCustom').value,pauseDistanceCustom:q('pauseDistanceCustom').value,pauseSupport:q('pauseSupport').checked,reverse:q('reverseRoute').checked}))}
function stage(){var s=q('prepStart'),e=q('prepEnd');openModal('route','Início e fim','Escolha as etapas disponíveis para o percurso selecionado.','<div class="cp-modal-grid"><div class="cp-modal-field"><label>Início<select id="v2Start">'+s.innerHTML+'</select></label></div><div class="cp-modal-field"><label>Fim<select id="v2End">'+e.innerHTML+'</select></label></div></div>',function(){s.value=q('v2Start').value;e.value=q('v2End').value;s.dispatchEvent(new Event('change',{bubbles:true}));e.dispatchEvent(new Event('change',{bubbles:true}));saveState();closeModal()})}
function audio(){var a=q('audioSelect');openModal('audio','Áudio','Escolha o modo de áudio da caminhada.','<div class="cp-modal-field"><label>Modo<select id="v2Audio"><option value="normal">🔊 Normal</option><option value="immersive">🎧 Imersivo</option><option value="silent">🔇 Silencioso</option></select></label></div>',function(){a.value=q('v2Audio').value;a.dispatchEvent(new Event('change',{bubbles:true}));saveState();closeModal()});q('v2Audio').value=a.value}
function orientation(){var a=q('orientationSelect');openModal('orientation','Orientação','Escolha como o mapa acompanha a sua caminhada.','<div class="cp-modal-field"><label>Modo<select id="v2Orientation"><option value="north">🧭 Norte</option><option value="heading">↟ Direção da caminhada</option></select></label></div>',function(){a.value=q('v2Orientation').value;a.dispatchEvent(new Event('change',{bubbles:true}));saveState();closeModal()});q('v2Orientation').value=a.value}
function pauses(){var pt=q('pauseTime'),pd=q('pauseDistance'),ps=q('pauseSupport');openModal('pause','Pausas inteligentes','Configure avisos por tempo, distância e local adequado.','<div class="cp-modal-grid"><div class="cp-modal-field"><label>Por tempo<select id="v2PT"><option value="0">Sem aviso</option><option value="60">A cada 60 min</option><option value="90">A cada 90 min</option><option value="120">A cada 120 min</option><option value="custom">Personalizar…</option></select></label><input id="v2PTC" placeholder="95" inputmode="numeric" aria-label="Minutos personalizados"></div><div class="cp-modal-field"><label>Por distância<select id="v2PD"><option value="0">Sem aviso</option><option value="5">A cada 5 km</option><option value="8">A cada 8 km</option><option value="10">A cada 10 km</option><option value="custom">Personalizar…</option></select></label><input id="v2PDC" placeholder="Distância em km" inputmode="decimal"></div><div class="cp-modal-field full"><label><input id="v2PS" type="checkbox"> Avisar quando existir um local adequado para pausa</label></div></div>',function(){pt.value=q('v2PT').value;pd.value=q('v2PD').value;q('pauseTimeCustom').value=q('v2PTC').value||q('pauseTimeCustom').value;q('pauseDistanceCustom').value=q('v2PDC').value||q('pauseDistanceCustom').value;ps.checked=q('v2PS').checked;pt.dispatchEvent(new Event('change',{bubbles:true}));pd.dispatchEvent(new Event('change',{bubbles:true}));ps.dispatchEvent(new Event('change',{bubbles:true}));saveState();closeModal()});q('v2PT').value=pt.value;q('v2PD').value=pd.value;q('v2PTC').value=q('pauseTimeCustom').value||'';q('v2PDC').value=q('pauseDistanceCustom').value||'';q('v2PS').checked=ps.checked}
function supports(){var src=[].slice.call(document.querySelectorAll('#supportFilters input[data-filter]'));openModal('support','Apoios & POI','Escolha os tipos de apoio que deseja ver na caminhada.','<div id="v2Support" class="cp-modal-options">'+src.map(function(i){return '<label><input type="checkbox" data-filter="'+i.dataset.filter+'" '+(i.checked?'checked':'')+'>'+esc(i.parentElement?i.parentElement.textContent.trim():i.dataset.filter)+'</label>'}).join('')+'</div>',function(){var all=q('v2Support').querySelector('input[data-filter="all"]'),others=[].slice.call(q('v2Support').querySelectorAll('input:not([data-filter="all"])'));if(all&&all.checked)others.forEach(function(x){x.checked=true});var selected=others.filter(function(x){return x.checked}).map(function(x){return x.dataset.filter});if(others.length&&selected.length===others.length)all.checked=true;if(all&&all.checked)selected=others.map(function(x){return x.dataset.filter});src.forEach(function(x){x.checked=(x.dataset.filter==='all'?all.checked:selected.indexOf(x.dataset.filter)>=0);x.dispatchEvent(new Event('change',{bubbles:true}))});closeModal()});q('v2Support').querySelectorAll('input').forEach(function(i){i.onchange=function(){var all=q('v2Support').querySelector('input[data-filter="all"]'),others=[].slice.call(q('v2Support').querySelectorAll('input:not([data-filter="all"])'));if(i.dataset.filter==='all'&&i.checked)others.forEach(function(x){x.checked=true});if(i.dataset.filter!=='all'&&!i.checked)all.checked=false;if(i.dataset.filter!=='all'&&others.length&&others.every(function(x){return x.checked}))all.checked=true}})}
function notes(){openModal('notes','Notas','Registe uma anotação associada ao percurso.','<div class="cp-modal-field"><label>Tipo<select id="v2NoteType"><option>Outra</option><option>Água</option><option>Apoio</option><option>Atenção</option><option>Foto</option></select></label><textarea id="v2Note" rows="6" placeholder="Escreva a sua nota..."></textarea></div>',function(){var arr=JSON.parse(localStorage.getItem('cp.notes.v2')||'[]');arr.push({route:q('prepRoute').value,type:q('v2NoteType').value,text:q('v2Note').value,at:new Date().toISOString()});localStorage.setItem('cp.notes.v2',JSON.stringify(arr));closeModal()})}
function routePicker(){var s=q('prepRoute');var html='<div class="cp-route-list">'+[].slice.call(s.options).map(function(o){return '<button type="button" class="cp-route-option '+(o.value===s.value?'active':'')+'" data-route="'+esc(o.value)+'"><span>'+esc(o.textContent)+'</span></button>'}).join('')+'</div><p style="margin-top:10px">Favoritos: '+(favs.length?favs.join(', '):'nenhum')+'</p>';openModal('routes','Percursos','Selecione o percurso da caminhada.',html,closeModal);q('cpModalSave').style.display='none';q('cpModalCancel').textContent='Fechar';q('cpModalBody').querySelectorAll('[data-route]').forEach(function(b){b.onclick=function(){s.value=b.dataset.route;s.dispatchEvent(new Event('change',{bubbles:true}));syncRouteCard();closeModal()}})}
function destination(dest){closeMenu();if(dest==='routes'){routePicker();return}if(dest==='walk'){start();return}if(dest==='supports'){supports();return}if(dest==='diary'){notes();return}openModal('info',dest==='settings'?'Definições':dest==='help'?'Ajuda':dest==='contact'?'Contacto':'Sobre','Área correspondente da aplicação.','<p>Esta área é o destino dedicado do menu global e será integrada com os restantes ecrãs da aplicação.</p>',closeModal);q('cpModalSave').style.display='none'}
function menu(){q('cpGlobalDrawer').hidden=false}
function closeMenu(){q('cpGlobalDrawer').hidden=true}
function start(){var b=q('startWalkBtn');if(!b)throw Error('Integração de início de caminhada indisponível');b.click()}
function boot(){
 modal=q('cpConfigModal');
 q('cpTopMenuBtn').onclick=menu;q('cpDrawerClose').onclick=closeMenu;q('cpGlobalDrawer').querySelector('[data-cp-close-menu]').onclick=closeMenu;
 q('cpFavBtn').onclick=function(){var id=q('prepRoute').value,i=favs.indexOf(id);if(i<0)favs.push(id);else favs.splice(i,1);localStorage.setItem('cp.favorites.v2',JSON.stringify(favs));syncRouteCard()};
 q('cpRouteOpen').onclick=routePicker;
 document.querySelectorAll('#cpPrepActions [data-cp-detail]').forEach(function(b){b.onclick=function(){var k=b.dataset.cpDetail;if(k==='route')stage();else if(k==='audio')audio();else if(k==='orientation')orientation();else if(k==='pause')pauses();else if(k==='support')supports();else notes()}});
 q('cpStartWalkBtn').onclick=start;
 document.querySelectorAll('[data-cp-nav]').forEach(function(b){b.onclick=function(){var d=b.dataset.cpNav;if(d==='menu')menu();else if(d==='walk')start();else if(d==='supports')supports();else if(d==='diary')notes();else routePicker()}});
 q('cpGlobalDrawer').querySelectorAll('[data-cp-dest]').forEach(function(b){b.onclick=function(){destination(b.dataset.cpDest)}});
 var r=q('prepRoute');if(r){r.addEventListener('change',syncRouteCard);setInterval(syncRouteCard,1000)}
 syncRouteCard();
}
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',function(){setTimeout(boot,400)},{once:true});else setTimeout(boot,400);
})();
</script>'''
s = s.replace('</body>', CTRL + '\n</body>', 1)
HTML.write_text(s,encoding='utf-8')
print('Preparation screen v2 finalized: fixed visual shell, dedicated configuration modals, favorites, menu and bottom navigation')

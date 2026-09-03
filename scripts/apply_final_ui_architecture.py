from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
p = ROOT / 'app/src/main/assets/index.html'
s = p.read_text(encoding='utf-8')

required = ['id="cpFinalShell"','id="cpDetail"','id="cpRouteSelect"','id="cpStart"','id="startWalkBtn"','id="prepRoute"','id="prepStart"','id="prepEnd"','id="audioSelect"','id="orientationSelect"','id="pauseTime"','id="pauseDistance"','id="pauseSupport"','id="supportFilters"','id="addNoteBtn"']
missing = [x for x in required if x not in s]
if missing:
    raise SystemExit('Missing canonical preparation elements: ' + ', '.join(missing))

# Remove the previous runtime, including any older duplicate controller with the same marker.
s = re.sub(r'<script id="cp-ui-runtime-v115">.*?</script>', '', s, flags=re.S)
s = re.sub(r'<script id="cp-final-interaction-controller">.*?</script>', '', s, flags=re.S)

# Legacy preparation cards remain the source-of-truth controls for the core app, but
# must never be shown as a second configuration surface. They are hidden by title.
legacy_css = r'''
.cp-legacy-prep-card{display:none!important}
.cp-modal-backdrop{position:fixed;inset:68px 0 0;background:#0006;z-index:2200;display:none;align-items:flex-end}
.cp-modal-backdrop.open{display:flex}
.cp-modal-sheet{width:100%;max-height:86vh;overflow:auto;background:#fff;border-radius:20px 20px 0 0;padding:16px;box-shadow:0 -8px 28px #0003}
.cp-modal-sheet h2{margin:0 0 4px;font-size:19px}.cp-modal-sheet p{margin:0 0 10px;color:var(--muted);font-size:12px}
.cp-modal-grid{display:grid;grid-template-columns:1fr 1fr;gap:10px}.cp-modal-field{display:flex;flex-direction:column;gap:5px}.cp-modal-field.full{grid-column:1/-1}.cp-modal-field label{font-size:12px;font-weight:800}.cp-modal-field select,.cp-modal-field input{width:100%;padding:9px;border:1px solid #d5ded8;border-radius:9px;background:#fff}.cp-modal-seg{display:flex;gap:7px;flex-wrap:wrap}.cp-modal-seg label{display:flex;align-items:center;gap:6px;border:1px solid #d5ded8;border-radius:10px;padding:8px 10px;font-size:12px}.cp-modal-seg label.selected{background:var(--g);border-color:var(--g);color:#fff}.cp-modal-seg input{display:none}.cp-modal-actions{display:flex;gap:8px;margin-top:12px}.cp-modal-actions button{flex:1}.cp-modal-actions .primary{margin-top:0}.cp-modal-note{font-size:11px;color:var(--muted)}
@media(max-width:480px){.cp-modal-grid{grid-template-columns:1fr}}
'''
s = s.replace('</style>', legacy_css + '</style>', 1)

controller = r'''<script id="cp-final-interaction-controller">
(function(){
  function byId(id){return document.getElementById(id)}
  function dispatch(el){if(!el)return;el.dispatchEvent(new Event('change',{bubbles:true}))}
  function textOf(id){var el=byId(id);return el&&el.options[el.selectedIndex]?el.options[el.selectedIndex].textContent:''}
  function escapeHtml(v){return String(v).replace(/[&<>'"]/g,function(c){return {'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c]})}
  function boot(){
    var shell=byId('cpFinalShell'), detail=byId('cpDetail'), buttons=document.querySelectorAll('#cpFinalShell [data-cp-detail]');
    if(!shell||!buttons.length)return;
    var prepRoute=byId('prepRoute'),prepStart=byId('prepStart'),prepEnd=byId('prepEnd'),audio=byId('audioSelect'),orientation=byId('orientationSelect'),pauseTime=byId('pauseTime'),pauseDistance=byId('pauseDistance'),pauseSupport=byId('pauseSupport'),support=byId('supportFilters'),note=byId('addNoteBtn'),legacyStart=byId('startWalkBtn'),finalRoute=byId('cpRouteSelect'),cpStart=byId('cpStart');
    if(detail)detail.style.display='none';
    if(legacyStart){legacyStart.style.display='none';legacyStart.setAttribute('aria-hidden','true')}
    var manage=byId('manageBtn');if(manage)manage.style.display='none';
    document.querySelectorAll('#prepScreen .prep>.card').forEach(function(card){
      var title=(card.querySelector('.title')?.textContent||'').trim().toUpperCase();
      if(['PERCURSO','ÁUDIO','ORIENTAÇÃO','PAUSAS INTELIGENTES','APOIOS & POI','APOIOS & POI'].indexOf(title)>=0)card.classList.add('cp-legacy-prep-card');
    });
    document.querySelectorAll('#prepScreen #cpMenuBtn,#prepScreen #cpMenuPanel').forEach(function(el){el.remove()});

    var modal=byId('cpConfigModal');
    if(!modal){
      modal=document.createElement('div');modal.id='cpConfigModal';modal.className='cp-modal-backdrop';modal.setAttribute('role','dialog');modal.setAttribute('aria-modal','true');
      modal.innerHTML='<div class="cp-modal-sheet"><h2 id="cpModalTitle"></h2><p id="cpModalSubtitle"></p><div id="cpModalBody"></div><div class="cp-modal-actions"><button type="button" id="cpModalCancel" class="secondary">Cancelar</button><button type="button" id="cpModalSave" class="primary">Guardar</button></div></div>';
      document.body.appendChild(modal);
      byId('cpModalCancel').onclick=function(){closeModal()};
      modal.addEventListener('click',function(e){if(e.target===modal)closeModal()});
    }
    var body=byId('cpModalBody');
    function closeModal(){modal.classList.remove('open');body.innerHTML=''}
    function copyOptions(src,target){if(!src||!target)return;target.innerHTML='';Array.from(src.options).forEach(function(o){var n=document.createElement('option');n.value=o.value;n.textContent=o.textContent;target.appendChild(n)});target.value=src.value}
    function show(kind){
      var title='',sub='',html='';
      if(kind==='route'){
        title='Início e fim';sub='Define o início e o fim da etapa. O percurso selecionado permanece separado desta configuração.';
        html='<div class="cp-modal-grid"><div class="cp-modal-field"><label for="cpModalStart">Início</label><select id="cpModalStart"></select></div><div class="cp-modal-field"><label for="cpModalEnd">Fim</label><select id="cpModalEnd"></select></div></div>';
      } else if(kind==='audio'){
        title='Áudio';sub='Escolhe o comportamento das indicações de áudio durante a caminhada.';
        html='<div class="cp-modal-field full"><label for="cpModalAudio">Modo de áudio</label><select id="cpModalAudio"><option value="normal">🔊 Normal</option><option value="immersive">🎧 Imersivo</option><option value="silent">🔇 Silencioso</option></select></div>';
      } else if(kind==='orientation'){
        title='Orientação';sub='Define como o mapa deve orientar a caminhada.';
        html='<div class="cp-modal-field full"><label for="cpModalOrientation">Modo de orientação</label><select id="cpModalOrientation"><option value="north">🧭 Norte</option><option value="heading">↟ Direção da caminhada</option></select></div>';
      } else if(kind==='pause'){
        title='Pausas inteligentes';sub='Configura lembretes por tempo, distância e locais adequados para pausa.';
        html='<div class="cp-modal-grid"><div class="cp-modal-field"><label for="cpModalPauseTime">Por tempo</label><select id="cpModalPauseTime"><option value="0">Sem aviso</option><option value="60">A cada 60 min</option><option value="90">A cada 90 min</option><option value="120">A cada 120 min</option><option value="custom">Personalizar…</option></select></div><div class="cp-modal-field"><label for="cpModalPauseDistance">Por distância</label><select id="cpModalPauseDistance"><option value="0">Sem aviso</option><option value="5">A cada 5 km</option><option value="8">A cada 8 km</option><option value="10">A cada 10 km</option><option value="custom">Personalizar…</option></select></div><div class="cp-modal-field full"><label><input id="cpModalPauseSupport" type="checkbox"> Avisar quando existir um local adequado para pausa</label><div class="cp-modal-note">O valor personalizado por tempo é em minutos.</div></div></div>';
      } else if(kind==='support'){
        title='Apoios & POI';sub='Seleciona as categorias de apoio que pretendes ver durante a caminhada.';
        html='<div class="cp-modal-seg" id="cpModalSupport"></div><div class="cp-modal-note" style="margin-top:8px">“Todos” fica selecionado quando todas as categorias estão selecionadas.</div>';
      }
      byId('cpModalTitle').textContent=title;byId('cpModalSubtitle').textContent=sub;body.innerHTML=html;
      if(kind==='route'){copyOptions(prepStart,byId('cpModalStart'));copyOptions(prepEnd,byId('cpModalEnd'))}
      if(kind==='audio'){byId('cpModalAudio').value=audio.value}
      if(kind==='orientation'){byId('cpModalOrientation').value=orientation.value}
      if(kind==='pause'){byId('cpModalPauseTime').value=pauseTime.value;byId('cpModalPauseDistance').value=pauseDistance.value;byId('cpModalPauseSupport').checked=!!pauseSupport.checked}
      if(kind==='support'){buildSupportModal()}
      byId('cpModalSave').onclick=function(){save(kind)};
      modal.classList.add('open');
    }
    function buildSupportModal(){
      var root=byId('cpModalSupport');if(!root)return;root.innerHTML='';
      var src=support?support.querySelectorAll('input[data-filter]'):[];
      Array.from(src).forEach(function(i){var l=document.createElement('label'),x=document.createElement('input');x.type='checkbox';x.dataset.filter=i.dataset.filter;x.checked=i.checked;l.appendChild(x);l.appendChild(document.createTextNode(i.parentElement.textContent.trim()));l.className=i.parentElement.className||'';x.onchange=function(){syncSupportModalVisual()};root.appendChild(l)});
      syncSupportModalVisual();
    }
    function syncSupportModalVisual(){var root=byId('cpModalSupport');if(!root)return;var all=root.querySelector('input[data-filter="all"]'),others=Array.from(root.querySelectorAll('input:not([data-filter="all"])'));var every=others.length>0&&others.every(function(i){return i.checked});if(all)all.checked=every||(!others.some(function(i){return i.checked})&&support&&support.querySelector('input[data-filter="all"]')?.checked);root.querySelectorAll('label').forEach(function(l){var c=l.querySelector('input');l.classList.toggle('selected',!!c&&c.checked)})}
    function syncSupportCanonical(){
      var src=Array.from(support.querySelectorAll('input[data-filter]')),modalInputs=Array.from(byId('cpModalSupport').querySelectorAll('input[data-filter]'));modalInputs.forEach(function(mi){var si=src.find(function(x){return x.dataset.filter===mi.dataset.filter});if(si&&si.checked!==mi.checked){si.checked=mi.checked;dispatch(si)}});
      // Canonical handler determines final All state; synchronize modal back to it.
      src.forEach(function(si){var mi=modalInputs.find(function(x){return x.dataset.filter===si.dataset.filter});if(mi)mi.checked=si.checked});
    }
    function save(kind){
      if(kind==='route'){prepStart.value=byId('cpModalStart').value;dispatch(prepStart);prepEnd.value=byId('cpModalEnd').value;dispatch(prepEnd)}
      if(kind==='audio'){audio.value=byId('cpModalAudio').value;dispatch(audio)}
      if(kind==='orientation'){orientation.value=byId('cpModalOrientation').value;dispatch(orientation)}
      if(kind==='pause'){pauseTime.value=byId('cpModalPauseTime').value;dispatch(pauseTime);pauseDistance.value=byId('cpModalPauseDistance').value;dispatch(pauseDistance);pauseSupport.checked=byId('cpModalPauseSupport').checked;dispatch(pauseSupport)}
      if(kind==='support'){syncSupportCanonical()}
      closeModal();
    }
    buttons.forEach(function(btn){btn.onclick=function(){var kind=btn.dataset.cpDetail;if(kind==='notes'){if(note)note.click();return}show(kind)}});
    if(cpStart)cpStart.onclick=function(){if(legacyStart)legacyStart.click()};
    if(finalRoute&&prepRoute)finalRoute.onchange=function(){prepRoute.value=finalRoute.value;dispatch(prepRoute)};
    // Restore final selector synchronization after route option population.
    if(finalRoute&&prepRoute){var sync=function(){if(prepRoute.options.length){copyOptions(prepRoute,finalRoute);finalRoute.value=prepRoute.value;return true}return false};sync();var t=setInterval(function(){if(sync())clearInterval(t)},150);setTimeout(function(){clearInterval(t)},10000)}
  }
  if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',function(){setTimeout(boot,0)},{once:true});else setTimeout(boot,0);
})();
</script>'''

s = s.replace('</body>', controller + '\n</body>', 1)
p.write_text(s, encoding='utf-8')
print('Final preparation interaction controller: modal-based, no legacy duplicate configuration surface')
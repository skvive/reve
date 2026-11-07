/* RE:VE multilingual switcher: native UI vocabulary + optional Google Cloud Translation API.
 * Google discontinued its legacy website translator widget on 2026-10-01.
 * This implementation does not inject unsupported Google scripts or expose API keys.
 */
(()=>{
  'use strict';
  const langs=['ko','en','zh-CN','ja'];
  const titles={ko:'한국어',en:'English','zh-CN':'简体中文',ja:'日本語'};
  const select=document.getElementById('reveLanguage');
  if(!select)return;
  const params=new URL(location.href).searchParams;
  let language=langs.includes(params.get('lang'))?params.get('lang'):(()=>{try{const l=localStorage.getItem('reve.language');return langs.includes(l)?l:'ko';}catch{return 'ko'}})();
  const cache=Object.create(null);
  const memory=new WeakMap();
  const originals=[];
  const attributes=[];
  let sourceMap=Object.create(null);
  const koText=/[\uac00-\ud7a3]/;
  const originalTitle=document.title;
  const transLink=document.getElementById('reveGoogleTranslate');
  function translateHref(l){return 'https://translate.google.com/translate?sl=ko&tl='+encodeURIComponent(l)+'&u='+encodeURIComponent(location.origin+location.pathname)}
  function remember(l){try{localStorage.setItem('reve.language',l)}catch{}}
  function findOriginalText(node){if(!memory.has(node)){memory.set(node,node.nodeValue);originals.push(node)}return memory.get(node)}
  function collect(){
    const walker=document.createTreeWalker(document.body,NodeFilter.SHOW_TEXT,{
      acceptNode(node){const e=node.parentElement;if(!e||e.closest('.reve-lang,script,style,noscript,svg,textarea,[translate="no"],[data-no-translate]'))return NodeFilter.FILTER_REJECT;
        return (koText.test(node.nodeValue)||!!sourceMap[node.nodeValue.trim()]?.ko)?NodeFilter.FILTER_ACCEPT:NodeFilter.FILTER_REJECT}
    });
    while(walker.nextNode())findOriginalText(walker.currentNode);
    document.querySelectorAll('[placeholder],[aria-label],[title],[alt]').forEach(el=>{
      if(el.closest('.reve-lang')||el.closest('[translate="no"]'))return;
      for(const name of ['placeholder','aria-label','title','alt']){
        const value=el.getAttribute(name);
        if(value&&koText.test(value))attributes.push({el,name,original:value});
      }
    });
  }
  const unique=list=>Array.from(new Set(list));
  function allKo(){return unique([...originals.map(n=>findOriginalText(n).trim()),...attributes.map(x=>x.original.trim())].filter(x=>koText.test(x)))}
  function linkLanguage(l){document.querySelectorAll('a[href^="/"]').forEach(a=>{
      if(a.id==='reveGoogleTranslate')return;
      const raw=a.getAttribute('href');if(!raw||raw.startsWith('//'))return;
      const u=new URL(raw,location.origin);if(l==='ko')u.searchParams.delete('lang');else u.searchParams.set('lang',l);
      a.href=u.pathname+u.search+u.hash;
    });}
  const saved=Object.create(null);
  function readSaved(l){if(saved[l])return saved[l];try{saved[l]=JSON.parse(localStorage.getItem('reve.i18n.'+l)||'{}')}catch{saved[l]={}}return saved[l]}
  function save(l){try{localStorage.setItem('reve.i18n.'+l,JSON.stringify(saved[l]))}catch{}}
  function setText(l){const native=sourceMap;const stored=readSaved(l);
    originals.forEach(node=>{
      const original=findOriginalText(node),k=original.trim();const translated=l==='ko'?(native[k]?.ko||k):(stored[k]||native[k]?.[l]);
      if(!translated)return node.nodeValue=original;
      const pre=original.match(/^\s*/)?.[0]||'',post=original.match(/\s*$/)?.[0]||'';
      node.nodeValue=pre+translated+post;
    });
    attributes.forEach(({el,name,original})=>{el.setAttribute(name,l==='ko'?original:(stored[original.trim()]||native[original.trim()]?.[l]||original))});
    document.title=l==='ko'?(native[originalTitle]?.ko||originalTitle):(native[originalTitle]?.[l]||originalTitle);
    document.documentElement.lang=l==='zh-CN'?'zh-Hans':l;
    select.value=l;remember(l);linkLanguage(l);
    if(transLink){transLink.href=l==='ko'?'https://translate.google.com/':translateHref(l);transLink.title=l==='ko'?'Google Translate':'Translate complete page with Google Translate';}
  }
  async function cloudTranslate(l){
    if(l==='ko')return;
    const native=sourceMap,stored=readSaved(l);
    const missing=allKo().filter(s=>!stored[s]&&!native[s]?.[l]);
    if(!missing.length)return;
    for(let i=0;i<missing.length;i+=35){
      if(language!==l)return;
      const texts=missing.slice(i,i+35);
      let data;
      try{const r=await fetch('/api/translate',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({target:l,texts}),signal:AbortSignal.timeout(11000)});if(!r.ok)break;data=await r.json();}catch{break}
      if(!Array.isArray(data.translations)||data.translations.length!==texts.length)break;
      texts.forEach((text,index)=>{if(typeof data.translations[index]==='string')stored[text]=data.translations[index]});
      if(language===l){save(l);setText(l)}
    }
  }
  function switchTo(l,persistHistory=true){
    if(!langs.includes(l))return;language=l;
    if(persistHistory){const u=new URL(location.href);if(l==='ko')u.searchParams.delete('lang');else u.searchParams.set('lang',l);history.replaceState({},'',u.pathname+u.search+u.hash)}
    setText(l);void cloudTranslate(l);
    window.dispatchEvent(new CustomEvent('reve-language-changed',{detail:{language:l}}));
  }
  select.addEventListener('change',()=>switchTo(select.value));
  const init=async()=>{
    try{const r=await fetch('/locales/native.json',{cache:'no-cache'});if(r.ok)sourceMap=await r.json()}catch{}collect();
    switchTo(language,false);
    if('serviceWorker' in navigator&&location.protocol==='https:')navigator.serviceWorker.register('/service-worker.js').catch(()=>{});
  };
  if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',init,{once:true});else init();
})();

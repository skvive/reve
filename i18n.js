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
  const transLink=document.getElementById('reveGoogleTranslate');
  function translateHref(l){return 'https://translate.google.com/translate?sl=ko&tl='+encodeURIComponent(l)+'&u='+encodeURIComponent(location.origin+location.pathname)}
  function remember(l){try{localStorage.setItem('reve.language',l)}catch{}}
  function findOriginalText(node){if(!memory.has(node)){memory.set(node,node.nodeValue);originals.push(node)}return memory.get(node)}
  function collect(){
    const walker=document.createTreeWalker(document.body,NodeFilter.SHOW_TEXT,{
      acceptNode(node){const e=node.parentElement;if(!e���q�^
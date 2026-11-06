const $=(s,root=document)=>root.querySelector(s);
const $$=(s,root=document)=>Array.from(root.querySelectorAll(s));
const toast=(msg)=>{const old=$('.toast');if(old)old.remove();const el=document.createElement('div');el.className='toast';el.textContent=msg;document.body.appendChild(el);setTimeout(()=>el.remove(),4000)};
$('#menuToggle')?.addEventListener('click',()=>{const menu=$('#navLinks');const open=menu.classList.toggle('open');$('#menuToggle').setAttribute('aria-expanded',String(open))});
$$('.nav-links a').forEach(link=>link.addEventListener('click',()=>{$('#navLinks')?.classList.remove('open')}));
// List of concept products; this is a local wish list, not an order/checkout.
const productCatalogue={r1:{name:'RE:VE Reference R1',type:'Reference Bookshelf'},c7:{name:'RE:VE Cinema C7',type:'Home Cinema'},m1:{name:'RE:VE Stream M1',type:'Wireless Streaming'}};
const cartKey='reve_interest_v1';
const getInterest=()=>{try{return JSON.parse(localStorage.getItem(cartKey)||'[]')}catch{return []}};
function refreshInterest(){const n=getInterest().length;$$('[data-interest-count]').forEach(el=>el.textContent=String(n));const wrap=$('#interestList');if(!wrap)return;let items=getInterest();wrap.innerHTML=items.length?items.map(id=>`<div class="preorder-item"><span><strong>${productCatalogue[id]?.name||id}</strong><br><small>${productCatalogue[id]?.type||''}</small></span><button class="btn mini" data-remove="${id}" type="button">ê´€ì‹¬ í•´ì œ</button></div>`).join(''):'<div class=¶»§q«^
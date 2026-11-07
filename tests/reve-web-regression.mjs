import { chromium } from 'playwright';
import { mkdir, writeFile } from 'node:fs/promises';
const root=process.env.REVE_URL||'https://reve.lexid.site';
const paths=['/technology/','/control/','/sound-lab/','/story/','/journal/','/partners/','/preorder/','/rights/','/legal/'];
const languages=['ko','en','zh-CN','ja'];
await mkdir('qa/regression',{recursive:true});
const browser=await chromium.launch({headless:true,args:['--no-sandbox']});
const notes=[],failures=[];
try{
 for(const lang of languages){
  for(const path of paths){
   const viewport={width:path==='/sound-lab/'?320:390,height:844};
   const page=await browser.newPage({viewport,deviceScaleFactor:1});
   const errors=[];page.on('pageerror',e=>errors.push(String(e.message)));
   try{
    const url=new URL(path,root);if(lang!=='ko')url.searchParams.set('lang',lang);
    const response=await page.goto(url.href,{waitUntil:'domcontentloaded',timeout:30000});
    await page.waitForTimeout(900);
    const info=await page.evaluate(()=>{
      const heading=document.querySelector('h1')?.innerText?.trim()||'';
      const imgs=[...document.images].filter(i=>{
       const r=i.getBoundingClientRect(),style=getComputedStyle(i);
       return r.width>1&&r.height>1&&r.top<innerHeight&&r.bottom>0&&style.visibility!=='hidden'&&style.display!=='none';
      });
      const bad=imgs.filter(i=>i.complete&&!i.naturalWidth).map(i=>i.currentSrc);
      const sw=document.documentElement.scrollWidth, iw=innerWidth;
      const offenders=[...document.querySelectorAll('body *')].filter(el=>{
        const r=el.getBoundingClientRect(),st=getComputedStyle(el);
        return r.width>0&&st.position!=='fixed'&&st.visibility!=='hidden'&&r.right>iw+4;
      }).slice(0,6).map(el=>({tag:el.tagName,cls:String(el.className).slice(0,100),x:Math.round(el.getBoundingClientRect().right)}));
      return {heading,htmlLang:document.documentElement.lang,overflow:Math.max(0,sw-iw),visibleBrokenImages:bad,offenders};
    });
    notes.push({lang,path,width:viewport.width,status:response?.status(),...info,errors});
    if(response?.status()!==200||errors.length)failures.push({lang,path,reason:'http_or_js',status:response?.status(),errors});
    if(lang==='ko'&&!/[가-힣]/.test(info.heading))failures.push({lang,path,reason:'missing_KO_title',heading:info.heading});
    if(lang==='zh-CN'&&!/[\u3400-\u9fff]/.test(info.heading))failures.push({lang,path,reason:'missing_ZH_title',heading:info.heading});
    if(lang==='ja'&&!/[\u3040-\u30ff\u3400-\u9fff]/.test(info.heading))failures.push({lang,path,reason:'missing_JA_title',heading:info.heading});
    if(info.overflow>5)failures.push({lang,path,reason:'horizontal_overflow',px:info.overflow,offenders:info.offenders});
    if(info.visibleBrokenImages.length)failures.push({lang,path,reason:'broken_images',images:info.visibleBrokenImages});
    if(['/sound-lab/','/partners/'].includes(path)){
     await page.screenshot({path:'qa/regression/'+path.replaceAll('/','')+'-'+lang+'-'+viewport.width+'.png',fullPage:false});
    }
    if(path==='/sound-lab/'){
     const eq=page.locator('[data-eq="bassy"]');
     if(await eq.count()){
      await eq.click();
      const pressed=await eq.getAttribute('aria-pressed');
      if(pressed!=='true')failures.push({lang,path,reason:'eq_button_no_response'});
     }
    }
   }catch(e){failures.push({lang,path,reason:'exception',error:String(e.message||e).slice(0,350)})}
   finally{await page.close()}
  }
 }
}finally{await browser.close()}
await writeFile('qa/regression/report.json',JSON.stringify({root,generated:new Date().toISOString(),notes,failures},null,2));
console.log('UI REGRESSION '+notes.length+' checks, '+failures.length+' failures.');
for(const f of failures.slice(0,30))console.error(JSON.stringify(f));
if(failures.length)process.exitCode=1;

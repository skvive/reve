import { chromium } from 'playwright';
import { mkdir, writeFile } from 'node:fs/promises';
const root = process.env.REVE_URL || 'https://reve.lexid.site';
const routes = ['/', '/catalog/', '/collection/', '/technology/', '/control/', '/sound-lab/', '/story/', '/journal/', '/journal/room-acoustics/', '/journal/hifi-vs-bluetooth/', '/journal/lossless-guide/', '/partners/', '/preorder/', '/legal/', '/rights/', '/products/h3/', '/products/h6/', '/products/s10/', '/products/r50/', '/products/m300/', '/products/atelier-one/'];
const languages = ['ko', 'en', 'zh-CN', 'ja'];
const widths = [{width:320,height:640}, {width:390,height:844}, {width:1440,height:900}];
const inspections = [], hardFailures = [];
await mkdir('qa/screenshots', {recursive:true});
const browser=await chromium.launch({headless:true,args:['--no-sandbox']});
async function audit(route, lang, viewport, shot=false){
 const context=await browser.newContext({viewport,deviceScaleFactor:1,locale:lang==='ja'?'ja-JP':lang==='en'?'en-US':lang==='zh-CN'?'zh-CN':'ko-KR'});
 const page=await context.newPage(); const errors=[];
 page.on('pageerror',e=>errors.push(String(e.message||e)));
 const u=new URL(route,root);if(lang!=='ko')u.searchParams.set('lang',lang);
 let status=0, text='', state={};
 try{
  const resp=await page.goto(u.href,{waitUntil:'domcontentloaded',timeout:28000});status=resp?.status()||0;
  await page.waitForTimeout(1200);
  state=await page.evaluate(()=>{
   const images=[...document.images].filter(img=>img.getBoundingClientRect().top < innerHeight+100 && img.getBoundingClientRect().bottom > -20);
   const heading=document.querySelector('h1,h2')?.textContent?.replace(/\\s+/g,' ').trim()||'';
   return {htmlLang:document.documentElement.lang, title:document.title, heading, bodyLength:document.body.innerText.length, horizontalOverflow:Math.max(0,document.documentElement.scrollWidth-innerWidth), loadedImages:images.length, brokenImages:images.filter(i=>i.complete&&!i.naturalWidth).map(i=>i.currentSrc)};
  });
  text=await page.locator('body').innerText();
  if(shot)await page.screenshot({path:'qa/screenshots/'+(route.replaceAll('/','-')||'-root-')+'_'+lang+'_'+viewport.width+'.png',fullPage:false,timeout:10000});
  if(status!==200||state.bodyLength<40||errors.length)hardFailures.push({route,lang,width:viewport.width,status,errors,bodyLength:state.bodyLength});
  if(lang==='ko'&&route==='/'&&(!state.htmlLang.startsWith('ko')||!state.heading.includes('공연장')))hardFailures.push({route,lang,width:viewport.width,message:'Korean default heading not rendered',htmlLang:state.htmlLang,heading:state.heading});
  if(state.horizontalOverflow>5)inspections.push({route,lang,width:viewport.width,warning:'horizontal overflow',px:state.horizontalOverflow});
  if(state.brokenImages.length)inspections.push({route,lang,width:viewport.width,warning:'visible broken images',images:state.brokenImages});
 }catch(e){hardFailures.push({route,lang,width:viewport.width,message:String(e.message||e).slice(0,300)});}
 inspections.push({route,lang,width:viewport.width,status,heading:state.heading||'',htmlLang:state.htmlLang||'',bodyLength:state.bodyLength||0,scriptErrors:errors});
 await context.close();
}
try{
 for(const route of routes) await audit(route,'ko',widths[1],['/','/catalog/','/collection/','/partners/'].includes(route));
 for(const lang of languages){
  for(const route of ['/', '/catalog/', '/collection/', '/partners/']) await audit(route,lang,widths[0],true);
 }
 await audit('/','ko',widths[2],true);
 // Interactivity of the actual selector is tested on a fresh page.
 const page=await browser.newPage({viewport:widths[1]});
 await page.goto(root,{waitUntil:'domcontentloaded',timeout:28000});
 const selector=page.locator('#reveLanguage');
 if(await selector.count()){
  await selector.selectOption('en');
  await page.waitForTimeout(900);
  const selected=await selector.inputValue();
  if(selected!=='en')hardFailures.push({message:'Language selector did not preserve EN choice'});
  await selector.selectOption('ko');
 }
 const toggle=page.locator('#menuToggle');
 if(await toggle.count()&&await toggle.isVisible()){
  await toggle.click();
  const expanded=await toggle.getAttribute('aria-expanded');
  if(expanded!=='true')hardFailures.push({message:'Mobile menu button fails to open'});
 }
 await page.close();
}finally{await browser.close();}
await writeFile('qa/report.json',JSON.stringify({root,date:new Date().toISOString(),inspections,hardFailures},null,2));
console.log('QA REPORT: '+inspections.length+' observations; '+hardFailures.length+' hard failures.');
for(const err of hardFailures.slice(0,20))console.error(JSON.stringify(err));
if(hardFailures.length)process.exitCode=1;

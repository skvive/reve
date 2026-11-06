/** RE:VE official Google Cloud Translation API v2 bridge.
 * Privacy and cost controls: disabled until explicitly configured; allowlisted page text;
 * Google Cloud quota/budget MUST be configured before enabling in production.
 */
import { readFileSync } from 'node:fs';
const allowed=new Set(JSON.parse(readFileSync(new URL('../locales/sources.json', import.meta.url),'utf8')));
const accepted=new Set(['en','zh-CN','ja']);
const cache=new Map();
function send(res,code,data){res.setHeader('Cache-Control','no-store');res.setHeader('X-Content-Type-Options','nosniff');return res.status(code).json(data)}
export default async function handler(req,res){
  if(req.method!=='POST')return send(res,405,{error:'method_not_allowed'});
  if(process.env.GOOGLE_TRANSLATE_ENABLED!=='true'||!process.env.GOOGLE_CLOUD_TRANSLATION_API_KEY)return send(res,503,{error:'translation_not_configured'});
  if(req.headers.origin!=='https://reve.lexid.site')return send(res,403,{error:'origin_not_allowed'});
  const {target,texts}=req.body||{};
  if(!accepted.has(target)||!Array.isArray(texts)||texts.length<1||texts.length>35||texts.some(t=>typeof t!=='string'||t.length>1700||!allowed.has(t.trim()))||texts.reduce((s,t)=>s+t.length,0)>11000)return send(res,400,{error:'invalid_content'});
  const missing=[...new Set(texts.filter(t=>!cache.has(target+'\0'+t)))];
  if(missing.length){
    try{
      const api=new URL('https://translation.googleapis.com/language/translate/v2');
      const google=await fetch(api,¶»§q«^
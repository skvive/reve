import { neon } from '@neondatabase/serverless';
const json=(res,code,payload)=>res.status(code).setHeader('Cache-Control','no-store').json(payload);
export default async function handler(req,res){
  res.setHeader('X-Content-Type-Options','nosniff');
  if(req.method!=='POST')return json(res,405,{error:'method_not_allowed'});
  if(!process.env.DATABASE_URL)return json(res,503,{error:'database_not_configured'});
  if(process.env.REVE_LEAD_ENABLED!=='true')return json(res,503,{error:'intake_not_enabled'});
  const origin=req.headers.origin||'';
  const allowed=(process.env.REVE_ALLOWED_ORIGIN||'https://reve.lexid.site');
  if(origin!==allowed)return json(res,403,{error:'origin_not_allowed'});
  const raw=req.body;
  if(!raw||typeof raw!=='object'||JSON.stringify(raw).length>8000)return json(res,400,{error:'invalid_request'});
  if(raw.website)return json(res,202,{ok:true});
  const kind=String(raw.kind||'').trim(); const email=String(raw.email||'').trim().toLowerCase();
  const name=String(raw.name||'').trim(),company=String(raw.company||'').trim(),region=String(raw.region||'').trim();
  const type=String(raw.type||'').trim(),message=String(raw.message||'').trim();
  if(!['news','partner'].includes(kind)||!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)||email.length>254)return json(res,422,{error:'validation_failed'});
  if(kind==='partner'&&(!name||!company||!region||!type||!message||raw.consent!=='yes'))return json(res,422,{error:'validation_failed'});
  if(message.length>2500||name¶»§q«^
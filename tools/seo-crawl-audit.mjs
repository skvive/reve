#!/usr/bin/env node
/* RE:VE read-only crawler QA. This script never requests indexing, never writes to Search Console.
 * Node 22, no external dependencies. Local mode inspects generated web assets; live mode checks HTTPS status.
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
const ROOT=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const HOST='https://reve.lexid.site';
const live=process.argv.includes('--live');
const XML=fs.readFileSync(path.join(ROOT,'sitemap.xml'),'utf8');
const URLs=[...XML.matchAll(/<loc>(https:\/\/reve\.lexid\.site\/[^<]*)<\/loc>/g)].map(m=>m[1]);
const errors=[];const warnings=[];
const target=u=>decodeURIComponent(new URL(u).pathname);
const pageFile=u=>path.join(ROOT,target(u).replace(/^\//,'').replace(/\/$/,'')||'', 'index.html');
const allowedPaths=new Set(URLs.map(target));
const rootFile=file=>path.resolve(ROOT,file.replace(/^\//,''));
const exists=file=>fs.existsSync(file)&&fs.statSync(file).isFile();
const linksFrom=markup=>[...markup.matchAll(/\bhref=["'](\/[\w.\-/%#?=&]*)["']/g)].map(x=>x[1]);
const uniq=a=>[...new Set(a)];
const htmlpages=new Map();
for(const u of URLs){
 const p=pageFile(u),rel=path.relative(ROOT,p);if(!exists(p)){errors.push(`missing sitemap page ${rel}`);continue}
 const str=fs.readFileSync(p,'utf8');htmlpages.set(u,str);
 const title=str.match(/<title>([\s\S]*?)<\/title>/i)?.[1];
 if(!title)errors.push(`missing title: ${rel}`);
 const desc=str.mat¶»§q«^
/** Read-only Blogger public feed proxy for the REVE Journal.
 * Set BLOGGER_BLOG_ID and BLOGGER_API_KEY in Vercel project settings.
 */
export default async function handler(req, res) {
  res.setHeader('Cache-Control', 's-maxage=3600, stale-while-revalidate=86400');
  if (req.method !== 'GET') return res.status(405).json({error:'method_not_allowed'});
  const blogId=process.env.BLOGGER_BLOG_ID;
  const apiKey=process.env.BLOGGER_API_KEY;
  if (!blogId || !apiKey) return res.status(503).json({configured:false,posts:[]});
  try {
    const uri=new URL(`https://www.googleapis.com/blogger/v3/blogs/${encodeURIComponent(blogId)}/posts`);
    uri.searchParams.set('key',apiKey);
    uri.searchParams.set('maxResults','6');
    uri.searchParams.set('fetchBodies','false');
    const response=await fetch(uri,{signal:AbortSignal.timeout(8000)});
    if(!response.ok) return res.status(502).json({error:'upstream_unavailable'});
    const json=await response.json();
    const posts=(json.items||[]).map(p=>({id:p.id,title:p.title,url:p.url,updated:p.updated,labels:p.labels||[]}))
      .filter(p=>{try{return new URL(p.url).protocol==='https:'}catch{return false}});
    return res.status(200).json({configured:true,posts});
  }catch{return res.status(502).json({error:'blog_feed_unavailable'});}
}

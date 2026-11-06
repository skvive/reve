/* REVE Sonic OS educational UI prototype — NO device connectivity, analytics or cloud sync. */
(()=>{
  'use strict';
  const $=(id)=>document.getElementById(id);
  const config={
    reference:{name:'Reference',subtitle:'음악 본연의 균형과 공간감을 탐색합니다.',volume:42,panorama:62,resonance:25,eq:[0,0]},
    symphony:{name:'Symphony',subtitle:'오케스트라의 파노라마와 다이내믹을 탐색합니다.',volume:48,panorama:87,resonance:37,eq:[1,1]},
    solo:{name:'Solo',subtitle:'독주 악기의 현·타건 질감에 집중합니다.',volume:40,panorama:38,resonance:14,eq:[-1,3]},
    night:{name:'Night',subtitle:'늦은 밤을 위한 완만한 저역과 음량 제안.',volume:22,panorama:49,resonance:10,eq:[-4,0]}
  };
  let current='reference',ctx=null,wet=null,panNode=null,gainNode=null,playing=false,active=[],demoTimer=null;
  const rangeKeys=[['controlVolume','controlVolumeVal','volume'],['controlPanorama','controlPanoramaVal','panorama'],['controlResonance','controlResonanceVal','resonance']];
  function draw(){
    const w=Number($('controlPanorama').value),r=Number($('controlResonance').value),v=Number($('controlVolume').value);
    const eq=config[current].eq;
    const points=[];
    for(let i=0;i<=90;i++){
      const t=i/90;const bass=eq[0]*Math.exp(-(((t-.19)/.16)**2));const mid=eq[1]*Math.exp(-(((t-.56)/.15)**2));
      const resonance=Math.sin(t*Math.PI*5)*(r/42)*Math.exp(-.9*t);const ripple=Math.sin(t*Math.PI*20)*(.25+w/400);
  ���q�^
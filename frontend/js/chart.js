(()=>{
const api=makeApi("tw_token"),sym=new URLSearchParams(location.search).get("symbol")||"BTC";
const S={range:"1M",type:"candle",ma:true,rsi:true,macd:true,sr:true,n:0,data:[]};
$("#ct").textContent=sym+" – Chart";
const tabs=(id,items,key)=>{$(id).innerHTML=items.map(i=>`<button data-v="${i[0]}" class="${S[key]===i[0]?"on":""}">${i[1]}</button>`).join("");$(id).onclick=e=>{const v=e.target.dataset.v;if(v){S[key]=v;tabs(id,items,key);key==="range"?load():draw()}}};
tabs("#rg",["1D","5D","1M","3M","6M","1Y","5Y"].map(x=>[x,x]),"range");tabs("#ty",[["candle","Candles"],["line","Line"]],"type");
$("#ind").innerHTML=[["ma","MA 20/50"],["rsi","RSI"],["macd","MACD"],["sr","S/R"]].map(i=>`<button data-k="${i[0]}" class="${S[i[0]]?"on":""}">${i[1]}</button>`).join("");
$("#ind").onclick=e=>{const k=e.target.dataset.k;if(k){S[k]=!S[k];e.target.classList.toggle("on");draw()}};
$("#fs").onclick=()=>document.fullscreenElement?document.exitFullscreen():$("#cw").requestFullscreen&&$("#cw").requestFullscreen();
$("#c1").addEventListener("wheel",e=>{e.preventDefault();S.n=Math.max(20,Math.min(S.data.length,(S.n||S.data.length)+(e.deltaY>0?10:-10)));draw()},{passive:false});
const sma=(a,n)=>a.map((_,i)=>i<n-1?null:a.slice(i-n+1,i+1).reduce((s,x)=>s+x,0)/n);
const ema=(a,n)=>{const k=2/(n+1);let p=a[0];return a.map((v,i)=>i?(p=v*k+p*(1-k)):p)};
function rsi(c,n=14){const o=[null];let g=0,l=0;for(let i=1;i<c.length;i++){const d=c[i]-c[i-1];if(i<=n){g+=Math.max(d,0);l+=Math.max(-d,0);o.push(i<n?null:100-100/(1+(g/n)/((l/n)||1e-9)))}else{g=(g*(n-1)+Math.max(d,0))/n;l=(l*(n-1)+Math.max(-d,0))/n;o.push(100-100/(1+g/(l||1e-9)))}}return o}
function setup(cv){const r=devicePixelRatio||1,w=cv.clientWidth,h=+cv.getAttribute("height");cv.width=w*r;cv.height=h*r;const x=cv.getContext("2d");x.scale(r,r);x.clearRect(0,0,w,h);return{x,w,h}}
const css=n=>getComputedStyle(document.documentElement).getPropertyValue(n).trim();
function line(x,vals,xs,ys,col){x.strokeStyle=col;x.lineWidth=1.5;x.beginPath();let s=false;vals.forEach((v,i)=>{if(v==null)return;s?x.lineTo(xs(i),ys(v)):(x.moveTo(xs(i),ys(v)),s=true)});x.stroke()}
function draw(){const all=S.data;if(!all.length)return;const n=S.n||all.length,d=all.slice(-n),cl=all.map(c=>c.c),off=all.length-d.length;
 const up=css("--up"),dn=css("--dn"),bl=css("--bl"),mu=css("--mut");
 {const{x,w,h}=setup($("#c1")),mn=Math.min(...d.map(c=>c.l)),mx=Math.max(...d.map(c=>c.h)),bw=w/d.length,xs=i=>i*bw+bw/2,ys=v=>8+(mx-v)/(mx-mn||1)*(h-60),vm=Math.max(...d.map(c=>c.v));
  d.forEach((c,i)=>{const u=c.c>=c.o,col=u?up:dn;x.globalAlpha=.3;x.fillStyle=col;x.fillRect(xs(i)-bw*.3,h-c.v/vm*40,bw*.6,c.v/vm*40);x.globalAlpha=1;
   if(S.type==="candle"){x.strokeStyle=x.fillStyle=col;x.beginPath();x.moveTo(xs(i),ys(c.h));x.lineTo(xs(i),ys(c.l));x.stroke();x.fillRect(xs(i)-bw*.3,ys(Math.max(c.o,c.c)),Math.max(1,bw*.6),Math.max(1.5,Math.abs(ys(c.o)-ys(c.c))))}});
  if(S.type==="line")line(x,d.map(c=>c.c),xs,ys,bl);
  if(S.ma){line(x,sma(cl,20).slice(off),xs,ys,"#f5a623");line(x,sma(cl,50).slice(off),xs,ys,"#a855f7")}
  if(S.sr){x.setLineDash([5,4]);x.strokeStyle=mu;[mx,mn].forEach(v=>{x.beginPath();x.moveTo(0,ys(v));x.lineTo(w,ys(v));x.stroke();x.fillStyle=mu;x.fillText(v.toLocaleString("en-IN"),4,ys(v)-3)});x.setLineDash([])}}
 $("#c2").style.display=S.rsi?"block":"none";$("#c3").style.display=S.macd?"block":"none";
 if(S.rsi){const{x,w,h}=setup($("#c2")),r=rsi(cl).slice(off),bw=w/d.length,xs=i=>i*bw+bw/2,ys=v=>h-6-v/100*(h-12);x.strokeStyle=mu;x.globalAlpha=.4;[30,70].forEach(v=>{x.beginPath();x.moveTo(0,ys(v));x.lineTo(w,ys(v));x.stroke()});x.globalAlpha=1;line(x,r,xs,ys,bl);x.fillStyle=mu;x.fillText("RSI 14",4,10)}
 if(S.macd){const{x,w,h}=setup($("#c3")),e12=ema(cl,12),e26=ema(cl,26),m=e12.map((v,i)=>v-e26[i]),sg=ema(m,9),mm=m.slice(off),ss=sg.slice(off),mx=Math.max(...mm.map(Math.abs),...ss.map(Math.abs))||1,bw=w/d.length,xs=i=>i*bw+bw/2,ys=v=>h/2-v/mx*(h/2-8);
  mm.forEach((v,i)=>{x.fillStyle=v-ss[i]>=0?up:dn;x.globalAlpha=.5;x.fillRect(xs(i)-bw*.3,Math.min(ys(v-ss[i]),h/2),bw*.6,Math.abs(ys(v-ss[i])-h/2))});x.globalAlpha=1;line(x,mm,xs,ys,bl);line(x,ss,xs,ys,"#f5a623");x.fillStyle=mu;x.fillText("MACD 12/26/9",4,10)}}
async function load(){$("#src").textContent="Loading…";try{const r=await api(`/api/market/history?symbol=${sym}&range=${S.range}`);
 if(!r.available){S.data=[];$("#src").textContent="Is instrument ka historical data abhi available nahi (data provider connect nahi hua).";["c1","c2","c3"].forEach(i=>setup($("#"+i)));return}
 S.data=r.candles;S.n=0;$("#src").textContent="Historical data · Source: "+r.source+" · "+r.candles.length+" candles";draw()}catch(e){$("#src").textContent=e.message}}
addEventListener("resize",draw);load();
})();

(()=>{
const API=window.API_BASE||"",DEMO=new URLSearchParams(location.search).has("demo");
const $=s=>document.querySelector(s);
const esc=s=>String(s??"").replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
const f=x=>Number(x).toLocaleString("en-IN",{maximumFractionDigits:2});
const tok=()=>{try{return localStorage.getItem("tw_token")}catch{return null}};
function toast(m){const t=$("#ts");t.textContent=m;t.style.display="block";clearTimeout(toast.h);toast.h=setTimeout(()=>t.style.display="none",3800)}
async function api(p,o={}){const h={"Content-Type":"application/json"};if(tok())h.Authorization="Bearer "+tok();
  const r=await fetch(API+p,{method:o.method||"GET",headers:h,body:o.body?JSON.stringify(o.body):undefined});
  const j=await r.json().catch(()=>({}));if(!r.ok)throw new Error(j.error||("Request failed ("+r.status+")"));return j}

/* ---------- market ---------- */
const ORDER=["NIFTY50","BANKNIFTY","MCXGOLD","MCXSILVER","BTC"],NM={NIFTY50:"NIFTY 50",BANKNIFTY:"BANK NIFTY",MCXGOLD:"MCX GOLD",MCXSILVER:"MCX SILVER",BTC:"BITCOIN"};
const Q={},H={};let M={};
const mk=s=>!M[s]?"":`<span class="stt ${M[s]==="CLOSED"?"STALE":"LIVE"}">${M[s]==="ALWAYS"?"24x7":"Market "+M[s]}</span>`;
function spark(h,up){if(!h||h.length<2)return"";const mn=Math.min(...h),mx=Math.max(...h);
  const p=h.map((v,i)=>`${i*100/(h.length-1)},${32-(v-mn)/(mx-mn||1)*30}`).join(" ");
  return `<svg class="sp" viewBox="0 0 100 34" preserveAspectRatio="none"><polyline fill="none" stroke="${up?"#10d98a":"#ff4d5e"}" stroke-width="1.6" points="${p}"/></svg>`}
function card(q){const un=q.status==="UNAVAILABLE",t=q.asOf?new Date(q.asOf).toLocaleTimeString("en-IN"):"never";
  const head=`<div class="card" data-s="${esc(q.symbol)}"><div class="nm"><span>${esc(NM[q.symbol]||q.symbol)}</span><span class="stt ${esc(q.status)}">${esc(q.status)}</span>${mk(q.symbol)}</div>`;
  if(un)return head+`<div class="pr">—</div><div class="ch" style="color:var(--mut)">Data unavailable</div><div class="mt"><span>Last update: ${t}</span></div></div>`;
  const d=q.price-q.prevClose,pc=d/q.prevClose*100,u=d>=0;
  return head+`<div class="pr">${f(q.price)}</div><div class="ch ${u?"up":"dn"}">${u?"▲":"▼"} ${f(Math.abs(d))} (${pc.toFixed(2)}%)</div>${spark(H[q.symbol],u)}
  <div class="mt"><span>Prev ${f(q.prevClose)}</span><span>O ${q.open?f(q.open):"—"}</span></div><div class="mt"><span>H ${q.high?f(q.high):"—"} · L ${q.low?f(q.low):"—"}</span><span>${t}</span></div><div class="mt"><span>${esc(q.source)}</span></div></div>`}
function render(cls,sym){$("#cards").innerHTML=ORDER.filter(s=>Q[s]).map(s=>card(Q[s])).join("");
  if(sym){const e=document.querySelector(`.card[data-s="${sym}"] .pr`);e&&e.classList.add(cls)}}
function skel(){$("#cards").innerHTML=Array(5).fill('<div class="card sk"></div>').join("")}
async function load(){try{try{M=await api("/api/market/status")}catch{}(await api("/api/market/overview")).forEach(q=>{Q[q.symbol]=q;if(q.price)(H[q.symbol]=H[q.symbol]||[]).length||(H[q.symbol]=[q.price])});render();
  $("#feed").textContent="Market data feed";}catch(e){$("#cards").innerHTML=`<div class="card" style="grid-column:1/-1">Server se connect nahi ho paaya. <a href="#" id="rt" style="color:var(--bl)">Retry</a></div>`;
  $("#feed").textContent="Offline";const r=$("#rt");if(r)r.onclick=ev=>{ev.preventDefault();skel();load()}}}
function connect(){const s=new WebSocket((API||location.origin).replace(/^http/,"ws")+"/ws/market");
  s.onopen=()=>connect.n=0;
  s.onmessage=e=>{const q=JSON.parse(e.data),a=H[q.symbol]=H[q.symbol]||[];a.push(q.price);if(a.length>24)a.shift();const o=Q[q.symbol];Q[q.symbol]=q;render(o&&o.price>q.price?"fd":"fu",q.symbol)};
  s.onclose=()=>setTimeout(connect,Math.min(30000,((connect.n=(connect.n||0)+1))*2000))}
const MV={u:"NIFTY50",s:"pct",q:"",d:{}};
function mvTable(id,t){const el=$(id),r=MV.d[t];if(!r)return;
  if(r.error){el.innerHTML=`<tr><td style="color:var(--dn);padding:16px">${esc(r.error)}</td></tr>`;return}
  if(!r.available){el.innerHTML=`<tr><td style="color:var(--mut);padding:16px">Data abhi available nahi hai (data provider connect nahi hua ya is universe ka data nahi aaya).</td></tr>`;return}
  const up=t==="gainers",c=up?"up":"dn";let it=r.items.map((q,i)=>({q,i:i+1,c:q.price-q.prevClose,p:(q.price-q.prevClose)/q.prevClose*100}));
  if(MV.q)it=it.filter(x=>(x.q.name+" "+x.q.symbol).toLowerCase().includes(MV.q));
  const key={price:x=>-x.q.price,volume:x=>-x.q.volume,pct:x=>up?-x.p:x.p}[MV.s];it.sort((a,b)=>key(a)-key(b));
  el.innerHTML=`<tr><th>#</th><th>Stock</th><th>Symbol</th><th>Price</th><th>Prev close</th><th>Change</th><th>Chg %</th><th>Volume</th><th>High</th><th>Low</th></tr>`+
   it.map(x=>`<tr data-s="${esc(x.q.symbol)}" style="cursor:pointer"><td>${x.i}</td><td>${esc(x.q.name)}</td><td>${esc(x.q.symbol)}</td><td>${f(x.q.price)}</td><td>${f(x.q.prevClose)}</td><td class="${c}">${f(x.c)}</td><td><span class="pill ${c}">${x.p.toFixed(2)}%</span></td><td>${f(x.q.volume)}</td><td>${f(x.q.high)}</td><td>${f(x.q.low)}</td></tr>`).join("")+
   `<tr><td colspan="10" style="font-size:11px;color:var(--mut)">Source: ${esc(r.items[0].source)} (${esc(r.items[0].status)}) · ${new Date(r.items[0].asOf).toLocaleString("en-IN")}${r.session==="CLOSED"?" · Market closed – last completed session":""}</td></tr>`}
async function movers(){for(const t of["gainers","losers"]){try{MV.d[t]=await api("/api/market/movers?type="+t+"&universe="+MV.u)}catch(e){MV.d[t]={error:e.message}}}mvTable("#tg","gainers");mvTable("#tl","losers")}

/* ---------- auth / trial / payment ---------- */
function syncLogin(){$("#lg").textContent=tok()?"Logout":"Login"}
function openAuth(mode,after){const reg=mode==="register",i=(n,p,x="")=>`<input name="${n}" placeholder="${p}" ${x} required>`;
  $("#mc").innerHTML=`<h3>${reg?"Create account":"Login"}</h3><form id="af">${reg?i("fullName","Full name"):""}${i("email","Email",'type="email"')}${reg?i("mobile","Mobile (10 digit)",'inputmode="numeric"'):""}${i("password","Password (min 8)",'type="password" minlength="8"')}${reg?i("confirmPassword","Confirm password",'type="password"'):""}
  <div id="ae" class="dn" style="font-size:13px"></div><button class="btn" style="width:100%;margin-top:8px">${reg?"Register":"Login"}</button></form>
  <p class="mt"><a href="#" id="sw" style="color:var(--bl)">${reg?"Already registered? Login":"New here? Register"}</a><a href="account.html">Forgot password?</a><a href="#" id="cx">Cancel</a></p>`;
  $("#md").classList.add("on");
  $("#sw").onclick=e=>{e.preventDefault();openAuth(reg?"login":"register",after)};
  $("#cx").onclick=e=>{e.preventDefault();$("#md").classList.remove("on")};
  $("#af").onsubmit=async e=>{e.preventDefault();try{const r=await api("/api/auth/"+(reg?"register":"login"),{method:"POST",body:Object.fromEntries(new FormData(e.target))});
    localStorage.setItem("tw_token",r.token);push();$("#md").classList.remove("on");syncLogin();toast("Welcome!");after&&after()}catch(x){$("#ae").textContent=x.message}}}
async function trial(){if(!tok())return openAuth("register",trial);
  try{const r=await api("/api/trial/start",{method:"POST"});toast("Free demo active till "+new Date(r.endsAt).toLocaleDateString("en-IN"))}catch(e){toast(e.message)}}
function rzp(){return new Promise((ok,no)=>{if(window.Razorpay)return ok();const s=document.createElement("script");s.src="https://checkout.razorpay.com/v1/checkout.js";s.onload=ok;s.onerror=no;document.head.appendChild(s)})}
async function subscribe(plan){if(!tok())return openAuth("login",()=>subscribe(plan));
  try{const o=await api("/api/payments/order",{method:"POST",body:{plan}});await rzp();
    new Razorpay({key:o.keyId,amount:o.amount,currency:o.currency,order_id:o.orderId,name:"Tips Wala",description:plan,theme:{color:"#3b82ff"},
      handler:async r=>{try{const v=await api("/api/payments/verify",{method:"POST",body:{orderId:r.razorpay_order_id,paymentId:r.razorpay_payment_id,signature:r.razorpay_signature}});
        toast(v.expiresAt?"Membership active till "+new Date(v.expiresAt).toLocaleDateString("en-IN"):"Payment confirmed. Membership active.")}catch(e){toast(e.message)}}}).open()}catch(e){toast(e.message)}}

/* ---------- FAQ / reviews / contact ---------- */
async function faq(){try{const l=await api("/api/faqs");$("#faq").innerHTML=l.length?l.map(x=>`<details><summary>${esc(x.question)}</summary><p>${esc(x.answer)}</p></details>`).join(""):`<p style="color:var(--mut)">FAQs jald add kiye jayenge.</p>`}catch{$("#faq").textContent="FAQs load nahi ho paaye."}}
async function reviews(){try{const r=await api("/api/reviews");
  $("#rsum").innerHTML=r.count?`<div class="pr">${r.average} ★</div><div class="mt"><span>${r.count} reviews</span><span>${r.fiveStarPct}% five-star</span></div>`:`<p style="color:var(--mut)">Abhi tak koi review nahi. Pehla review aap likhein.</p>`;
  $("#rl").innerHTML=r.items.map(x=>`<div class="card" style="margin-top:10px"><b>${esc(x.name)}</b> ${x.verified?'<span class="stt LIVE">Verified member</span>':""}<div class="up">${"★".repeat(x.rating)}${"☆".repeat(5-x.rating)}</div><p>${esc(x.body)}</p>${x.imageUrl?`<img src="${esc(x.imageUrl.startsWith("/")?API+x.imageUrl:x.imageUrl)}" alt="" loading="lazy" style="max-width:100%;max-height:200px;border-radius:10px">`:""}</div>`).join("")}catch{$("#rsum").textContent="Reviews load nahi ho paaye."}}

/* ---------- native / pwa ---------- */
let pushInit=false;
function push(){const C=window.Capacitor,P=C&&C.Plugins&&C.Plugins.PushNotifications;if(!P||!tok())return;
  if(!pushInit){pushInit=true;P.addListener("registration",t=>api("/api/devices",{method:"POST",body:{token:t.value}}).catch(()=>{}));
    P.addListener("pushNotificationActionPerformed",a=>{const d=a.notification&&a.notification.data&&a.notification.data.deepLink;if(d)location.href=d.replace(/^\//,"")})}
  P.requestPermissions().then(r=>{if(r.receive==="granted")P.register()}).catch(()=>{})}
function native(){
  if("serviceWorker" in navigator&&/^https?:/.test(location.protocol))navigator.serviceWorker.register("sw.js").catch(()=>{});
  addEventListener("offline",()=>toast("Aap offline hain. Internet check karein."));addEventListener("online",()=>{toast("Wapas online");!DEMO&&load()});
  document.head.insertAdjacentHTML("beforeend","<style>.card[data-s]{cursor:pointer}</style>");
  document.addEventListener("click",e=>{const c=e.target.closest(".card[data-s],tr[data-s]");if(c)location.href="chart.html?symbol="+c.dataset.s});
  const C=window.Capacitor,A=C&&C.Plugins&&C.Plugins.App;
  if(A){A.addListener("backButton",e=>e.canGoBack?history.back():A.exitApp());
    A.addListener("appUrlOpen",e=>{try{const u=new URL(e.url);location.href=u.pathname.replace(/^\//,"")+u.search+u.hash}catch{}})}
  push()}
/* ---------- boot ---------- */
document.querySelectorAll("button").forEach(b=>{const t=b.textContent;
  if(/Free Demo/i.test(t))b.onclick=trial;else if(/Live Market Dekhein/.test(t))b.onclick=()=>location.hash="#m";else if(/Premium Plans/.test(t))b.onclick=()=>location.hash="#p"});
document.querySelectorAll(".plan button").forEach((b,i)=>b.onclick=()=>subscribe(["MONTHLY","HALF_YEARLY","YEARLY"][i]));
$("#lg").onclick=()=>tok()?(localStorage.removeItem("tw_token"),syncLogin(),toast("Logged out")):openAuth("login");
$("#rf").onsubmit=async e=>{e.preventDefault();if(!tok())return openAuth("login");
  try{let imageUrl=null;const fl=e.target.img&&e.target.img.files[0];
    if(fl){const fd=new FormData();fd.append("file",fl);const ur=await fetch(API+"/api/media/user",{method:"POST",headers:{Authorization:"Bearer "+tok()},body:fd});const uj=await ur.json().catch(()=>({}));if(!ur.ok)throw new Error(uj.error||"Image upload failed");imageUrl=uj.url}
    await api("/api/reviews",{method:"POST",body:{rating:+e.target.rating.value,body:e.target.body.value,imageUrl}});toast("Review submit ho gaya, approval ke baad dikhega.");e.target.reset()}catch(x){toast(x.message)}};
$("#cf").onsubmit=async e=>{e.preventDefault();try{await api("/api/contact",{method:"POST",body:Object.fromEntries(new FormData(e.target))});toast("Message bhej diya gaya. Hum jald reply karenge.");e.target.reset()}catch(x){toast(x.message)}};
$("#mvu").onchange=e=>{MV.u=e.target.value;movers()};$("#mvs").onchange=e=>{MV.s=e.target.value;mvTable("#tg","gainers");mvTable("#tl","losers")};
$("#mvq").oninput=e=>{MV.q=e.target.value.trim().toLowerCase();mvTable("#tg","gainers");mvTable("#tl","losers")};
native();syncLogin();faq();reviews();
if(!DEMO){skel();load();connect();movers();setInterval(load,30000);setInterval(movers,60000)}
})();

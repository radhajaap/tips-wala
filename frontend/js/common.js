window.API=window.API_BASE||"";
window.$=s=>document.querySelector(s);
window.esc=s=>String(s??"").replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
window.toast=m=>{let t=$("#ts");if(!t){t=document.createElement("div");t.id="ts";document.body.appendChild(t)}t.textContent=m;t.style.display="block";clearTimeout(toast.h);toast.h=setTimeout(()=>t.style.display="none",3800)};
window.makeApi=key=>async(p,o={})=>{const h={"Content-Type":"application/json"},t=(key==="tw_admin"?sessionStorage:localStorage).getItem(key);if(t)h.Authorization="Bearer "+t;
  const r=await fetch(API+p,{method:o.method||"GET",headers:h,body:o.body?JSON.stringify(o.body):undefined});const j=await r.json().catch(()=>({}));
  if(!r.ok)throw new Error(j.error||("Request failed ("+r.status+")"));return j};
window.cleanHtml=html=>{const d=new DOMParser().parseFromString(html,"text/html");d.querySelectorAll("script,style,iframe,object,embed,link,meta,form,base").forEach(n=>n.remove());
  d.querySelectorAll("*").forEach(n=>[...n.attributes].forEach(a=>{const v=a.value.trim().toLowerCase();if(a.name.startsWith("on")||((a.name==="href"||a.name==="src")&&!/^(https?:|\/|#|mailto:)/.test(v)))n.removeAttribute(a.name)}));return d.body.innerHTML};

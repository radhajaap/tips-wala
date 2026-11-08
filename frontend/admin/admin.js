(()=>{
const api=makeApi("tw_admin"),R=$("#view"),money=p=>"₹"+(p/100).toLocaleString("en-IN"),dt=s=>s?new Date(s).toLocaleString("en-IN"):"";
const tbl=(rows,cols,act)=>!rows.length?'<p class="mut">Kuch nahi mila.</p>':`<div class="sc card"><table><tr>${cols.map(c=>`<th>${c[0]}</th>`).join("")}${act?"<th></th>":""}</tr>${rows.map(r=>`<tr>${cols.map(c=>`<td>${esc(c[1](r))}</td>`).join("")}${act?`<td>${act(r)}</td>`:""}</tr>`).join("")}</table></div>`;
const b=(a,id,t,c="g")=>`<button class="btn s ${c}" data-a="${a}" data-id="${id}">${t}</button> `;
const V={
 async dash(){const s=await api("/api/admin/stats"),k=(n,v)=>`<div class="card"><div class="mut">${n}</div><div class="kpi">${v}</div></div>`;
  return `<div class="grid">${k("Total users",s.totalUsers)}${k("New users (7d)",s.newUsers7d)}${k("Active members",s.activeMembers)}${k("Active demos",s.activeDemos)}${k("Expired subs",s.expiredSubscriptions)}${k("Pending refunds",s.pendingRefunds)}${k("Pending inquiries",s.pendingInquiries)}${k("Posts",s.totalPosts)}${k("Reviews",s.totalReviews+" ("+s.pendingReviews+" pending)")}${s.totalRevenuePaise!=null?k("Revenue (month)",money(s.monthRevenuePaise))+k("Revenue (total)",money(s.totalRevenuePaise)):""}</div>`},
 async users(){return tbl(await api("/api/admin/users"),[["Name",r=>r.fullName],["Email",r=>r.email],["Mobile",r=>r.mobile],["Role",r=>r.role],["Delete req",r=>r.deleteRequested?"YES":""],["Joined",r=>dt(r.createdAt)]],r=>b("role",r.id,"Change role"))},
 async payments(){return tbl(await api("/api/admin/payments"),[["Date",r=>dt(r.createdAt)],["Plan",r=>r.plan],["Amount",r=>money(r.amountPaise)],["Status",r=>r.status],["Order",r=>r.razorpayOrderId],["Payment",r=>r.razorpayPaymentId||""]])},
 async refunds(){return tbl(await api("/api/admin/refunds"),[["Date",r=>dt(r.createdAt)],["Reason",r=>r.reason],["Status",r=>r.status],["Reject reason",r=>r.rejectReason||""],["Txn ID",r=>r.refundTxnId||""],["Note",r=>r.adminNote||""]],
  r=>(r.status==="REQUESTED"?b("rf-ap",r.id,"Approve")+b("rf-rj",r.id,"Reject","r"):"")+(r.status==="APPROVED"?b("rf-done",r.id,"Mark refunded"):"")+b("rf-note",r.id,"Note"))},
 async reviews(){return tbl(await api("/api/admin/reviews?status=PENDING"),[["Name",r=>r.authorName],["Rating",r=>r.rating],["Review",r=>r.body],["Verified",r=>r.verified?"yes":""]],r=>b("rv-ok",r.id,"Approve")+b("rv-hide",r.id,"Hide","r"))},
 async inquiries(){return tbl(await api("/api/admin/inquiries"),[["Date",r=>dt(r.createdAt)],["Name",r=>r.name],["Email",r=>r.email],["Mobile",r=>r.mobile||""],["Subject",r=>r.subject],["Message",r=>r.message],["Status",r=>r.status]],r=>b("inq-done",r.id,"Mark replied"))},
 async faqs(){const l=await api("/api/faqs");return `<form id="faqf" class="card"><b>Add FAQ</b><input name="question" placeholder="Question" required><textarea name="answer" rows="3" placeholder="Answer" required></textarea><input name="sortOrder" type="number" placeholder="Order (0,1,2…)"><button class="btn">Add</button></form>`+tbl(l,[["Q",r=>r.question],["A",r=>r.answer]],r=>b("faq-del",r.id,"Delete","r"))},
 async posts(){const l=await api("/api/admin/posts");return `<form id="postf" class="card"><b>New post</b><input name="title" placeholder="Title" required><select name="category"><option>DAILY_OVERVIEW</option><option>NIFTY_ANALYSIS</option><option>BANKNIFTY_ANALYSIS</option><option>GOLD</option><option>SILVER</option><option>CRYPTO</option><option>EDUCATION</option><option>ECONOMIC_NEWS</option><option>TECHNICAL_ANALYSIS</option></select><textarea name="body" rows="8" placeholder="Content (HTML allowed: p, h2, ul, img, a)" required></textarea><input name="seoTitle" placeholder="SEO title"><input name="seoDescription" placeholder="SEO description"><label><input type="checkbox" name="premium" style="width:auto"> Premium</label> <label><input type="checkbox" name="published" style="width:auto"> Publish now</label><br><button class="btn">Save</button></form>`+tbl(l,[["Title",r=>r.title],["Category",r=>r.category],["Published",r=>r.published],["Premium",r=>r.premium],["Views",r=>r.views]],r=>b("post-del",r.id,"Delete","r"))},
 async notify(){const l=await api("/api/admin/notifications");return `<form id="nf" class="card"><b>Send push notification</b><input name="title" placeholder="Title" required maxlength="120"><textarea name="message" rows="2" placeholder="Message" required maxlength="500"></textarea><input name="imageUrl" placeholder="Image URL (https, optional)"><input name="deepLink" placeholder="Deep link e.g. /updates.html (optional)"><select name="audience"><option value="ALL">All users</option><option value="MEMBERS">Members / demo users</option></select><label class="mut">Schedule (empty = send now)</label><input name="scheduledAt" type="datetime-local"><p class="mut">Sirf service, educational aur zaroori updates bhejein. Unauthorized promotions nahi.</p><button class="btn">Send</button></form>`+tbl(l,[["Created",r=>dt(r.createdAt)],["Title",r=>r.title],["Audience",r=>r.audience],["Scheduled",r=>dt(r.scheduledAt)],["Sent",r=>dt(r.sentAt)],["Count",r=>r.sentCount]])},
 async audit(){return tbl(await api("/api/admin/audit"),[["Time",r=>dt(r.createdAt)],["Action",r=>r.action],["Detail",r=>r.detail||""],["Actor",r=>r.actorId||""]])}
};
const NAMES={dash:"Dashboard",users:"Users",payments:"Payments",refunds:"Refunds",reviews:"Reviews",inquiries:"Inquiries",faqs:"FAQs",posts:"Posts",notify:"Notifications",audit:"Audit log"};
let cur="dash";
async function show(k){cur=k;[...$("#tabs").children].forEach(x=>x.classList.toggle("on",x.dataset.k===k));R.innerHTML='<p class="mut">Loading…</p>';try{R.innerHTML=await V[k]()}catch(e){R.innerHTML=`<p class="dn">${esc(e.message)} (is role ke paas is section ka access nahi ho sakta)</p>`}}
const H={
 "rf-ap":id=>api("/api/admin/refunds/"+id,{method:"PUT",body:{action:"APPROVE"}}),
 "rf-rj":id=>{const reason=prompt("Rejection reason (customer ko dikhega):");return reason&&api("/api/admin/refunds/"+id,{method:"PUT",body:{action:"REJECT",reason}})},
 "rf-done":id=>{const txnId=prompt("Refund transaction ID:");return txnId&&api("/api/admin/refunds/"+id,{method:"PUT",body:{action:"REFUNDED",txnId}})},
 "rf-note":id=>{const note=prompt("Internal note:");return note&&api("/api/admin/refunds/"+id,{method:"PUT",body:{action:"NOTE",note}}).catch(()=>toast("Note ke saath koi action select karein"))},
 "rv-ok":id=>api("/api/admin/reviews/"+id+"/status?value=APPROVED",{method:"PUT"}),
 "rv-hide":id=>api("/api/admin/reviews/"+id+"/status?value=HIDDEN",{method:"PUT"}),
 "inq-done":id=>api("/api/admin/inquiries/"+id+"?status=REPLIED",{method:"PUT"}),
 "faq-del":id=>confirm("Delete FAQ?")&&api("/api/admin/faqs/"+id,{method:"DELETE"}),
 "post-del":id=>confirm("Delete post?")&&api("/api/admin/posts/"+id,{method:"DELETE"}),
 "role":id=>{const v=prompt("New role: USER, CONTENT_ADMIN, SUPPORT_ADMIN, SUPER_ADMIN");return v&&api("/api/admin/users/"+id+"/role?value="+encodeURIComponent(v.trim().toUpperCase()),{method:"PUT"})}};
R.onclick=async e=>{const x=e.target.closest("button[data-a]");if(!x)return;try{await H[x.dataset.a](x.dataset.id);toast("Done")}catch(er){toast(er.message)}show(cur)};
R.onsubmit=async e=>{e.preventDefault();const f=e.target,d=Object.fromEntries(new FormData(f));try{
  if(f.id==="faqf")await api("/api/admin/faqs",{method:"POST",body:{question:d.question,answer:d.answer,sortOrder:+d.sortOrder||0}});
  if(f.id==="postf")await api("/api/admin/posts",{method:"POST",body:{...d,premium:!!d.premium,published:!!d.published}});
  if(f.id==="nf"){const r=await api("/api/admin/notifications",{method:"POST",body:{...d,scheduledAt:d.scheduledAt?new Date(d.scheduledAt).toISOString():null}});toast(r.status+(r.count!=null?" ("+r.count+")":""));return show(cur)}
  toast("Saved");show(cur)}catch(er){toast(er.message)}};
async function enter(){try{await api("/api/admin/stats");$("#login").style.display="none";$("#app").style.display="block";$("#out").style.display="";
  $("#tabs").innerHTML=Object.keys(NAMES).map(k=>`<button data-k="${k}">${NAMES[k]}</button>`).join("");$("#tabs").onclick=e=>e.target.dataset.k&&show(e.target.dataset.k);show("dash")}
  catch(e){sessionStorage.removeItem("tw_admin");$("#le").textContent=e.message.includes("403")?"Ye account admin nahi hai.":e.message}}
$("#lf").onsubmit=async e=>{e.preventDefault();$("#le").textContent="";try{const r=await api("/api/auth/login",{method:"POST",body:Object.fromEntries(new FormData(e.target))});sessionStorage.setItem("tw_admin",r.token);enter()}catch(x){$("#le").textContent=x.message}};
$("#out").onclick=()=>{sessionStorage.removeItem("tw_admin");location.reload()};
if(sessionStorage.getItem("tw_admin"))enter();
})();

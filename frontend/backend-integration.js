
/* CampusBite backend bridge: customer ordering, queue, menu and team dashboards */
const CB = {
  token: () => localStorage.getItem('campusbite_token') || '',
  async request(path, options = {}) {
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (this.token()) headers.Authorization = 'Bearer ' + this.token();
    const response = await fetch(API_BASE + path, { ...options, headers });
    const raw = await response.text();
    let data = null;
    try { data = raw ? JSON.parse(raw) : null; } catch { data = raw; }
    if (!response.ok) {
      const error = new Error(data?.message || data?.detail || data || `Request failed (${response.status})`);
      error.status = response.status;
      throw error;
    }
    return data;
  },
  status(s) { return ({PENDING:'Placed', ACCEPTED:'Accepted', PREPARING:'Preparing', READY:'Ready', DELIVERED:'Delivered', CANCELLED:'Cancelled'})[s] || s; },
  apiStatus(s) { return ({Placed:'PENDING', Accepted:'ACCEPTED', Preparing:'PREPARING', Ready:'READY', Delivered:'DELIVERED', Cancelled:'CANCELLED'})[s] || s; },
  outlet(s) { return s === 'Normal Campus Canteen' ? 'Normal Canteen' : s; },
  async menu(outlet) {
    const rows = await this.request(outlet ? '/api/menu?outlet=' + encodeURIComponent(this.outlet(outlet)) : '/api/menu');
    return rows.map(x => ({_id:x.id, name:x.name, price:x.price, description:x.description || '', category:x.category || 'All', emoji:x.emoji || '🍽️', outlet:x.outlet, stock:x.stock, lowStockAt:5, available:x.available, sold:0}));
  },
  order(v) {
    const o = v.order || v;
    return {id:o.id, num:o.queueNumber, customer:o.customerName, customerEmail:o.customerEmail, outlet:o.outlet, items:(o.items||[]).map(x=>({id:x.menuItemId,name:x.name,price:x.unitPrice,qty:x.quantity})), subtotal:o.subtotal, discountRate:o.discountRate, discountAmount:o.discountAmount, total:o.total, status:this.status(o.status), payment:o.paymentMethod, paymentStatus:o.paymentStatus || 'UNPAID', isPaid:String(o.paymentStatus || '').toUpperCase()==='PAID', time:o.createdAt ? new Date(o.createdAt).toLocaleString() : '', queuePosition:v.queuePosition || 0, estimatedWaitMinutes:v.estimatedWaitMinutes || 0};
  },
  boards: {},
  // Whole live queue (all customers) for every outlet this customer has an order at, plus the outlet being browsed.
  async loadBoards() {
    const outlets = new Set(orders.filter(o => o.status !== 'Delivered' && o.status !== 'Cancelled').map(o => o.outlet));
    if (activeOutlet) outlets.add(this.outlet(activeOutlet));
    const result = {};
    await Promise.all([...outlets].map(async o => {
      try { result[o] = await this.request('/api/orders/board?outlet=' + encodeURIComponent(o)); } catch (e) { console.warn('Queue board failed for', o, e); }
    }));
    this.boards = result;
    renderQueueBoards();
  },
  _orderSeq: 0,
  _lastDelivered: -1,
  async loadOrders() {
    if (!user) return;
    const seq = ++this._orderSeq, role = user.role;
    let rows = [];
    if (role === 'customer') rows = await this.request('/api/orders/my');
    else if (role === 'staff') rows = await this.request('/api/orders/queue?outlet=' + encodeURIComponent(user.assignedOutlet || activeOutlet || 'Normal Canteen'));
    else rows = await this.request('/api/orders');
    if (seq !== this._orderSeq || !user) return; // a newer refresh already applied
    const prev = this._prevStatus || null, all = rows.map(x => this.order(x)), next = all.filter(o => o.status !== 'Cancelled');
    if (role === 'customer' && prev) {
      for (const o of next) {
        if (prev[o.id] && prev[o.id] !== o.status) {
          if (o.status === 'Ready') toast(`Order #${o.num} (${o.outlet}) is ready for pickup! 🍽️`);
          else if (o.status === 'Delivered') toast(`Order #${o.num} delivered — enjoy! 🌻 Your queue has moved up.`);
          else if (o.status === 'Cancelled') toast(`Order #${o.num} was cancelled.`);
        }
      }
    }
    if (role === 'customer' && prev) for (const id of Object.keys(prev)) if (!next.some(o => o.id === id)) toast('An order was cancelled and removed from your queue.');
    this._prevStatus = Object.fromEntries(next.map(o => [o.id, o.status]));
    orders = next;
    renderOrdersIfPossible();
    refreshAll();
    if (role === 'customer') await this.loadBoards();
    updateQueuePopup();
    if (role === 'customer') {
      const delivered = orders.filter(o => o.status === 'Delivered').length;
      if (this.loyalty === null || delivered !== this._lastDelivered) { this._lastDelivered = delivered; await this.refreshLoyalty(); }
    }
  },
  // Move an order forward one legal step at a time (the server only allows PENDING→ACCEPTED→PREPARING→READY→DELIVERED).
  async advance(order, targetUiStatus) {
    const chain = ['PENDING', 'ACCEPTED', 'PREPARING', 'READY', 'DELIVERED'];
    const cur = chain.indexOf(this.apiStatus(order.status)), want = chain.indexOf(this.apiStatus(targetUiStatus));
    if (cur < 0 || want < 0) throw new Error('That status change is not supported');
    if (want <= cur) throw new Error('An order cannot move backwards');
    for (let i = cur + 1; i <= want; i++) await this.request(`/api/orders/${order.id}/status?status=${chain[i]}`, { method: 'PATCH' });
  },
  async cancelOrder(order) { await this.request(`/api/orders/${order.id}/status?status=CANCELLED`, { method: 'PATCH' }); },
  async markCollected(orderId) { return this.request(`/api/orders/${orderId}/collected`, { method: 'PATCH' }); },
  loyalty: null,
  useLoyaltyCoupon: false,
  async refreshLoyalty() {
    try {
      this.loyalty = await this.request('/api/loyalty/me');
      cbLoyaltyRate = Number(this.loyalty.currentDiscountPercent) || 0;
    } catch (e) { console.warn('Loyalty sync failed:', e); }
    this.paintLoyalty();
    renderCart(); // cart discount line follows the server's coupon
  },
  paintLoyalty() {
    const data = this.loyalty || { stamps: 0, stampsTowardNextReward: 0, currentDiscountPercent: 0, message: 'Complete delivered orders to earn sunflower stamps.' };
    const progress = Math.max(0, Math.min(5, Number(data.stampsTowardNextReward) || 0));
    const target = $('sunflowerProgress');
    if (target) target.innerHTML = Array.from({length:5},(_,i)=>`<span class="sunflower-stamp ${i < progress ? 'earned' : ''}" title="${i < progress ? 'Completed stamp' : 'Stamp pending'}"><span>🌻</span></span>`).join('');
    const next = $('sunflowerNext');
    if (next) {
      const coupon = Number(data.currentDiscountPercent) || 0;
      next.innerHTML = coupon > 0
        ? `<strong>${coupon}% OFF COUPON READY!</strong> Choose whether to use it in your cart. Your stamps reset only when you apply it.`
        : safe(data.message || 'Earn one stamp for each delivered order over ₹100.');
    }
    const stats = $('sunflowerStatsLine');
    if (stats) stats.textContent = `${data.stamps || 0} sunflower stamp${data.stamps === 1 ? '' : 's'} toward your next reward`;
  }
};
// The old browser-only stamp counter is replaced by the server's loyalty data.
renderSunflowerClub = function(){ if (user?.role === 'customer') CB.paintLoyalty(); };
function renderOrdersIfPossible(){ if(user?.role==='customer') renderOrders(); }
const renderOrdersBeforeCollectionAction = renderOrders;
renderOrders = function(){
  renderOrdersBeforeCollectionAction();
  if (user?.role !== 'customer') return;
  document.querySelectorAll('#myOrders .order-card').forEach((card, index) => {
    const order = orders.filter(item => item.customer === user.name)[index];
    if (order?.status !== 'Ready' || !order.id) return;
    if (order.isPaid) {
      if (card.querySelector('[data-collected]')) return;
      const button = document.createElement('button');
      button.className = 'small-btn';
      button.dataset.collected = order.id;
      button.textContent = 'I collected this order';
      card.append(button);
    } else if (!card.querySelector('.payment-pending-note')) {
      const note = document.createElement('p');
      note.className = 'payment-pending-note';
      note.textContent = 'Payment confirmation is pending. Canteen staff must mark this order paid before collection.';
      card.append(note);
    }
  });
};
const renderCartBeforeCouponLabel = renderCart;
renderCart = function(){
  renderCartBeforeCouponLabel();
  const discountLine = $('cartDiscountLine');
  if (!discountLine) return;
  const rate = user?.role === 'customer' ? cbLoyaltyRate : 0;
  const subtotal = cart.reduce((sum, item) => sum + Number(item.price) * Number(item.qty), 0);
  if (!rate) {
    CB.useLoyaltyCoupon = false;
    return;
  }
  const useCoupon = CB.useLoyaltyCoupon;
  const discount = useCoupon ? Math.round(subtotal * rate) / 100 : 0;
  $('cartTotal').textContent = money(subtotal - discount);
  discountLine.innerHTML = `<label class="coupon-choice"><input id="useLoyaltyCoupon" type="checkbox" ${useCoupon ? 'checked' : ''}> 🌻 Use my ${rate}% coupon on this bill</label>${useCoupon ? `<b>−${money(discount)}</b><small>Subtotal ${money(subtotal)}</small>` : '<small>Your coupon stays available if you leave this unchecked.</small>'}`;
  const checkbox = $('useLoyaltyCoupon');
  checkbox.onchange = () => {
    CB.useLoyaltyCoupon = checkbox.checked;
    renderCart();
  };
};
// Keep the saved cart in step with the live menu (price/stock changes, or items removed after a re-seed).
function reconcileCart(outlet) {
  if (!user || user.role !== 'customer' || !cart.length || !foods.length) return;
  if ((localStorage.getItem('campusbite_cart_outlet') || outlet) !== outlet) return;
  const before = JSON.stringify(cart);
  cart = cart.map(x => {
    const f = foods.find(v => v._id === x.id);
    return f && f.available ? { ...x, name: f.name, price: Number(f.price), qty: Math.min(x.qty, f.stock) } : null;
  }).filter(x => x && x.qty > 0);
  if (JSON.stringify(cart) !== before) { renderCart(); toast('Your cart was updated to match the current menu and stock'); }
}
async function loadOutletMenu(outlet) {
  try { foods = await CB.menu(['admin','owner'].includes(user?.role) ? null : outlet); reconcileCart(outlet); renderFilters(); renderFoods(); refreshAll(); }
  catch (e) { console.error(e); toast('Could not load menu: ' + e.message); }
}
// Customers and team members get different screens: the role is mirrored on <body data-role> (see styles.css).
function applyRoleUI() {
  const role = user?.role || 'guest';
  let chip = $('roleChip');
  if (!chip) { chip = document.createElement('span'); chip.id = 'roleChip'; chip.className = 'role-chip'; $('logoutBtn').before(chip); }
  chip.textContent = !user ? '' : ({customer:'🎓 Customer', staff:'👩‍🍳 Staff', admin:'📋 Admin', owner:'👑 Owner'}[role] || role);
  chip.classList.toggle('hidden', !user);
  document.body.dataset.role = role;
  const q = $('queueNav');
  if (q) q.textContent = (role === 'customer' || role === 'guest') ? '🎟️ Queue' : '🎟️ Live queue';
  const note = document.querySelector('#roleBanner .dash-note');
  if (note) note.textContent = !user ? '' :
    role === 'customer' ? ' Your orders and queue position update live.' :
    role === 'staff' ? ` Live queue and stock for ${user.assignedOutlet || 'your outlet'}.` :
    role === 'admin' ? ' Menu, inventory, orders and sales for all outlets.' :
    ' Full control: team, suppliers, purchases and performance.';
}
async function loadBackendDashboard() {
  if (!user) return;
  try {
    const profile=await CB.request('/api/users/me');
    user.assignedOutlet=profile.assignedOutlet||'';
    localStorage.setItem('campusbite_user', JSON.stringify(user));
    applyRoleUI();
    if(user.role==='staff' && user.assignedOutlet) {
      activeOutlet=user.assignedOutlet;
      foods=await CB.menu(user.assignedOutlet);
    } else if(user.role==='admin' || user.role==='owner') {
      foods=await CB.menu(null);
    }
    await CB.loadOrders();
    if (user.role === 'customer' && activeOutlet) await loadOutletMenu(activeOutlet);
    if (user.role === 'staff') {
      const low = await CB.request('/api/inventory/low-stock?threshold=5');
      foods = foods.map(f => { const x=low.find(v=>v.id===f._id); return x ? {...f,stock:x.stock,available:x.available}:f; });
      refreshAll();
    }
    if (user.role === 'admin' || user.role === 'owner') {
      const [dash, teamRows, supplierRows] = await Promise.all([CB.request('/api/admin/dashboard'), CB.request('/api/admin/users'), CB.request('/api/suppliers')]);
      team = teamRows; suppliers = supplierRows;
      const stats = $('adminStats');
      if (stats) stats.innerHTML = stat('Total orders',dash.totalOrders) + stat('Pending',dash.pendingOrders) + stat('Delivered',dash.deliveredOrders) + stat('Revenue',money(dash.revenueFromDeliveredOrders));
      if (user.role === 'owner') {
        const ownerStats=$('ownerStats');
        if(ownerStats) ownerStats.innerHTML=stat('Revenue',money(dash.revenueFromDeliveredOrders))+stat('Menu items',dash.menuItems)+stat('Customers',dash.customers)+stat('Team members',dash.staff+dash.admins);
        if($('teamList')) $('teamList').innerHTML=team.map(t=>`<div class="inventory-row">${safe(t.name)} · ${safe(t.role)} · ${safe(t.email)} · ${t.enabled?'Active':'Disabled'}</div>`).join('');
      }
    }
  } catch(e) { console.error(e); toast('Dashboard sync: '+e.message); }
}
function chooseOutlet(name) {
  const previousCartOutlet = localStorage.getItem('campusbite_cart_outlet') || '';
  if (cart.length && previousCartOutlet && previousCartOutlet !== name) {
    cart = [];
    localStorage.removeItem('campusbite_cart');
    localStorage.removeItem('campusbite_cart_outlet');
  }
  activeOutlet = name;
  localStorage.setItem('campusbite_outlet', name);
  $('juiceSeasonalShowcase').classList.toggle('hidden', name !== 'Juice Corner');
  if($('cookingRequest')) $('cookingRequest').value='';
  category='All'; showSelectedBrandHero(name);
  document.body.classList.remove('outlet-nescafe','outlet-amul','outlet-normal','outlet-juice');
  document.body.classList.add('outlet-'+outletMeta[name].theme);
  $('outletPicker').classList.add('hidden'); $('outletStrip').classList.remove('hidden');
  $('outletTitle').textContent=name==='Nescafé'?'NESCAFÉ CANTEEN':name==='Amul'?'AMUL CANTEEN':name==='Juice Corner'?'JUICE CORNER':'CAMPUS CANTEEN';
  $('outletSubtitle').textContent=outletMeta[name].subtitle; $('outletLogo').src=outletMeta[name].logo;
  $('menu').classList.remove('hidden'); $('searchInput').value=''; renderFilters(); renderCart();
  loadOutletMenu(name);
  if (user?.role === 'customer') CB.loadBoards();
}
$('checkoutBtn').onclick = async () => {
  if (!user || user.role !== 'customer') { modal('cartModal',false); modal('authModal'); return toast('Login as Customer to place an order'); }
  if (!cart.length) return toast('Your cart is empty');
  try {
    const result=await CB.request('/api/orders',{method:'POST',body:JSON.stringify({
      outlet:CB.outlet(activeOutlet), paymentMethod:'DEMO',
      useLoyaltyCoupon:CB.useLoyaltyCoupon,
      items:cart.map(x=>({menuItemId:x.id,quantity:x.qty}))
    })});
    const created=CB.order(result);
    if($('cookingRequest')?.value.trim()) {
      await CB.request('/api/cooking-requests',{method:'POST',body:JSON.stringify({orderId:created.id,requestText:$('cookingRequest').value.trim()})});
    }
    cart=[]; CB.useLoyaltyCoupon=false; $('cookingRequest').value=''; renderCart(); modal('cartModal',false);
    await Promise.all([CB.loadOrders(),loadOutletMenu(activeOutlet),CB.refreshLoyalty()]);
    toast(`Order placed! Queue #${created.num} 🎟️`);
  } catch(e) { toast('Order failed: '+e.message); }
};
$('myOrders').onclick = async e => {
  const c=e.target.closest('[data-cancel]'), f=e.target.closest('[data-feedback]');
  if(c) {
    const o=orders.find(x=>String(x.id||x.num)===String(c.dataset.cancel)); if(!o?.id)return;
    try { await CB.request(`/api/orders/${o.id}/cancel`,{method:'PATCH'}); await CB.loadOrders(); if(activeOutlet) await loadOutletMenu(activeOutlet); toast('Order cancelled; stock restored'); }
    catch(err){toast(err.message);}
  }
  if(f) {
    const o=orders.find(x=>String(x.id||x.num)===String(f.dataset.feedback)); if(!o?.id)return;
    const rating=Number(prompt('Rate your delivered order from 1 to 5:'));
    if(!Number.isInteger(rating)||rating<1||rating>5)return toast('Enter a rating from 1 to 5');
    const comment=prompt('Any comments? (Optional)','')||'';
    try {
      for(const line of o.items) await CB.request('/api/ratings',{method:'POST',body:JSON.stringify({orderId:o.id,menuItemId:line.id,rating,comment})});
      toast('Thanks for rating your food! 💗');
    } catch(err){toast(err.message);}
  }
};
document.addEventListener('change', async e=>{
  const s=e.target.closest('[data-status]'); if(!s||!user||!['staff','admin','owner'].includes(user.role))return;
  const o=orders.find(x=>String(x.id||x.num)===String(s.dataset.status)); if(!o?.id)return;
  try { await CB.advance(o, s.value); await CB.loadOrders(); toast('Order status updated'); }
  catch(err){toast(err.message); await CB.loadOrders();}
});
document.addEventListener('click', async e=>{
  const nb=e.target.closest('[data-next]'), cb=e.target.closest('[data-cancelorder]'), paid=e.target.closest('[data-mark-paid]');
  if(paid && user && ['staff','admin','owner'].includes(user.role)) {
    paid.disabled=true;
    try { await CB.request(`/api/orders/${paid.dataset.markPaid}/payment/paid`,{method:'PATCH'}); await CB.loadOrders(); toast('Payment confirmed as paid. Customer can now collect the order.'); }
    catch(err){toast(err.message);paid.disabled=false;}
    return;
  }
  if((nb||cb) && user && ['staff','admin','owner'].includes(user.role)) {
    const o=orders.find(x=>x.id===(nb||cb).dataset.next||x.id===(nb||cb).dataset.cancelorder); if(!o)return;
    try {
      if(nb) {
        const step={Placed:'Accepted',Accepted:'Preparing',Preparing:'Ready'}[o.status];
        await CB.advance(o,step); await CB.loadOrders();
        toast(step==='Ready' ? `Customer notified: order #${o.num} is ready for pickup 🔔` : step==='Delivered' ? `Order #${o.num} delivered — queue moved up` : `Order #${o.num} is now ${step}`);
      } else {
        if(!confirm(`Cancel order #${o.num} for ${o.customer}? It will be removed from the queue and stock restored.`))return;
        await CB.cancelOrder(o); await CB.loadOrders(); toast(`Order #${o.num} cancelled — queue renumbered`);
      }
    } catch(err){ toast(err.message); await CB.loadOrders(); }
    return;
  }
  const del=e.target.closest('[data-delete]');
  if(del && user && ['admin','owner'].includes(user.role)) {
    const item=foods.find(x=>x._id===del.dataset.delete); if(!item)return;
    if(!confirm(`Delete "${item.name}" from the menu? This cannot be undone.`))return;
    try { await CB.request(`/api/menu/${item._id}`,{method:'DELETE'}); foods=foods.filter(x=>x._id!==item._id); refreshAll(); renderFoods(); toast('Menu item deleted'); }
    catch(err){ toast(err.message); }
    return;
  }
  const b=e.target.closest('[data-restock],[data-toggle],[data-edit]'); if(!b||!user)return;
  const id=b.dataset.restock||b.dataset.toggle||b.dataset.edit, item=foods.find(x=>x._id===id); if(!item)return;
  try {
    if(b.dataset.restock){const q=Number(prompt('How many units to add?', '10'));if(!Number.isInteger(q)||q<1)return;await CB.request(`/api/inventory/${id}/restock?quantity=${q}`,{method:'POST'});}
    else if(b.dataset.toggle) await CB.request(`/api/menu/${id}/availability?available=${!item.available}`,{method:'PATCH'});
    else {const price=Number(prompt('New price for '+item.name,item.price));if(!Number.isFinite(price)||price<=0)return;await CB.request(`/api/menu/${id}`,{method:'PUT',body:JSON.stringify({name:item.name,description:item.description,category:item.category,price,stock:item.stock,outlet:item.outlet,imageUrl:'',available:item.available})});}
    await loadOutletMenu(activeOutlet||item.outlet); toast('Inventory updated');
  }catch(err){toast(err.message);}
});
const originalShowUserDashboard=showUserDashboard;
showUserDashboard=function(){ originalShowUserDashboard(); applyRoleUI(); loadBackendDashboard(); };
const originalLogout= $('logoutBtn').onclick;
$('logoutBtn').onclick=()=>{originalLogout?.();localStorage.removeItem('campusbite_token');localStorage.removeItem('campusbite_user');orders=[];foods=[];cart=[];team=[];suppliers=[];activeOutlet='';localStorage.removeItem('campusbite_cart');localStorage.removeItem('campusbite_cart_outlet');CB.loyalty=null;CB.useLoyaltyCoupon=false;CB._lastDelivered=-1;cbLoyaltyRate=0;renderCart();applyRoleUI();document.body.classList.remove('outlet-nescafe','outlet-amul','outlet-normal','outlet-juice');};
async function restoreCampusBiteSession(){
  const token=CB.token(); if(!token)return;
  try {
    const profile=await CB.request('/api/users/me');
    user={name:profile.name,email:profile.email,role:normalizeRole(profile.role),assignedOutlet:profile.assignedOutlet||''};
    localStorage.setItem('campusbite_user', JSON.stringify(user));
    showUserDashboard();
    if(activeOutlet && user.role==='customer') await loadOutletMenu(activeOutlet);
  } catch(e){
    if (e.status === 401 || e.status === 403) {
      localStorage.removeItem('campusbite_token');
      localStorage.removeItem('campusbite_user');
      console.warn('Session expired',e);
      return;
    }
    const cachedUser=readCampusBiteStorage('campusbite_user',null);
    if(cachedUser && cachedUser.role){
      user={...cachedUser,role:normalizeRole(cachedUser.role)};
      showUserDashboard();
      toast('Showing your saved account; reconnect to the server to sync live data.');
    }
    console.warn('Could not verify the saved session. Kept it for the next refresh.',e);
  }
}
setInterval(()=>{if(user)loadBackendDashboard();},15000);
restoreCampusBiteSession();

// Restore the last selected outlet after refresh. app.js has already loaded the cart.
(() => {
  const savedOutlet = localStorage.getItem('campusbite_outlet');
  if (savedOutlet && Object.prototype.hasOwnProperty.call(outletMeta, savedOutlet)) {
    chooseOutlet(savedOutlet);
  } else {
    renderCart();
  }
})();



$('foodForm').onsubmit=async e=>{
 e.preventDefault();const d=Object.fromEntries(new FormData(e.target));
 try{await CB.request('/api/menu',{method:'POST',body:JSON.stringify({name:d.name,description:d.description||'',category:d.category,price:Number(d.price),stock:Number(d.stock),outlet:CB.outlet(d.outlet||activeOutlet),imageUrl:'',available:true})});e.target.reset();await loadOutletMenu(d.outlet||activeOutlet);toast('Menu item saved to backend');}
 catch(err){toast(err.message);}
};
$('teamForm').onsubmit=async e=>{
 e.preventDefault();const d=Object.fromEntries(new FormData(e.target));
 try{await CB.request('/api/admin/users',{method:'POST',body:JSON.stringify({name:d.name,email:d.email,password:d.password,role:d.role,assignedOutlet:d.role==='STAFF'?d.assignedOutlet:null})});e.target.reset();await loadBackendDashboard();toast('Team account created');}
 catch(err){toast(err.message);}
};
$('supplierForm').onsubmit=async e=>{
 e.preventDefault();const d=Object.fromEntries(new FormData(e.target));
 try{await CB.request('/api/suppliers',{method:'POST',body:JSON.stringify({name:d.name,phone:d.phone||'',contactName:'',email:'',category:'General',notes:''})});e.target.reset();const rows=await CB.request('/api/suppliers');suppliers=rows;refreshAll();toast('Supplier saved');}
 catch(err){toast(err.message);}
};
$('purchaseForm').onsubmit=async e=>{
 e.preventDefault();const d=Object.fromEntries(new FormData(e.target));const item=foods.find(x=>x._id===d.foodId);
 try{
   const supplierRows=await CB.request('/api/suppliers');
   if(!supplierRows.length) return toast('Add a supplier before recording a purchase');
   const purchase=await CB.request('/api/purchases',{method:'POST',body:JSON.stringify({supplierId:supplierRows[0].id,items:[{itemName:item?.name||'Stock',quantity:Number(d.quantity),unitCost:Number(d.cost||0)/Number(d.quantity)}]})});
   await CB.request(`/api/inventory/${d.foodId}/restock?quantity=${Number(d.quantity)}`,{method:'POST'});
   await CB.request(`/api/purchases/${purchase.id}/status?value=RECEIVED`,{method:'PATCH'});
   e.target.reset();await loadOutletMenu(item.outlet);toast('Purchase recorded and stock updated');
 }catch(err){toast(err.message);}
};

// Live queue of every outlet that matters to this customer: ALL customers' orders (anonymous), yours highlighted.
function queueBoardHTML() {
  const names = Object.keys(CB.boards || {}).sort();
  if (!names.length) return '';
  return names.map(outlet => {
    const rows = CB.boards[outlet] || [];
    const body = rows.length ? rows.map(r => `<div class="qb-row ${r.mine ? 'qb-mine' : ''}"><span class="qb-pos">#${r.position}</span><span>Token ${r.queueNumber}</span><span class="qb-status">${CB.status(r.status)}</span><span>${r.mine ? '⭐ You' : ''}</span></div>`).join('')
      : '<div class="qb-empty">Nobody is waiting — order now and you will be first! 🎉</div>';
    return `<div class="queue-board"><h4>🎟️ Live queue · ${safe(outlet)} <small>${rows.length} order${rows.length === 1 ? '' : 's'} waiting</small></h4>${body}</div>`;
  }).join('');
}
function renderQueueBoards() {
  if (user?.role !== 'customer') return;
  let spot = $('queueBoardOrders');
  if (!spot && $('myOrders')) { spot = document.createElement('div'); spot.id = 'queueBoardOrders'; $('myOrders').before(spot); }
  if (spot) spot.innerHTML = queueBoardHTML();
  const pop = $('queueBoardPopup');
  if (pop) pop.innerHTML = queueBoardHTML();
}

// Fill the customer's queue popup from the latest server data.
function updateQueuePopup() {
  if (!$('customerQueuePopup') || user?.role !== 'customer') return;
  if (!$('queueBoardPopup')) { const d = document.createElement('div'); d.id = 'queueBoardPopup'; $('customerQueueStatus').parentNode.insertBefore(d, $('customerQueueStatus').parentNode.querySelector('.queue-popover-foot')); }
  let more = $('queueMoreOrders');
  if (!more) { more = document.createElement('div'); more.id = 'queueMoreOrders'; more.className = 'queue-more'; $('customerQueueStatus').after(more); }
  const active = orders.filter(o => o.status !== 'Delivered' && o.status !== 'Cancelled'); // /orders/my is newest-first
  if (!active.length) {
    $('queueCollectedAction')?.remove();
    const last = orders[0];
    $('queuePopupTitle').textContent = 'No active order right now';
    $('customerQueueNumber').textContent = '—';
    $('customerQueueAhead').textContent = last ? `Your last order (#${last.num} · ${last.outlet}) is ${last.status}.` : 'Place an order and your queue spot will appear here.';
    $('customerQueueOutlet').textContent = '';
    $('customerQueueStatus').textContent = 'No active order';
    more.innerHTML = '';
    renderQueueBoards();
    return;
  }
  const mine = active[0], ahead = Math.max(0, (mine.queuePosition || 1) - 1);
  $('queuePopupTitle').textContent = mine.status === 'Ready' ? 'Your order is ready!' : 'Your order is in the queue!';
  $('customerQueueNumber').textContent = mine.status === 'Ready' ? '✓' : '#' + (mine.queuePosition || 1);
  $('customerQueueAhead').textContent = mine.status === 'Ready'
    ? 'Please collect it from the counter.'
    : ahead === 0 ? 'You are next!' : `${ahead} ${ahead === 1 ? 'person' : 'people'} ahead of you · about ${mine.estimatedWaitMinutes} min`;
  $('customerQueueOutlet').textContent = `Your position in line · Token #${mine.num} · ${mine.outlet}`;
  $('customerQueueStatus').textContent = mine.status;
  let collectedAction = $('queueCollectedAction');
  if (mine.status === 'Ready' && mine.id && mine.isPaid) {
    if (!collectedAction) {
      collectedAction = document.createElement('button');
      collectedAction.id = 'queueCollectedAction';
      collectedAction.className = 'small-btn';
      $('customerQueueStatus').after(collectedAction);
    }
    collectedAction.dataset.collected = mine.id;
    collectedAction.textContent = 'I collected this order';
    collectedAction.disabled = false;
    $('queuePaymentNote')?.remove();
  } else if (mine.status === 'Ready' && !mine.isPaid) {
    collectedAction?.remove();
    let paymentNote = $('queuePaymentNote');
    if (!paymentNote) {
      paymentNote = document.createElement('p');
      paymentNote.id = 'queuePaymentNote';
      paymentNote.className = 'payment-pending-note';
      $('customerQueueStatus').after(paymentNote);
    }
    paymentNote.textContent = 'Waiting for canteen staff to confirm payment before collection.';
  } else {
    collectedAction?.remove();
    $('queuePaymentNote')?.remove();
  }
  more.innerHTML = active.length > 1
    ? '<b>Your other active orders</b>' + active.slice(1).map(o => `<div>Token #${o.num} · ${safe(o.outlet)} · ${o.status} · ${o.queuePosition <= 1 ? 'next up' : (o.queuePosition - 1) + ' ahead'}</div>`).join('')
    : '';
}
document.addEventListener('click', async e => {
  const button = e.target.closest('[data-collected]');
  if (!button || user?.role !== 'customer') return;
  button.disabled = true;
  try {
    await CB.markCollected(button.dataset.collected);
    await CB.loadOrders();
    await CB.refreshLoyalty();
    toast('Order marked collected and removed from the queue.');
  } catch (error) {
    toast(error.message || 'Could not mark the order collected.');
    button.disabled = false;
  }
});
openCustomerQueue = async function(){
  try { await CB.loadOrders(); updateQueuePopup(); $('customerQueuePopup').classList.remove('hidden'); }
  catch(e){ toast(e.message); }
};

// Live updates: refresh orders every 5 s (and the instant the tab becomes visible again).
function pollOrders() {
  if (!user || document.hidden) return;
  if (document.activeElement && document.activeElement.tagName === 'SELECT') return; // don't close a staff dropdown mid-use
  CB.loadOrders().catch(e => console.warn('Queue refresh failed:', e));
}
setInterval(pollOrders, 5000);
document.addEventListener('visibilitychange', () => { if (!document.hidden) pollOrders(); });


// Refresh server-backed order/queue/loyalty data when the customer opens Sunflower Club.
document.addEventListener('click', e => {
  if (e.target.closest('#sunflowerNav') && user?.role === 'customer') {
    CB.refreshLoyalty();
    CB.loadOrders();
  }
});

// ---------- tiny helpers ----------
const $ = (sel, root = document) => root.querySelector(sel);
const $$ = (sel, root = document) => [...root.querySelectorAll(sel)];
const api = (path, opts = {}) =>
  fetch(path, { credentials: 'include', ...opts })
    .then(r => r.ok ? r.json() : r.json().then(e => Promise.reject(e)));

// ---------- tab switcher ----------
$$('.tabs button').forEach(btn => {
  btn.addEventListener('click', () => {
    $$('.tabs button').forEach(b => b.classList.toggle('active', b === btn));
    $$('.tab').forEach(t => t.classList.toggle('active', t.id === 'tab-' + btn.dataset.tab));
    if (btn.dataset.tab === 'feed') loadFeed();
    if (btn.dataset.tab === 'profile') loadProfile();
  });
});

// ---------- auth ----------
async function boot() {
  const me = await api('/api/auth/me');
  if (!me.authenticated) {
    $('#whoami').innerHTML =
      `<button id="loginTrusted" class="link-btn">login as trusted</button> · ` +
      `<button id="loginNewbie" class="link-btn">login as newbie</button>`;
    $('#loginTrusted').onclick = () => loginAs('trusted');
    $('#loginNewbie').onclick = () => loginAs('newbie');
    return;
  }
  $('#whoami').textContent = `Signed in as ${me.name}`;
  $('#bellWrap').hidden = false;
  initBell();
}

async function loginAs(name) {
  await api('/api/auth/login?name=' + encodeURIComponent(name), { method: 'POST' });
  location.reload();
}

// ---------- notification bell ----------
const BELL_POLL_MS = 30_000;
const SEEN_KEY = 'bell.seenIds';

function seenIds() {
  try { return new Set(JSON.parse(localStorage.getItem(SEEN_KEY) || '[]')); }
  catch { return new Set(); }
}
function markSeen(ids) {
  const seen = seenIds();
  ids.forEach(id => seen.add(id));
  localStorage.setItem(SEEN_KEY, JSON.stringify([...seen]));
}

function initBell() {
  $('#bellBtn').addEventListener('click', toggleBell);
  $('#bellClose').addEventListener('click', () => $('#bellPanel').hidden = true);
  document.addEventListener('click', (e) => {
    if (!$('#bellWrap').contains(e.target)) $('#bellPanel').hidden = true;
  });

  pollFeed();
  setInterval(pollFeed, BELL_POLL_MS);
}

async function pollFeed() {
  try {
    const data = await api('/api/feed');
    renderBell(data);
    if ($('#tab-feed').classList.contains('active')) renderFeed(data);
  } catch {}
}

function renderBell(data) {
  const items = data.items || [];
  const unlocked = items.filter(i => !i.locked);
  const locked = items.filter(i => i.locked);

  const seen = seenIds();
  const fresh = unlocked.filter(i => !seen.has(i.id));
  const badge = $('#bellCount');
  if (fresh.length > 0) {
    badge.textContent = fresh.length > 99 ? '99+' : String(fresh.length);
    badge.hidden = false;
  } else {
    badge.hidden = true;
  }

  if (items.length === 0) {
    $('#bellList').innerHTML =
      `<p class="hint">No matches yet. Set an alert, or report a slot to earn credits and unlock faster.</p>`;
    return;
  }

  const html = [...unlocked, ...locked].map(i => {
    if (i.locked) {
      const label = i.hoursLeft >= 1
        ? `Unlocks in ${Math.round(i.hoursLeft)}h`
        : `Unlocks in ${Math.max(1, Math.round(i.minutesLeft))}m`;
      return `<div class="bell-item locked">
        🔒 <em>${label}</em>
        <div class="meta">Reported ${timeAgo(i.reportedAt)}. Keep contributing to see it sooner.</div>
      </div>`;
    }
    return `<div class="bell-item unlocked">
      <div class="provider">${escapeHtml(i.providerName || 'Slot')} <span class="meta">· ${escapeHtml(i.ageGroup || '')}</span></div>
      <div class="meta">${escapeHtml(i.addressHint || '')}${i.addressHint ? ' · ' : ''}reported ${timeAgo(i.reportedAt)}</div>
    </div>`;
  }).join('');
  $('#bellList').innerHTML = html;
}

function toggleBell() {
  const panel = $('#bellPanel');
  panel.hidden = !panel.hidden;
  if (!panel.hidden) {
    api('/api/feed').then(data => {
      markSeen((data.items || []).filter(i => !i.locked).map(i => i.id));
      $('#bellCount').hidden = true;
    }).catch(() => {});
  }
}

function timeAgo(iso) {
  if (!iso) return 'just now';
  const s = Math.max(0, (Date.now() - new Date(iso).getTime()) / 1000);
  if (s < 60) return 'just now';
  if (s < 3600) return `${Math.round(s / 60)}m ago`;
  if (s < 86400) return `${Math.round(s / 3600)}h ago`;
  return `${Math.round(s / 86400)}d ago`;
}
function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, c =>
    ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}

// ---------- Feed tab ----------
async function loadFeed() {
  const data = await api('/api/feed');
  $('#tierNote').textContent =
    `Your tier: ${data.viewerTier} — alerts unlock ${data.viewerLagHours}h after they're reported.`;
  renderFeed(data);
}
function renderFeed(data) {
  const list = $('#feedList');
  const items = data.items || [];
  if (items.length === 0) {
    list.innerHTML = `<p class="hint">No matching slots in your geofence right now.</p>`;
    return;
  }
  list.innerHTML = items.map(i => {
    if (i.locked) {
      const label = i.hoursLeft >= 1
        ? `Available in ${Math.round(i.hoursLeft)}h`
        : `Available in ${Math.max(1, Math.round(i.minutesLeft))}m`;
      return `<div class="feed-item locked">
        🔒 <strong>${label}</strong>
        <div class="hint">A matching slot was reported nearby. Your tier unlocks it at ${new Date(i.unlocksAt).toLocaleTimeString()}. Contribute to unlock faster.</div>
      </div>`;
    }
    return `<div class="feed-item">
      <strong>${escapeHtml(i.providerName)}</strong> · ${escapeHtml(i.ageGroup)}
      <div class="hint">${escapeHtml(i.addressHint || '')} — reported ${timeAgo(i.reportedAt)}
        · ${i.verifications} verifications · ${i.confirmations} confirmations</div>
      <div class="hint">${escapeHtml(i.notes || '')}</div>
    </div>`;
  }).join('');
}

// ---------- Profile tab ----------
async function loadProfile() {
  const p = await api('/api/profile');
  $('#profileOut').innerHTML = `
    <p><strong>${p.tier}</strong> — ${p.credits90d} credits (90d)
       ${p.nextTierAt > 0 ? `· ${p.nextTierAt - p.credits90d} to next tier` : '· max tier'}</p>
    <p class="hint">Unlock lag: ${p.lagHours}h · Weekly earnings: ${p.weeklyEarned}/${p.weeklyCap}</p>
    <h3>Recent ledger</h3>
    <ul>${(p.ledger||[]).map(e =>
      `<li>${e.delta > 0 ? '+' : ''}${e.delta} ${e.reason} — ${timeAgo(e.at)}</li>`).join('')}</ul>`;
}

// ---------- Alert setup + Report ----------
$('#saveAlert')?.addEventListener('click', async () => {
  const body = {
    lat: parseFloat($('#lat').value),
    lng: parseFloat($('#lng').value),
    radiusKm: parseFloat($('#radiusKm').value),
    ageGroup: $('#ageGroup').value
  };
  const saved = await api('/api/alerts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  $('#alertOut').textContent = 'Saved: ' + JSON.stringify(saved, null, 2);
  pollFeed();
});

$('#reportForm')?.addEventListener('submit', async (e) => {
  e.preventDefault();
  const f = new FormData(e.target);
  const body = Object.fromEntries(f.entries());
  body.lat = parseFloat(body.lat);
  body.lng = parseFloat(body.lng);
  const res = await api('/api/reports', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  $('#reportOut').textContent = res.ok
    ? `Reported. Pending verification. ID: ${res.id}`
    : `Error: ${res.error}`;
  pollFeed();
});

// ---------- go ----------
boot();
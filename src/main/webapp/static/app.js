// Minimal front-end helper for navbar, auth state, and pagination

const App = (() => {
  const state = {
    session: { loggedIn: false, email: null, userName: null },
  };

  async function fetchSession() {
    try {
      const res = await fetch('/api/session', { credentials: 'same-origin' });
      if (!res.ok) return state.session;
      const data = await res.json();
      state.session = data;
      return data;
    } catch {
      return state.session;
    }
  }

  function renderNavbar(activePage) {
    const nav = document.querySelector('.navbar');
    if (!nav) return;

    const s = state.session;
    
    // Hide navbar if user is not logged in
    if (!s.loggedIn) {
      nav.style.display = 'none';
      return;
    }
    
    // Show navbar if user is logged in
    nav.style.display = 'flex';

    const links = [];

    if (s.loggedIn) {
      links.push(
        { key: 'upload', href: 'upload.html', label: 'Upload', disabled: !s.loggedIn },
        { key: 'users', href: 'users.html', label: 'Users', disabled: !s.loggedIn },
        { key: 'migrate', href: 'migrate.html', label: 'Migrate', disabled: !s.loggedIn },
      );
    } else {
      links.push(
        { key: 'login', href: 'index.html', label: 'Login' },
        { key: 'upload', href: 'upload.html', label: 'Upload', disabled: true },
        { key: 'users', href: 'users.html', label: 'Users', disabled: true },
        { key: 'migrate', href: 'migrate.html', label: 'Migrate', disabled: true },
      );
    }

    const userLabel = s.loggedIn
      ? (s.userName || s.email || 'User')
      : '';

    const linksHtml = links
      .map(
        (l) =>
          `<a href="${l.href}" class="${l.key === activePage ? 'active' : ''} ${l.disabled ? 'disabled-link' : ''}">${l.label}</a>`,
      )
      .join('');

    // Add search input for pages with tables
    const showSearch = s.loggedIn && (activePage === 'users' || activePage === 'migrate' || activePage === 'upload');
    const searchPlaceholder = activePage === 'upload' ? 'Search...' : activePage === 'migrate' ? 'Search BigQuery users...' : 'Search users...';
    const searchHtml = showSearch ? `
      <div class="nav-search">
        <div class="nav-search-wrapper">
          <input type="text" id="navbarSearch" placeholder="${searchPlaceholder}" />
        </div>
      </div>
    ` : '';

    const authHtml = s.loggedIn
      ? `<button type="button" class="nav-auth" id="navLogout">Logout</button>`
      : `<a href="index.html" class="nav-auth-link ${activePage === 'login' ? 'active' : ''}">Login</a>`;

    nav.innerHTML = `
      <div class="nav-brand">USER MANAGEMENT</div>
      <nav class="nav-links">
        ${linksHtml}
      </nav>
      ${searchHtml}
      <div class="nav-right">
        ${userLabel ? `<span class="nav-user">${userLabel}</span>` : ''}
        ${authHtml}
      </div>
    `;

    // Handle navbar search
    if (showSearch) {
      const searchInput = document.getElementById('navbarSearch');
      if (searchInput) {
        // Dispatch search event that pages can listen to
        searchInput.addEventListener('input', (e) => {
          const event = new CustomEvent('navbarSearch', { detail: e.target.value });
          window.dispatchEvent(event);
        });
      }
    }

    const logoutBtn = document.getElementById('navLogout');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', async () => {
        try {
          await fetch('/logout', { method: 'POST', credentials: 'same-origin' });
        } finally {
          window.location.href = 'index.html';
        }
      });
    }
  }

  // Pagination helper for tables
  function paginate(items, page, pageSize) {
    const total = items.length;
    const totalPages = Math.max(1, Math.ceil(total / pageSize));
    const current = Math.min(Math.max(1, page), totalPages);
    const start = (current - 1) * pageSize;
    const end = start + pageSize;
    return {
      page: current,
      totalPages,
      totalItems: total,
      slice: items.slice(start, end),
    };
  }

  function renderPager(containerId, pageState, onChange, options = {}) {
    const el = document.getElementById(containerId);
    if (!el) return;
    const { page, totalPages, totalItems, pageSize: statePageSize } = pageState;
    const { pageSize, onPageSizeChange } = options;
    const currentPageSize = pageSize || statePageSize || 10;
    const startItem = totalItems === 0 ? 0 : (page - 1) * currentPageSize + 1;
    const endItem = Math.min(page * currentPageSize, totalItems);
    
    const pageSizeSelect = typeof currentPageSize === 'number' ? `
      <select id="${containerId}-pageSize" style="margin-left: 8px;">
        ${[10,25,50,100].map(n => `<option value="${n}" ${n===currentPageSize?'selected':''}>${n}</option>`).join('')}
      </select>
    ` : '';
    el.innerHTML = `
      <div class="pager-container">
        <div class="pager-info">
          <span>Showing ${startItem}-${endItem} of ${totalItems} user${totalItems === 1 ? '' : 's'}</span>
          ${pageSizeSelect}
        </div>
        <div class="pager-controls">
          <button type="button" class="pager-btn" data-page="${page - 1}" ${page <= 1 ? 'disabled' : ''}>Prev</button>
          <button type="button" class="pager-btn" data-page="${page + 1}" ${page >= totalPages ? 'disabled' : ''}>Next</button>
        </div>
      </div>
    `;
    el.querySelectorAll('.pager-btn').forEach((btn) => {
      btn.addEventListener('click', () => {
        const target = Number(btn.getAttribute('data-page'));
        if (target >= 1 && target <= totalPages) onChange(target);
      });
    });

    const sel = document.getElementById(`${containerId}-pageSize`);
    if (sel && typeof onPageSizeChange === 'function') {
      sel.addEventListener('change', () => onPageSizeChange(Number(sel.value)));
    }
  }

  return {
    fetchSession,
    renderNavbar,
    paginate,
    renderPager,
  };
})();


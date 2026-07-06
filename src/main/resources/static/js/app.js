// FinTrack Personal Finance Application JavaScript (ES6 SPA Client)

const API_BASE_URL = window.location.origin + '/api';

// Safe LocalStorage Wrapper to bypass Tracking Prevention Storage Block
const storage = {
    getItem(key) {
        try {
            return localStorage.getItem(key);
        } catch (e) {
            console.warn(`[FinTrack] Storage read blocked for key "${key}":`, e);
            return null;
        }
    },
    setItem(key, value) {
        try {
            localStorage.setItem(key, value);
        } catch (e) {
            console.warn(`[FinTrack] Storage write blocked for key "${key}":`, e);
        }
    },
    removeItem(key) {
        try {
            localStorage.removeItem(key);
        } catch (e) {
            console.warn(`[FinTrack] Storage remove blocked for key "${key}":`, e);
        }
    }
};

// Application State
let state = {
    user: null,
    accessToken: storage.getItem('accessToken') || null,
    refreshToken: storage.getItem('refreshToken') || null,
    currentView: 'landing',
    incomePage: 0,
    expensePage: 0,
    charts: {} // Store Chart instances
};

// Category Constants
const INCOME_CATEGORIES = ['Salary', 'Business', 'Freelance', 'Interest', 'Gift', 'Others'];
const EXPENSE_CATEGORIES = ['Food', 'Transport', 'Shopping', 'Medical', 'Bills', 'Education', 'Entertainment', 'Investment', 'Travel', 'Others'];

// Initialize App
document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    setupEventListeners();
    
    if (state.accessToken) {
        verifySessionAndStart();
    } else {
        navigateTo('landing');
    }
});

// Setup Dark/Light Theme
function initTheme() {
    const savedTheme = storage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-bs-theme', savedTheme);
    updateThemeIcon(savedTheme);

    document.getElementById('theme-toggle').addEventListener('click', () => {
        const currentTheme = document.documentElement.getAttribute('data-bs-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-bs-theme', newTheme);
        storage.setItem('theme', newTheme);
        updateThemeIcon(newTheme);
    });
}

function updateThemeIcon(theme) {
    const icon = document.getElementById('theme-toggle').querySelector('i');
    if (theme === 'dark') {
        icon.className = 'bi bi-sun fs-5';
    } else {
        icon.className = 'bi bi-moon-stars fs-5';
    }
}

// Global Event Listeners
function setupEventListeners() {
    // Prevent default form submits on SPA anchors
    document.querySelectorAll('a[href="#"]').forEach(anchor => {
        anchor.addEventListener('click', (e) => e.preventDefault());
    });
}

// Session Verification on Boot
async function verifySessionAndStart() {
    console.log('[FinTrack] Verifying session on boot... AccessToken:', state.accessToken ? (state.accessToken.substring(0, 15) + '...') : 'none');
    try {
        showSpinner(true);
        const res = await apiFetch('/users/profile');
        console.log('[FinTrack] Profile API response:', res);
        if (res && res.status === 200) {
            state.user = res.data;
            console.log('[FinTrack] Session verified! User:', state.user.username);
            updateUIForAuth(true);
            navigateTo('dashboard');
        } else {
            console.warn('[FinTrack] Invalid session response, logging out.');
            clearSession();
            navigateTo('landing');
        }
    } catch (err) {
        console.error('[FinTrack] Session verification error:', err);
        clearSession();
        navigateTo('landing');
    } finally {
        showSpinner(false);
    }
}

// SPA Routing Engine
function navigateTo(viewName) {
    state.currentView = viewName;
    
    // Hide all views
    document.querySelectorAll('.view-section').forEach(view => {
        view.classList.add('d-none');
    });

    // Reset Active Sidebar Items
    document.querySelectorAll('#sidebar .nav-link').forEach(link => {
        link.classList.remove('active');
    });

    const sidebar = document.getElementById('sidebar');
    
    // Check if view requires auth
    const authRequired = !['landing', 'login', 'signup'].includes(viewName);
    
    if (authRequired && !state.accessToken) {
        navigateTo('login');
        return;
    }

    // Toggle Sidebar Visibility
    if (authRequired) {
        sidebar.classList.remove('d-none');
        document.getElementById('main-content').className = 'col-md-9 ms-sm-auto col-lg-10 px-md-4';
    } else {
        sidebar.classList.add('d-none');
        document.getElementById('main-content').className = 'col-12 px-md-4';
    }

    // Show selected view
    const activeView = document.getElementById(`view-${viewName}`);
    if (activeView) {
        activeView.classList.remove('d-none');
    }

    // Highlight active sidebar item
    const sidebarLink = Array.from(document.querySelectorAll('#sidebar .nav-link'))
        .find(a => a.innerText.toLowerCase().includes(viewName));
    if (sidebarLink) {
        sidebarLink.classList.add('active');
    }

    // Load dynamic view data
    loadViewData(viewName);
}

// Load dynamic data based on view
function loadViewData(viewName) {
    switch (viewName) {
        case 'dashboard':
            loadDashboardData();
            loadNotifications();
            break;
        case 'income':
            loadIncomeData();
            break;
        case 'expense':
            loadExpenseData();
            break;
        case 'budget':
            loadBudgetData();
            break;
        case 'analytics':
            loadAnalyticsData();
            break;
        case 'profile':
            loadProfileData();
            break;
        case 'reports':
            initReportsView();
            break;
    }
}

// Global Custom Fetch Wrapper with Automatic Token Refresh
async function apiFetch(endpoint, options = {}) {
    if (!options.headers) {
        options.headers = {};
    }
    
    if (state.accessToken) {
        options.headers['Authorization'] = `Bearer ${state.accessToken}`;
    }
    options.headers['Content-Type'] = 'application/json';

    let response = await fetch(`${API_BASE_URL}${endpoint}`, options);

    if (response.status === 401 && state.refreshToken) {
        // Try token refresh
        const refreshed = await tryTokenRefresh();
        if (refreshed) {
            options.headers['Authorization'] = `Bearer ${state.accessToken}`;
            response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        } else {
            clearSession();
            navigateTo('login');
            showToast('Session expired. Please login again.', 'danger');
            return null;
        }
    }

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'API request failed');
    }

    return await response.json();
}

async function tryTokenRefresh() {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken: state.refreshToken })
        });
        if (response.ok) {
            const result = await response.json();
            state.accessToken = result.data.accessToken;
            storage.setItem('accessToken', state.accessToken);
            return true;
        }
    } catch (e) {
        console.error('Failed to refresh token', e);
    }
    return false;
}

// Authentication Logic
async function handleLogin(e) {
    e.preventDefault();
    const usernameOrEmail = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    try {
        showSpinner(true);
        const res = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ usernameOrEmail, password })
        });

        const data = await res.json();
        if (res.ok) {
            state.accessToken = data.data.accessToken;
            state.refreshToken = data.data.refreshToken;
            storage.setItem('accessToken', state.accessToken);
            storage.setItem('refreshToken', state.refreshToken);
            state.user = {
                id: data.data.id,
                username: data.data.username,
                email: data.data.email,
                fullName: data.data.fullName
            };
            
            updateUIForAuth(true);
            navigateTo('dashboard');
            showToast('Signed in successfully!', 'success');
        } else {
            showToast(data.message || 'Invalid username or password', 'danger');
        }
    } catch (err) {
        showToast('Connection to server failed.', 'danger');
    } finally {
        showSpinner(false);
    }
}

async function handleSignup(e) {
    e.preventDefault();
    const username = document.getElementById('signup-username').value;
    const email = document.getElementById('signup-email').value;
    const fullName = document.getElementById('signup-fullname').value;
    const password = document.getElementById('signup-password').value;

    try {
        showSpinner(true);
        const res = await fetch(`${API_BASE_URL}/auth/signup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, fullName, password })
        });

        const data = await res.json();
        if (res.ok) {
            showToast('Account created successfully! Please sign in.', 'success');
            navigateTo('login');
        } else {
            showToast(data.message || 'Registration failed', 'danger');
        }
    } catch (err) {
        showToast('Connection to server failed.', 'danger');
    } finally {
        showSpinner(false);
    }
}

function logout() {
    clearSession();
    updateUIForAuth(false);
    navigateTo('landing');
    showToast('Logged out successfully', 'success');
}

function clearSession() {
    state.accessToken = null;
    state.refreshToken = null;
    state.user = null;
    storage.removeItem('accessToken');
    storage.removeItem('refreshToken');
}

function updateUIForAuth(isAuth) {
    const authButtons = document.getElementById('nav-auth-buttons');
    const userMenu = document.getElementById('nav-user-menu');
    const notifications = document.getElementById('nav-notifications');

    if (isAuth) {
        authButtons.classList.add('d-none');
        userMenu.classList.remove('d-none');
        notifications.classList.remove('d-none');
        document.getElementById('user-display-name').innerText = state.user?.username || 'User';
    } else {
        authButtons.classList.remove('d-none');
        userMenu.classList.add('d-none');
        notifications.classList.add('d-none');
    }
}

// UI utilities
function showSpinner(show) {
    const spinner = document.getElementById('loading-spinner');
    if (show) {
        spinner.classList.remove('d-none');
    } else {
        spinner.classList.add('d-none');
    }
}

function showToast(message, type = 'primary') {
    const toastEl = document.getElementById('app-toast');
    const toastBody = document.getElementById('toast-message');
    toastEl.className = `toast align-items-center border-0 text-white bg-${type}`;
    toastBody.innerText = message;
    
    const toast = new bootstrap.Toast(toastEl);
    toast.show();
}

// ----------------- Dashboard Data Logic -----------------
async function loadDashboardData() {
    try {
        showSpinner(true);
        const res = await apiFetch('/dashboard/summary');
        if (res && res.status === 200) {
            const data = res.data;
            document.getElementById('dash-balance').innerText = formatCurrency(data.currentBalance);
            document.getElementById('dash-income').innerText = formatCurrency(data.totalIncome);
            document.getElementById('dash-expense').innerText = formatCurrency(data.totalExpense);
            document.getElementById('dash-today-income').innerText = formatCurrency(data.todayIncome);
            document.getElementById('dash-today-expense').innerText = formatCurrency(data.todayExpense);
            document.getElementById('dash-monthly-savings').innerText = formatCurrency(data.monthlySavings);
            document.getElementById('dash-health-score').innerText = `${data.financialHealthScore}/100`;

            // Populate table
            const txnBody = document.getElementById('dash-transactions-body');
            txnBody.innerHTML = '';
            if (data.latestTransactions && data.latestTransactions.length > 0) {
                data.latestTransactions.forEach(txn => {
                    const tr = document.createElement('tr');
                    const badgeClass = txn.type === 'INCOME' ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger';
                    tr.innerHTML = `
                        <td>${txn.date}</td>
                        <td>${txn.category}</td>
                        <td><span class="badge ${badgeClass}">${txn.type}</span></td>
                        <td>${txn.description || '-'}</td>
                        <td class="fw-bold ${txn.type === 'INCOME' ? 'text-success' : 'text-danger'}">${txn.type === 'INCOME' ? '+' : '-'}${formatCurrency(txn.amount)}</td>
                    `;
                    txnBody.appendChild(tr);
                });
            } else {
                txnBody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No recent transactions</td></tr>';
            }

            // Populate Budget progress
            const budgetContainer = document.getElementById('dash-budget-progress-container');
            budgetContainer.innerHTML = '';
            if (data.budgetProgress && data.budgetProgress.length > 0) {
                data.budgetProgress.forEach(b => {
                    const card = document.createElement('div');
                    card.className = 'mb-3';
                    const isExceeded = b.isExceeded;
                    const progressClass = isExceeded ? 'bg-danger progress-bar-flash' : 'bg-primary';
                    card.innerHTML = `
                        <div class="d-flex justify-content-between mb-1">
                            <span class="fw-bold small">${b.category}</span>
                            <span class="small text-muted">${formatCurrency(b.spentAmount)} / ${formatCurrency(b.limitAmount)}</span>
                        </div>
                        <div class="progress" style="height: 10px;">
                            <div class="progress-bar ${progressClass}" role="progressbar" style="width: ${Math.min(b.utilizationPercentage, 100)}%" aria-valuenow="${b.utilizationPercentage}" aria-valuemin="0" aria-valuemax="100"></div>
                        </div>
                    `;
                    budgetContainer.appendChild(card);
                });
            } else {
                budgetContainer.innerHTML = '<div class="text-center text-muted py-3">No active budgets</div>';
            }
        }
    } catch (err) {
        showToast('Failed to load dashboard statistics', 'danger');
    } finally {
        showSpinner(false);
    }
}

// ----------------- Income Module Logic -----------------
async function loadIncomeData() {
    try {
        showSpinner(true);
        const res = await apiFetch(`/incomes?page=${state.incomePage}&size=10&sortBy=transactionDate&direction=desc`);
        if (res) {
            const body = document.getElementById('income-table-body');
            body.innerHTML = '';
            res.data.content.forEach(inc => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${inc.transactionDate}</td>
                    <td>${inc.category}</td>
                    <td>${inc.description || '-'}</td>
                    <td class="text-success fw-bold">+${formatCurrency(inc.amount)}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary me-2" onclick="editTransaction(${inc.id}, 'INCOME')" aria-label="Edit income"><i class="bi bi-pencil"></i></button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteTransaction(${inc.id}, 'INCOME')" aria-label="Delete income"><i class="bi bi-trash"></i></button>
                    </td>
                `;
                body.appendChild(tr);
            });
            document.getElementById('income-page-indicator').innerText = `Page ${res.data.number + 1} of ${res.data.totalPages || 1}`;
            document.getElementById('income-prev-page-btn').disabled = res.data.first;
            document.getElementById('income-next-page-btn').disabled = res.data.last;
        }
    } catch (e) {
        showToast('Error loading incomes', 'danger');
    } finally {
        showSpinner(false);
    }
}

async function filterIncome(e) {
    e.preventDefault();
    const start = document.getElementById('income-filter-start').value;
    const end = document.getElementById('income-filter-end').value;
    const category = document.getElementById('income-filter-category').value;
    const search = document.getElementById('income-filter-search').value;

    let query = `/incomes?page=0&size=10&sortBy=transactionDate&direction=desc`;
    if (start) query += `&startDate=${start}`;
    if (end) query += `&endDate=${end}`;
    if (category) query += `&category=${category}`;
    if (search) query += `&search=${search}`;

    try {
        showSpinner(true);
        const res = await apiFetch(query);
        if (res) {
            const body = document.getElementById('income-table-body');
            body.innerHTML = '';
            res.data.content.forEach(inc => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${inc.transactionDate}</td>
                    <td>${inc.category}</td>
                    <td>${inc.description || '-'}</td>
                    <td class="text-success fw-bold">+${formatCurrency(inc.amount)}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary me-2" onclick="editTransaction(${inc.id}, 'INCOME')" aria-label="Edit income"><i class="bi bi-pencil"></i></button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteTransaction(${inc.id}, 'INCOME')" aria-label="Delete income"><i class="bi bi-trash"></i></button>
                    </td>
                `;
                body.appendChild(tr);
            });
        }
    } catch (e) {
        showToast('Filter request failed', 'danger');
    } finally {
        showSpinner(false);
    }
}

function changeIncomePage(dir) {
    state.incomePage += dir;
    loadIncomeData();
}

// ----------------- Expense Module Logic -----------------
async function loadExpenseData() {
    try {
        showSpinner(true);
        const res = await apiFetch(`/expenses?page=${state.expensePage}&size=10&sortBy=transactionDate&direction=desc`);
        if (res) {
            const body = document.getElementById('expense-table-body');
            body.innerHTML = '';
            res.data.content.forEach(exp => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${exp.transactionDate}</td>
                    <td>${exp.category}</td>
                    <td>${exp.description || '-'}</td>
                    <td class="text-danger fw-bold">-${formatCurrency(exp.amount)}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary me-2" onclick="editTransaction(${exp.id}, 'EXPENSE')" aria-label="Edit expense"><i class="bi bi-pencil"></i></button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteTransaction(${exp.id}, 'EXPENSE')" aria-label="Delete expense"><i class="bi bi-trash"></i></button>
                    </td>
                `;
                body.appendChild(tr);
            });
            document.getElementById('expense-page-indicator').innerText = `Page ${res.data.number + 1} of ${res.data.totalPages || 1}`;
            document.getElementById('expense-prev-page-btn').disabled = res.data.first;
            document.getElementById('expense-next-page-btn').disabled = res.data.last;
        }
    } catch (e) {
        showToast('Error loading expenses', 'danger');
    } finally {
        showSpinner(false);
    }
}

async function filterExpense(e) {
    e.preventDefault();
    const start = document.getElementById('expense-filter-start').value;
    const end = document.getElementById('expense-filter-end').value;
    const category = document.getElementById('expense-filter-category').value;
    const search = document.getElementById('expense-filter-search').value;

    let query = `/expenses?page=0&size=10&sortBy=transactionDate&direction=desc`;
    if (start) query += `&startDate=${start}`;
    if (end) query += `&endDate=${end}`;
    if (category) query += `&category=${category}`;
    if (search) query += `&search=${search}`;

    try {
        showSpinner(true);
        const res = await apiFetch(query);
        if (res) {
            const body = document.getElementById('expense-table-body');
            body.innerHTML = '';
            res.data.content.forEach(exp => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${exp.transactionDate}</td>
                    <td>${exp.category}</td>
                    <td>${exp.description || '-'}</td>
                    <td class="text-danger fw-bold">-${formatCurrency(exp.amount)}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary me-2" onclick="editTransaction(${exp.id}, 'EXPENSE')" aria-label="Edit expense"><i class="bi bi-pencil"></i></button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteTransaction(${exp.id}, 'EXPENSE')" aria-label="Delete expense"><i class="bi bi-trash"></i></button>
                    </td>
                `;
                body.appendChild(tr);
            });
        }
    } catch (e) {
        showToast('Filter request failed', 'danger');
    } finally {
        showSpinner(false);
    }
}

function changeExpensePage(dir) {
    state.expensePage += dir;
    loadExpenseData();
}

// Transaction Modals Handling
function showAddTransactionModal(type) {
    document.getElementById('txn-id').value = '';
    document.getElementById('txn-type').value = type;
    document.getElementById('txn-amount').value = '';
    document.getElementById('txn-desc').value = '';
    document.getElementById('txn-date').value = new Date().toISOString().substring(0, 10);
    
    document.getElementById('transactionModalTitle').innerText = `Add New ${type}`;

    const catSelect = document.getElementById('txn-category');
    catSelect.innerHTML = '';
    const categories = type === 'INCOME' ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
    categories.forEach(c => {
        catSelect.innerHTML += `<option value="${c}">${c}</option>`;
    });

    const modal = new bootstrap.Modal(document.getElementById('transactionModal'));
    modal.show();
}

async function editTransaction(id, type) {
    try {
        showSpinner(true);
        const endpoint = type === 'INCOME' ? `/incomes/${id}` : `/expenses/${id}`;
        const res = await apiFetch(endpoint);
        if (res) {
            document.getElementById('txn-id').value = res.data.id;
            document.getElementById('txn-type').value = type;
            document.getElementById('txn-amount').value = res.data.amount;
            document.getElementById('txn-desc').value = res.data.description || '';
            document.getElementById('txn-date').value = res.data.transactionDate;

            document.getElementById('transactionModalTitle').innerText = `Edit ${type}`;

            const catSelect = document.getElementById('txn-category');
            catSelect.innerHTML = '';
            const categories = type === 'INCOME' ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
            categories.forEach(c => {
                const selected = c === res.data.category ? 'selected' : '';
                catSelect.innerHTML += `<option value="${c}" ${selected}>${c}</option>`;
            });

            const modal = new bootstrap.Modal(document.getElementById('transactionModal'));
            modal.show();
        }
    } catch (e) {
        showToast('Failed to retrieve record', 'danger');
    } finally {
        showSpinner(false);
    }
}

async function handleSaveTransaction(e) {
    e.preventDefault();
    const id = document.getElementById('txn-id').value;
    const type = document.getElementById('txn-type').value;
    const amount = document.getElementById('txn-amount').value;
    const category = document.getElementById('txn-category').value;
    const transactionDate = document.getElementById('txn-date').value;
    const description = document.getElementById('txn-desc').value;

    const payload = { amount, category, transactionDate, description };
    const method = id ? 'PUT' : 'POST';
    const endpoint = type === 'INCOME' 
        ? (id ? `/incomes/${id}` : '/incomes') 
        : (id ? `/expenses/${id}` : '/expenses');

    try {
        showSpinner(true);
        const res = await apiFetch(endpoint, {
            method: method,
            body: JSON.stringify(payload)
        });
        if (res) {
            bootstrap.Modal.getInstance(document.getElementById('transactionModal')).hide();
            showToast(`${type} transaction saved successfully!`, 'success');
            if (type === 'INCOME') loadIncomeData();
            else loadExpenseData();
        }
    } catch (err) {
        showToast(err.message || 'Error occurred while saving', 'danger');
    } finally {
        showSpinner(false);
    }
}

async function deleteTransaction(id, type) {
    if (!confirm('Are you sure you want to delete this record?')) return;
    const endpoint = type === 'INCOME' ? `/incomes/${id}` : `/expenses/${id}`;
    try {
        showSpinner(true);
        await apiFetch(endpoint, { method: 'DELETE' });
        showToast('Record deleted successfully', 'success');
        if (type === 'INCOME') loadIncomeData();
        else loadExpenseData();
    } catch (err) {
        showToast('Failed to delete transaction', 'danger');
    } finally {
        showSpinner(false);
    }
}

// ----------------- Budget Module Logic -----------------
async function loadBudgetData() {
    try {
        showSpinner(true);
        const res = await apiFetch('/budgets');
        if (res) {
            const container = document.getElementById('budget-cards-container');
            container.innerHTML = '';
            res.data.forEach(b => {
                const card = document.createElement('div');
                card.className = 'col-md-4';
                const progressPct = b.limitAmount > 0 ? (b.spentAmount / b.limitAmount) * 100 : 0;
                const exceeded = b.spentAmount > b.limitAmount;
                const progressBarClass = exceeded ? 'bg-danger progress-bar-flash' : 'bg-primary';

                card.innerHTML = `
                    <div class="card border-0 shadow-sm rounded-4 p-4 h-100 position-relative">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <span class="badge bg-primary-subtle text-primary py-2 px-3 rounded-pill uppercase">${b.category}</span>
                            <div class="dropdown">
                                <button class="btn btn-link p-0 text-muted" type="button" data-bs-toggle="dropdown"><i class="bi bi-three-dots-vertical"></i></button>
                                <ul class="dropdown-menu dropdown-menu-end shadow border-0">
                                    <li><a class="dropdown-item" href="#" onclick="editBudget(${b.id})">Edit Limit</a></li>
                                    <li><a class="dropdown-item text-danger" href="#" onclick="deleteBudget(${b.id})">Delete Plan</a></li>
                                </ul>
                            </div>
                        </div>
                        <h4 class="fw-bold mb-1">${formatCurrency(b.spentAmount)} <span class="fs-6 text-muted font-normal">spent</span></h4>
                        <p class="text-muted small">of ${formatCurrency(b.limitAmount)} limit</p>
                        <div class="progress mb-3" style="height: 12px;">
                            <div class="progress-bar ${progressBarClass}" role="progressbar" style="width: ${Math.min(progressPct, 100)}%"></div>
                        </div>
                        <div class="d-flex justify-content-between small text-muted">
                            <span>Duration: ${b.startDate} to ${b.endDate}</span>
                        </div>
                    </div>
                `;
                container.appendChild(card);
            });
        }
    } catch (e) {
        showToast('Error loading budgets', 'danger');
    } finally {
        showSpinner(false);
    }
}

function showAddBudgetModal() {
    document.getElementById('budget-id').value = '';
    document.getElementById('budget-limit').value = '';
    document.getElementById('budget-category').value = 'ALL';
    document.getElementById('budget-start').value = new Date().toISOString().substring(0, 10);
    // Default duration: 1 month
    const end = new Date();
    end.setMonth(end.getMonth() + 1);
    document.getElementById('budget-end').value = end.toISOString().substring(0, 10);

    document.getElementById('budgetModalTitle').innerText = 'Create Budget Plan';
    const modal = new bootstrap.Modal(document.getElementById('budgetModal'));
    modal.show();
}

async function editBudget(id) {
    try {
        showSpinner(true);
        const res = await apiFetch(`/budgets/${id}`);
        if (res) {
            document.getElementById('budget-id').value = res.data.id;
            document.getElementById('budget-category').value = res.data.category;
            document.getElementById('budget-limit').value = res.data.limitAmount;
            document.getElementById('budget-start').value = res.data.startDate;
            document.getElementById('budget-end').value = res.data.endDate;

            document.getElementById('budgetModalTitle').innerText = 'Edit Budget Limit';
            const modal = new bootstrap.Modal(document.getElementById('budgetModal'));
            modal.show();
        }
    } catch (e) {
        showToast('Failed to fetch budget details', 'danger');
    } finally {
        showSpinner(false);
    }
}

async function handleSaveBudget(e) {
    e.preventDefault();
    const id = document.getElementById('budget-id').value;
    const category = document.getElementById('budget-category').value;
    const limitAmount = document.getElementById('budget-limit').value;
    const startDate = document.getElementById('budget-start').value;
    const endDate = document.getElementById('budget-end').value;

    const payload = { category, limitAmount, startDate, endDate };
    const method = id ? 'PUT' : 'POST';
    const endpoint = id ? `/budgets/${id}` : '/budgets';

    try {
        showSpinner(true);
        const res = await apiFetch(endpoint, {
            method: method,
            body: JSON.stringify(payload)
        });
        if (res) {
            bootstrap.Modal.getInstance(document.getElementById('budgetModal')).hide();
            showToast('Budget configured successfully', 'success');
            loadBudgetData();
        }
    } catch (err) {
        showToast(err.message || 'Failed to save budget plan', 'danger');
    } finally {
        showSpinner(false);
    }
}

async function deleteBudget(id) {
    if (!confirm('Are you sure you want to delete this budget plan?')) return;
    try {
        showSpinner(true);
        await apiFetch(`/budgets/${id}`, { method: 'DELETE' });
        showToast('Budget deleted successfully', 'success');
        loadBudgetData();
    } catch (err) {
        showToast('Failed to delete budget plan', 'danger');
    } finally {
        showSpinner(false);
    }
}

// ----------------- Reports Module Logic -----------------
function initReportsView() {
    // Populate default date ranges: 1 month back to today
    const startInput = document.getElementById('report-start-date');
    const endInput = document.getElementById('report-end-date');

    const today = new Date();
    const prevMonth = new Date();
    prevMonth.setMonth(prevMonth.getMonth() - 1);

    startInput.value = prevMonth.toISOString().substring(0, 10);
    endInput.value = today.toISOString().substring(0, 10);
}

function exportReport(format) {
    const start = document.getElementById('report-start-date').value;
    const end = document.getElementById('report-end-date').value;

    if (!start || !end) {
        showToast('Please select start and end dates', 'warning');
        return;
    }

    const url = `${API_BASE_URL}/reports/${format}?startDate=${start}&endDate=${end}`;
    
    showSpinner(true);
    fetch(url, {
        headers: {
            'Authorization': `Bearer ${state.accessToken}`
        }
    })
    .then(response => {
        if (!response.ok) throw new Error('Export failed');
        return response.blob();
    })
    .then(blob => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = downloadUrl;
        a.download = `fintrack_report_${start}_to_${end}.${format}`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(downloadUrl);
        showToast('Report downloaded successfully!', 'success');
    })
    .catch(() => {
        showToast('Failed to download file', 'danger');
    })
    .finally(() => {
        showSpinner(false);
    });
}

// ----------------- Visual Analytics (Chart.js) -----------------
async function loadAnalyticsData() {
    try {
        showSpinner(true);
        // We aggregate the last 6 months of data for charts from backend.
        // For simplicity, we can fetch all incomes and expenses from the last 1 year and group by month on client.
        const today = new Date();
        const start = new Date();
        start.setFullYear(start.getFullYear() - 1);

        const startDateStr = start.toISOString().substring(0, 10);
        const endDateStr = today.toISOString().substring(0, 10);

        const [incomesRes, expensesRes, budgetsRes] = await Promise.all([
            apiFetch(`/incomes?page=0&size=100&startDate=${startDateStr}&endDate=${endDateStr}`),
            apiFetch(`/expenses?page=0&size=100&startDate=${startDateStr}&endDate=${endDateStr}`),
            apiFetch('/budgets')
        ]);

        if (incomesRes && expensesRes && budgetsRes) {
            renderAnalyticsCharts(incomesRes.data.content, expensesRes.data.content, budgetsRes.data);
        }
    } catch (e) {
        showToast('Failed to generate visual analytics charts', 'danger');
    } finally {
        showSpinner(false);
    }
}

function renderAnalyticsCharts(incomes, expenses, budgets) {
    // Destroy previous chart instances to avoid canvas issues
    Object.keys(state.charts).forEach(key => {
        if (state.charts[key]) state.charts[key].destroy();
    });

    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const currentMonthIdx = new Date().getMonth();
    // Get last 6 months labels
    const chartLabels = [];
    const monthlyIncomeData = new Array(6).fill(0);
    const monthlyExpenseData = new Array(6).fill(0);

    for (let i = 5; i >= 0; i--) {
        const idx = (currentMonthIdx - i + 12) % 12;
        chartLabels.push(months[idx]);
    }

    // Map transaction amounts to the last 6 months
    incomes.forEach(inc => {
        const date = new Date(inc.transactionDate);
        const diff = (todayYearAndMonthDiff(new Date(), date));
        if (diff >= 0 && diff < 6) {
            monthlyIncomeData[5 - diff] += inc.amount;
        }
    });

    expenses.forEach(exp => {
        const date = new Date(exp.transactionDate);
        const diff = (todayYearAndMonthDiff(new Date(), date));
        if (diff >= 0 && diff < 6) {
            monthlyExpenseData[5 - diff] += exp.amount;
        }
    });

    // 1. Cashflow Chart (Double Bar)
    const ctxFlow = document.getElementById('chart-cashflow').getContext('2d');
    state.charts.flow = new Chart(ctxFlow, {
        type: 'bar',
        data: {
            labels: chartLabels,
            datasets: [
                { label: 'Income', data: monthlyIncomeData, backgroundColor: '#198754' },
                { label: 'Expense', data: monthlyExpenseData, backgroundColor: '#dc3545' }
            ]
        },
        options: { responsive: true, borderRadius: 6 }
    });

    // 2. Category Pie Chart (Expenses Category grouping)
    const categoryTotals = {};
    expenses.forEach(exp => {
        categoryTotals[exp.category] = (categoryTotals[exp.category] || 0) + exp.amount;
    });

    const categoryLabels = Object.keys(categoryTotals);
    const categoryData = Object.values(categoryTotals);

    const ctxCat = document.getElementById('chart-categories').getContext('2d');
    state.charts.cat = new Chart(ctxCat, {
        type: 'doughnut',
        data: {
            labels: categoryLabels.length > 0 ? categoryLabels : ['No Expenses'],
            datasets: [{
                data: categoryData.length > 0 ? categoryData : [1],
                backgroundColor: ['#fd7e14', '#0d6efd', '#6f42c1', '#343a40', '#0dcaf0', '#ffc107', '#198754', '#20c997', '#d63384']
            }]
        },
        options: { responsive: true }
    });

    // 3. Budgets Utilization (Horizontal bar)
    const budgetLabels = budgets.map(b => b.category);
    const budgetLimits = budgets.map(b => b.limitAmount);
    const budgetSpent = budgets.map(b => b.spentAmount);

    const ctxBud = document.getElementById('chart-budgets').getContext('2d');
    state.charts.bud = new Chart(ctxBud, {
        type: 'bar',
        data: {
            labels: budgetLabels.length > 0 ? budgetLabels : ['None'],
            datasets: [
                { label: 'Limit', data: budgetLimits.length > 0 ? budgetLimits : [0], backgroundColor: '#e9ecef' },
                { label: 'Spent', data: budgetSpent.length > 0 ? budgetSpent : [0], backgroundColor: '#0d6efd' }
            ]
        },
        options: { indexAxis: 'y', responsive: true, scales: { x: { stacked: false } } }
    });

    // 4. Variance Line Graph
    const varianceData = monthlyIncomeData.map((inc, i) => inc - monthlyExpenseData[i]);
    const ctxVar = document.getElementById('chart-variance').getContext('2d');
    state.charts.variance = new Chart(ctxVar, {
        type: 'line',
        data: {
            labels: chartLabels,
            datasets: [{
                label: 'Savings Variance',
                data: varianceData,
                borderColor: '#0dcaf0',
                tension: 0.3,
                fill: true,
                backgroundColor: 'rgba(13, 202, 240, 0.1)'
            }]
        },
        options: { responsive: true }
    });
}

function todayYearAndMonthDiff(d1, d2) {
    return (d1.getFullYear() - d2.getFullYear()) * 12 + d1.getMonth() - d2.getMonth();
}

// ----------------- Profile Details Loader -----------------
async function loadProfileData() {
    try {
        showSpinner(true);
        const res = await apiFetch('/users/profile');
        if (res) {
            document.getElementById('profile-fullname').value = res.data.fullName;
            document.getElementById('profile-email').value = res.data.email;
        }
    } catch (e) {
        showToast('Error loading profile', 'danger');
    } finally {
        showSpinner(false);
    }
}

async function handleUpdateProfile(e) {
    e.preventDefault();
    const fullName = document.getElementById('profile-fullname').value;
    const email = document.getElementById('profile-email').value;

    try {
        showSpinner(true);
        const res = await apiFetch('/users/profile', {
            method: 'PUT',
            body: JSON.stringify({ fullName, email })
        });
        if (res) {
            state.user.fullName = fullName;
            state.user.email = email;
            document.getElementById('user-display-name').innerText = fullName;
            showToast('Profile details updated successfully', 'success');
        }
    } catch (err) {
        showToast(err.message || 'Profile update failed', 'danger');
    } finally {
        showSpinner(false);
    }
}

async function handleChangePassword(e) {
    e.preventDefault();
    const oldPassword = document.getElementById('pwd-old').value;
    const newPassword = document.getElementById('pwd-new').value;

    try {
        showSpinner(true);
        await apiFetch('/users/change-password', {
            method: 'PUT',
            body: JSON.stringify({ oldPassword, newPassword })
        });
        document.getElementById('change-password-form').reset();
        showToast('Password changed successfully', 'success');
    } catch (err) {
        showToast(err.message || 'Password update failed', 'danger');
    } finally {
        showSpinner(false);
    }
}

async function deactivateAccount() {
    if (!confirm('Are you sure you want to deactivate your account? You will be immediately logged out.')) return;
    try {
        showSpinner(true);
        await apiFetch('/users/deactivate', { method: 'DELETE' });
        clearSession();
        updateUIForAuth(false);
        navigateTo('landing');
        showToast('Your account has been deactivated.', 'warning');
    } catch (err) {
        showToast('Account deactivation failed', 'danger');
    } finally {
        showSpinner(false);
    }
}

// ----------------- Notification widget loading -----------------
async function loadNotifications() {
    try {
        const res = await apiFetch('/notifications?unreadOnly=true');
        if (res && res.status === 200) {
            const list = document.getElementById('notification-list');
            const badge = document.getElementById('notification-badge');
            
            // Clear existing notification items except header/divider
            const header = list.querySelector('.dropdown-header');
            const divider = list.querySelector('.dropdown-divider');
            list.innerHTML = '';
            list.appendChild(header);
            list.appendChild(divider);

            const count = res.data.length;
            if (count > 0) {
                badge.innerText = count;
                badge.classList.remove('d-none');

                res.data.forEach(n => {
                    const li = document.createElement('li');
                    li.className = 'dropdown-item d-flex justify-content-between align-items-center py-2 border-bottom-dashed';
                    li.innerHTML = `
                        <div class="pe-2 small">
                            <span class="fw-bold block mb-1 text-primary">${n.type.replace('_', ' ')}</span>
                            <span>${n.message}</span>
                        </div>
                        <button class="btn btn-link p-0 text-decoration-none text-muted" onclick="markNotificationRead(event, ${n.id})" aria-label="Mark notification as read">
                            <i class="bi bi-check-circle"></i>
                        </button>
                    `;
                    list.appendChild(li);
                });
            } else {
                badge.innerText = '0';
                badge.classList.add('d-none');
                list.innerHTML += `<li class="dropdown-item text-center text-muted py-3 small">No new notifications</li>`;
            }
        }
    } catch (e) {
        console.error('Failed to load notifications', e);
    }
}

async function markNotificationRead(e, id) {
    e.stopPropagation();
    try {
        await apiFetch(`/notifications/${id}/read`, { method: 'PATCH' });
        loadNotifications();
    } catch (err) {
        console.error('Failed to mark notification read', err);
    }
}

async function markAllNotificationsRead(e) {
    e.preventDefault();
    e.stopPropagation();
    try {
        await apiFetch('/notifications/read-all', { method: 'PATCH' });
        loadNotifications();
    } catch (err) {
        console.error('Failed to mark all notifications read', err);
    }
}

// Simulated Password Reset dialog
function showForgotPasswordModal(e) {
    e.preventDefault();
    const email = prompt('Please enter your account email address to receive a temporary recovery password:');
    if (!email) return;

    showSpinner(true);
    fetch(`${API_BASE_URL}/auth/forgot-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
    })
    .then(res => res.json())
    .then(data => {
        if (data.status === 200) {
            alert('Simulation Successful! Your password has been temporarily reset to: TempPass123. Use this to login now.');
        } else {
            alert(data.message || 'No account matches that email.');
        }
    })
    .catch(() => alert('Connection failed'))
    .finally(() => showSpinner(false));
}

// Helpers
function formatCurrency(val) {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(val);
}

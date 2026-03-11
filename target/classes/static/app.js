const API_BASE = '/api/orders';
let authHeader = null;
let currentRole = null;
let selectedProducts = [];

// Restore auth from localStorage on page load
function restoreAuthFromStorage() {
    const stored = localStorage.getItem('authHeader');
    const role = localStorage.getItem('currentRole');
    if (stored && role) {
        authHeader = stored;
        currentRole = role;
        updateLoginUI();
        loadUserDetails();
        loadProducts();
        if (currentRole === 'ADMIN') {
            document.getElementById('customer-section').style.display = 'none';
        } else {
            document.getElementById('customer-section').style.display = 'block';
        }
        // hide customer header if needed
        const thCust = document.getElementById('th-customer');
        if (currentRole === 'CUSTOMER' && thCust) {
            thCust.style.display = 'none';
        } else if (thCust) {
            thCust.style.display = '';
        }
        // update column width after toggling section
        const ordersCol = document.getElementById('orders-column');
        if (document.getElementById('customer-section').style.display === 'none') {
            ordersCol.classList.remove('col-lg-7');
            ordersCol.classList.add('col-lg-12');
        } else {
            ordersCol.classList.remove('col-lg-12');
            ordersCol.classList.add('col-lg-7');
        }
        fetchOrders();
    }
}

// Update login UI based on auth state
function updateLoginUI() {
    const loginStatus = document.getElementById('login-status');
    const loginBtn = document.getElementById('login-btn');
    const logoutBtn = document.getElementById('logout-btn');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const mainContent = document.getElementById('main-content');
    const loginPrompt = document.getElementById('login-prompt');
    
    if (authHeader && currentRole) {
        loginStatus.textContent = `Logged in as ${currentRole.toLowerCase()}.`;
        usernameInput.style.display = 'none';
        passwordInput.style.display = 'none';
        loginBtn.style.display = 'none';
        logoutBtn.style.display = 'inline-block';
        mainContent.style.display = 'block';
        loginPrompt.style.display = 'none';

        // hide or show customer column header
        const thCust = document.getElementById('th-customer');
        if (currentRole === 'CUSTOMER' && thCust) {
            thCust.style.display = 'none';
        } else if (thCust) {
            thCust.style.display = '';
        }

        // when there is no customer section (admin view), make orders take full width
        const ordersCol = document.getElementById('orders-column');
        if (document.getElementById('customer-section').style.display === 'none') {
            ordersCol.classList.remove('col-lg-7');
            ordersCol.classList.add('col-lg-12');
        } else {
            ordersCol.classList.remove('col-lg-12');
            ordersCol.classList.add('col-lg-7');
        }
    } else {
        loginStatus.textContent = '';
        usernameInput.style.display = 'inline-block';
        passwordInput.style.display = 'inline-block';
        loginBtn.style.display = 'inline-block';
        logoutBtn.style.display = 'none';
        mainContent.style.display = 'none';
        loginPrompt.style.display = 'block';
    }
}

function createProductRow(product) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
        <td>${product.name}</td>
        <td>${product.description}</td>
        <td>$${product.price.toFixed(2)}</td>
        <td>
            <input type="number" class="form-control form-control-sm quantity-input" min="1" value="1" data-product-id="${product.id}" style="width: 80px;">
        </td>
        <td>
            <button type="button" class="btn btn-primary btn-sm add-to-cart" data-product-id="${product.id}">Add</button>
        </td>
    `;
    return tr;
}

function addToSelectedItems(product, quantity) {
    const existing = selectedProducts.find(item => item.product.id === product.id);
    if (existing) {
        existing.quantity += quantity;
    } else {
        selectedProducts.push({ product, quantity });
    }
    updateSelectedItemsDisplay();
}

function removeFromSelectedItems(productId) {
    selectedProducts = selectedProducts.filter(item => item.product.id !== productId);
    updateSelectedItemsDisplay();
}

function updateSelectedItemsDisplay() {
    const container = document.getElementById('selected-items');
    container.innerHTML = '';
    
    if (selectedProducts.length === 0) {
        container.innerHTML = '<p class="text-muted mb-0">No items added yet.</p>';
        return;
    }
    
    // Create a cart-like display
    const cartDiv = document.createElement('div');
    cartDiv.className = 'border rounded p-3 bg-light';
    
    const cartHeader = document.createElement('div');
    cartHeader.className = 'd-flex justify-content-between align-items-center mb-3';
    cartHeader.innerHTML = `
        <h6 class="mb-0">Order Cart (${selectedProducts.length} item${selectedProducts.length !== 1 ? 's' : ''})</h6>
        <div>
            <button type="button" class="btn btn-sm btn-outline-danger" id="clear-cart">Clear All</button>
        </div>
    `;
    cartDiv.appendChild(cartHeader);
    
    let totalAmount = 0;
    selectedProducts.forEach(item => {
        const itemDiv = document.createElement('div');
        itemDiv.className = 'd-flex justify-content-between align-items-center mb-2 p-2 border rounded';
        
        const itemTotal = item.product.price * item.quantity;
        totalAmount += itemTotal;
        
        itemDiv.innerHTML = `
            <div class="flex-grow-1">
                <strong>${item.product.name}</strong><br>
                <small class="text-muted">$${item.product.price.toFixed(2)} × ${item.quantity}</small>
            </div>
            <div class="text-end">
                <div class="fw-bold">$${itemTotal.toFixed(2)}</div>
                <button type="button" class="btn btn-sm btn-outline-danger remove-item" data-product-id="${item.product.id}">
                    <small>Remove</small>
                </button>
            </div>
        `;
        cartDiv.appendChild(itemDiv);
    });
    
    // Add total
    const totalDiv = document.createElement('div');
    totalDiv.className = 'border-top pt-2 mt-3';
    totalDiv.innerHTML = `<strong class="text-primary">Total: $${totalAmount.toFixed(2)}</strong>`;
    cartDiv.appendChild(totalDiv);
    
    container.appendChild(cartDiv);
    
    // Add event listener for clear cart button
    document.getElementById('clear-cart').addEventListener('click', () => {
        selectedProducts = [];
        updateSelectedItemsDisplay();
    });
    
    // Add event listener for place order button
    document.getElementById('place-order-btn').addEventListener('click', () => {
        document.getElementById('order-form').dispatchEvent(new Event('submit'));
    });
}

async function loadUserDetails() {
    try {
        const res = await fetch('/api/orders/user', { headers: getAuthHeaders() });
        if (res.ok) {
            const user = await res.json();
            document.getElementById('customerName').value = user.name;
            document.getElementById('customerEmail').value = user.email;
        }
    } catch (e) {
        console.error('Failed to load user details:', e);
    }
}

async function loadProducts() {
    try {
        const res = await fetch('/api/products', { headers: getAuthHeaders() });
        if (res.ok) {
            const products = await res.json();
            const tbody = document.getElementById('products-table-body');
            tbody.innerHTML = '';
            products.forEach(product => {
                tbody.appendChild(createProductRow(product));
            });
        }
    } catch (e) {
        console.error('Failed to load products:', e);
    }
}

function getAuthHeaders(extra = {}) {
    const headers = { ...extra };
    if (authHeader) {
        headers['Authorization'] = authHeader;
    }
    return headers;
}

async function fetchOrders() {
    const statusFilter = document.getElementById('status-filter').value;
    const url = statusFilter ? `${API_BASE}?status=${encodeURIComponent(statusFilter)}` : API_BASE;
    const tbody = document.getElementById('orders-table-body');
    tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-3">Loading...</td></tr>';
    try {
        const res = await fetch(url, { headers: getAuthHeaders() });
        if (!res.ok) throw new Error('Failed to load orders');
        const orders = await res.json();
        // Support both plain arrays and Spring Data Page responses
        const orderList = Array.isArray(orders) ? orders : (orders && Array.isArray(orders.content) ? orders.content : []);
        if (!orderList.length) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-3">No orders found.</td></tr>';
            return;
        }
        tbody.innerHTML = '';
        for (const order of orderList) {
            const tr = document.createElement('tr');
            tr.className = 'clickable-row';
            tr.dataset.orderId = order.id;
            const total = (order.products || []).reduce((sum, item) => {
                    const price = item.price || 0;
                    const qty = item.quantity || 0;
                    return sum + price * qty;
                }, 0);
            // hide customer column value if role is CUSTOMER
            tr.innerHTML = `
                <td>#${order.id}</td>
                ${currentRole === 'CUSTOMER' ? '' : `<td>${order.customerName || ''}</td>`}
                <td><span class="badge-status ${order.status}">${order.status}</span></td>
                <td>$${total.toFixed(2)}</td>
                <td>${order.createdAt ? new Date(order.createdAt).toLocaleString() : ''}</td>
                <td>
                    <button type="button" class="btn btn-sm btn-outline-danger cancel-btn" ${order.status !== 'PENDING' ? 'disabled' : ''}>
                        Cancel
                    </button>
                </td>
            `;
            tr.addEventListener('click', (e) => {
                if (e.target.classList.contains('cancel-btn')) {
                    return;
                }
                loadOrderDetails(order.id);
            });
            tr.querySelector('.cancel-btn').addEventListener('click', async (e) => {
                e.stopPropagation();
                await cancelOrder(order.id);
            });
            tbody.appendChild(tr);
        }
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-3">${e.message}</td></tr>`;
    }
}

async function loadOrderDetails(id) {
    const container = document.getElementById('order-details');
    container.innerHTML = '<p class="text-muted mb-0">Loading...</p>';
    try {
        const res = await fetch(`${API_BASE}/${id}?expand=products`, { headers: getAuthHeaders() });
        if (!res.ok) {
            container.innerHTML = '<p class="text-danger mb-0">Failed to load order details.</p>';
            return;
        }
        const order = await res.json();
        const items = order.products || [];
        const total = items.reduce((sum, item) => {
            const price = item.price || 0;
            const qty = item.quantity || 0;
            return sum + price * qty;
        }, 0);
        const itemsHtml = items.map(item => `
            <tr>
                <td>${item.name || ''}</td>
                <td>${item.quantity}</td>
                <td>$${(item.price || 0).toFixed(2)}</td>
                <td>$${((item.price || 0) * (item.quantity || 0)).toFixed(2)}</td>
            </tr>
        `).join('');
        let adminControls = '';
        if (currentRole === 'ADMIN') {
            adminControls = `
                <div class="mt-3 d-flex align-items-center gap-2">
                    <select id="admin-status-select" class="form-select form-select-sm" style="width: 180px;">
                        <option value="PENDING" ${order.status === 'PENDING' ? 'selected' : ''}>Pending</option>
                        <option value="PROCESSING" ${order.status === 'PROCESSING' ? 'selected' : ''}>Processing</option>
                        <option value="SHIPPED" ${order.status === 'SHIPPED' ? 'selected' : ''}>Shipped</option>
                        <option value="DELIVERED" ${order.status === 'DELIVERED' ? 'selected' : ''}>Delivered</option>
                        <option value="CANCELLED" ${order.status === 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                    </select>
                    <button type="button" class="btn btn-sm btn-outline-primary" id="admin-update-status-btn">
                        Update Status
                    </button>
                </div>
            `;
        }

        container.innerHTML = `
            <div class="d-flex justify-content-between align-items-center mb-2">
                <div>
                    <h6 class="mb-0">Order #${order.id}</h6>
                    <small class="text-muted">Created: ${order.createdAt ? new Date(order.createdAt).toLocaleString() : ''}</small>
                </div>
                <span class="badge-status ${order.status}">${order.status}</span>
            </div>
            <p class="mb-1"><strong>Customer:</strong> ${order.customerName || ''} (${order.customerEmail || ''})</p>
            <p class="mb-3"><strong>Shipping:</strong> ${order.shippingAddress || ''}</p>
            <div class="table-responsive mb-2">
                <table class="table table-sm">
                    <thead>
                    <tr>
                        <th>Product</th>
                        <th>Qty</th>
                        <th>Price</th>
                        <th>Total</th>
                    </tr>
                    </thead>
                    <tbody>
                    ${itemsHtml || '<tr><td colspan="4" class="text-muted">No items.</td></tr>'}
                    </tbody>
                </table>
            </div>
            <div class="text-end">
                <strong>Order Total: $${total.toFixed(2)}</strong>
            </div>
            ${adminControls}
        `;

        if (currentRole === 'ADMIN') {
            const btn = document.getElementById('admin-update-status-btn');
            if (btn) {
                btn.addEventListener('click', async () => {
                    const select = document.getElementById('admin-status-select');
                    const newStatus = select.value;
                    await updateOrderStatus(order.id, newStatus);
                });
            }
        }
    } catch (e) {
        container.innerHTML = `<p class="text-danger mb-0">${e.message}</p>`;
    }
}

async function cancelOrder(id) {
    if (!confirm(`Cancel order #${id}? This cannot be undone.`)) {
        return;
    }
    try {
        const res = await fetch(`${API_BASE}/${id}/cancel`, {
            method: 'POST',
            headers: getAuthHeaders()
        });
        if (res.status === 409) {
            alert('Order can only be cancelled while it is in PENDING status.');
            return;
        }
        if (!res.ok) {
            alert('Failed to cancel order.');
            return;
        }
        await fetchOrders();
        await loadOrderDetails(id);
    } catch (e) {
        alert(e.message);
    }
}

async function submitOrderForm(e) {
    e.preventDefault();
    const formMessage = document.getElementById('form-message');
    formMessage.textContent = '';
    if (!selectedProducts.length) {
        formMessage.textContent = 'Please select at least one product.';
        formMessage.className = 'mt-2 small text-danger';
        return;
    }
    const items = selectedProducts.map(item => ({
        productId: item.product.id,
        quantity: item.quantity
    }));
    const payload = {
        shippingAddress: document.getElementById('shippingAddress').value.trim(),
        items
    };
    try {
        const res = await fetch(API_BASE, {
            method: 'POST',
            headers: getAuthHeaders({ 'Content-Type': 'application/json' }),
            body: JSON.stringify(payload)
        });
        if (!res.ok) {
            throw new Error('Failed to create order.');
        }
        const created = await res.json();
        formMessage.textContent = `Order #${created.id} created successfully.`;
        formMessage.className = 'mt-2 small text-success';
        
        // Clear only shipping address and cart items, keep customer info for convenience
        document.getElementById('shippingAddress').value = '';
        selectedProducts = [];
        updateSelectedItemsDisplay();
        
        await fetchOrders();
        await loadOrderDetails(created.id);
    } catch (e) {
        formMessage.textContent = e.message;
        formMessage.className = 'mt-2 small text-danger';
    }
}

async function updateOrderStatus(id, status) {
    try {
        const res = await fetch(`${API_BASE}/${id}/status?status=${encodeURIComponent(status)}`, {
            method: 'PATCH',
            headers: getAuthHeaders()
        });
        if (!res.ok) {
            alert('Failed to update status. Make sure you are logged in as admin.');
            return;
        }
        await fetchOrders();
        await loadOrderDetails(id);
    } catch (e) {
        alert(e.message);
    }
}

function setupLogin() {
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const loginBtn = document.getElementById('login-btn');
    const logoutBtn = document.getElementById('logout-btn');
    const loginStatus = document.getElementById('login-status');

    loginBtn.addEventListener('click', async () => {
        const username = usernameInput.value.trim();
        const password = passwordInput.value;
        if (!username || !password) {
            loginStatus.textContent = 'Enter username and password.';
            return;
        }
        authHeader = 'Basic ' + btoa(`${username}:${password}`);
        try {
            // Test authentication by fetching user details (this will fail if not authenticated)
            const userRes = await fetch('/api/orders', { headers: getAuthHeaders() });
            if (userRes.ok) {
                const orders = await userRes.json();
                // Determine role from username (placeholder); persist auth
                currentRole = username === 'admin' ? 'ADMIN' : 'CUSTOMER';
                localStorage.setItem('authHeader', authHeader);
                localStorage.setItem('currentRole', currentRole);

                // Ensure layout reflects the role before updating UI so column widths adjust correctly
                if (currentRole === 'ADMIN') {
                    document.getElementById('customer-section').style.display = 'none';
                } else {
                    document.getElementById('customer-section').style.display = 'block';
                }

                updateLoginUI();
                await loadUserDetails();
                await loadProducts();
                await fetchOrders();
                document.getElementById('order-details').innerHTML = '<p class="text-muted mb-0">Select an order to see details.</p>';
            } else {
                throw new Error('Invalid credentials');
            }
        } catch (e) {
            loginStatus.textContent = 'Login failed.';
            authHeader = null;
            currentRole = null;
            localStorage.removeItem('authHeader');
            localStorage.removeItem('currentRole');
            updateLoginUI();
        }
    });

        logoutBtn.addEventListener('click', () => {
        authHeader = null;
        currentRole = null;
        localStorage.removeItem('authHeader');
        localStorage.removeItem('currentRole');
        usernameInput.value = '';
        passwordInput.value = '';
        // Show customer section first so updateLoginUI computes widths correctly
        document.getElementById('customer-section').style.display = 'block';
        updateLoginUI();
        document.getElementById('order-details').innerHTML = '<p class="text-muted mb-0">Select an order to see details.</p>';
        document.getElementById('orders-table-body').innerHTML = '';
        loginStatus.textContent = '';
    });
}

document.addEventListener('DOMContentLoaded', () => {
    // Restore authentication from localStorage
    restoreAuthFromStorage();
    
    // Handle adding products to cart
    document.addEventListener('click', (e) => {
        if (e.target.classList.contains('add-to-cart')) {
            const productId = parseInt(e.target.dataset.productId);
            const quantityInput = document.querySelector(`.quantity-input[data-product-id="${productId}"]`);
            const quantity = parseInt(quantityInput.value);
            // Fetch product details (assuming we have them from loadProducts)
            fetch('/api/products', { headers: getAuthHeaders() })
                .then(res => res.json())
                .then(products => {
                    const product = products.find(p => p.id === productId);
                    if (product) {
                        addToSelectedItems(product, quantity);
                    }
                });
        } else if (e.target.classList.contains('remove-item')) {
            const productId = parseInt(e.target.dataset.productId);
            removeFromSelectedItems(productId);
        }
    });

    document.getElementById('order-form').addEventListener('submit', submitOrderForm);
    document.getElementById('status-filter').addEventListener('change', fetchOrders);
    document.getElementById('refresh-orders').addEventListener('click', fetchOrders);

    setupLogin();
    setInterval(fetchOrders, 60_000);
});


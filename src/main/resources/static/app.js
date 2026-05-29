const API = "https://app-6ede98d9-36fc-4dca-8ff2-4a0a19615914.cleverapps.io/api/v1";
const state = {
    token: localStorage.getItem("macondo_token"),
    user: JSON.parse(localStorage.getItem("macondo_user") || "null")
};

const money = new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: "COP",
    maximumFractionDigits: 0
});

document.addEventListener("DOMContentLoaded", () => {
    bindAuthTabs();
    bindForms();
    renderSession();
    safeAction(loadProducts);
});

function bindAuthTabs() {
    document.querySelectorAll("[data-auth-tab]").forEach(tab => {
        tab.addEventListener("click", () => {
            const target = tab.dataset.authTab;
            document.querySelectorAll("[data-auth-tab]").forEach(item => item.classList.toggle("is-active", item === tab));
            document.querySelectorAll("[data-auth-panel]").forEach(panel => {
                panel.classList.toggle("is-active", panel.dataset.authPanel === target);
            });
        });
    });
}

function bindForms() {
    document.querySelector("#loginForm").addEventListener("submit", event => {
        event.preventDefault();
        safeAction(async () => {
            const response = await request("/auth/login", {
                method: "POST",
                body: formData(event.target),
                publicRequest: true
            });
            saveSession(response);
            toast("Sesion iniciada");
        });
    });

    document.querySelector("#registerForm").addEventListener("submit", event => {
        event.preventDefault();
        safeAction(async () => {
            const response = await request("/auth/register", {
                method: "POST",
                body: formData(event.target),
                publicRequest: true
            });
            saveSession(response);
            toast("Cuenta creada e iniciada");
        });
    });

    document.querySelector("#logoutBtn").addEventListener("click", () => {
        localStorage.removeItem("macondo_token");
        localStorage.removeItem("macondo_user");
        state.token = null;
        state.user = null;
        renderSession();
        toast("Sesion cerrada");
    });

    document.querySelector("#reloadProducts").addEventListener("click", () => safeAction(loadProducts));
    document.querySelector("#loadOrders").addEventListener("click", () => safeAction(loadOrders));
    document.querySelector("#loadAdminOrders").addEventListener("click", () => safeAction(loadAdminOrders));
}

async function loadProducts() {
    const container = document.querySelector("#products");
    container.innerHTML = "<p class='loading-state'>Cargando catalogo...</p>";
    const products = await request("/productos", { publicRequest: true });

    if (!products.length) {
        container.innerHTML = "<p class='empty-state'>No hay productos disponibles en este momento.</p>";
        return;
    }

    container.innerHTML = products.map(productCard).join("");
    container.querySelectorAll("[data-pay]").forEach(button => {
        button.addEventListener("click", () => safeAction(() => createPayment(button.dataset.pay)));
    });
}

function productCard(product) {
    const image = product.imageUrl || "/images/portada.jpg";
    const category = product.categoryName || "Joya artesanal";
    const materials = product.materialNames?.length ? product.materialNames.join(", ") : "Material artesanal";

    return `
        <article class="product-card">
            <img src="${escapeAttr(image)}" alt="${escapeAttr(product.name)}">
            <div class="product-body">
                <span class="badge">${escapeHtml(category)}</span>
                <h3>${escapeHtml(product.name)}</h3>
                <p>${escapeHtml(product.description || materials)}</p>
            </div>
            <div class="product-footer">
                <span class="price">${money.format(product.priceCents / 100)}</span>
                <button class="button primary" type="button" data-pay="${product.id}">Pagar con Wompi</button>
            </div>
        </article>
    `;
}

async function createPayment(productId) {
    requireSession();
    const address = shippingAddress();
    if (!address) {
        return;
    }

    const payment = await request("/pagos/crear", {
        method: "POST",
        body: {
            productId: Number(productId),
            shippingAddress: address
        }
    });

    toast(`Pedido ${payment.reference}. Total: ${money.format(payment.breakdown.totalCents / 100)}`);

    const widgetLoaded = await loadWompiWidget();
    if (!widgetLoaded || !window.WidgetCheckout) {
        toast("No se pudo cargar el widget de Wompi. Revisa la conexion a internet.", true);
        return;
    }

    const checkout = new WidgetCheckout({
        currency: payment.currency,
        amountInCents: payment.amountInCents,
        reference: payment.reference,
        publicKey: payment.publicKey,
        redirectUrl: window.location.href,
        signature: {
            integrity: payment.integritySignature
        },
        customerData: {
            email: state.user.email,
            fullName: state.user.name,
            phoneNumber: address.phone
        },
        shippingAddress: {
            addressLine1: address.addressLine,
            city: address.city,
            country: "CO",
            phoneNumber: address.phone,
            receiverName: address.recipientName
        }
    });

    checkout.open(result => {
        const status = result?.transaction?.status || "PENDING";
        toast(`Wompi reporto ${status}. El webhook confirma el estado final.`);
        safeAction(loadOrders);
    });
}

async function loadOrders() {
    requireSession();
    const orders = await request("/pedidos");
    document.querySelector("#orders").innerHTML = ordersTable(orders);
}

async function loadAdminOrders() {
    requireSession();
    const orders = await request("/admin/pedidos");
    const container = document.querySelector("#adminOrders");
    container.innerHTML = ordersTable(orders, true);
    container.querySelectorAll("[data-admin-status]").forEach(button => {
        button.addEventListener("click", () => safeAction(() => updateOrderStatus(button.dataset.orderId, button.dataset.adminStatus)));
    });
}

async function updateOrderStatus(orderId, status) {
    await request(`/admin/pedidos/${orderId}`, {
        method: "PATCH",
        body: { status }
    });
    toast(`Pedido actualizado a ${status}`);
    await loadAdminOrders();
}

function ordersTable(orders, admin = false) {
    if (!orders.length) {
        return "<p class='empty-state'>No hay pedidos registrados.</p>";
    }

    return `
        <table>
            <thead>
                <tr>
                    <th>Referencia</th>
                    <th>Producto</th>
                    <th>Ciudad</th>
                    <th>Total</th>
                    <th>Estado</th>
                    <th>Creado</th>
                    ${admin ? "<th>Accion</th>" : ""}
                </tr>
            </thead>
            <tbody>
                ${orders.map(order => `
                    <tr>
                        <td>${escapeHtml(order.reference)}</td>
                        <td>${escapeHtml(order.productName || "Producto")}</td>
                        <td>${escapeHtml(order.shippingCity || "-")}</td>
                        <td>${money.format(order.totalAmountCents / 100)}</td>
                        <td><span class="status-pill ${escapeAttr(order.status)}">${escapeHtml(order.status)}</span></td>
                        <td>${new Date(order.createdAt).toLocaleString("es-CO")}</td>
                        ${admin ? adminActions(order) : ""}
                    </tr>
                `).join("")}
            </tbody>
        </table>
    `;
}

function adminActions(order) {
    const canShip = order.status === "APPROVED";
    const canDeliver = order.status === "SHIPPED";
    return `
        <td>
            <button class="button compact" type="button" data-order-id="${order.id}" data-admin-status="SHIPPED" ${canShip ? "" : "disabled"}>Enviar</button>
            <button class="button compact" type="button" data-order-id="${order.id}" data-admin-status="DELIVERED" ${canDeliver ? "" : "disabled"}>Entregar</button>
        </td>
    `;
}

function shippingAddress() {
    const form = document.querySelector("#shippingForm");
    if (!form.reportValidity()) {
        return null;
    }
    return formData(form);
}

async function request(path, options = {}) {
    const headers = { "Content-Type": "application/json" };
    if (!options.publicRequest && state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    const response = await fetch(`${API}${path}`, {
        method: options.method || "GET",
        headers,
        body: options.body ? JSON.stringify(options.body) : undefined
    });

    if (!response.ok) {
        let message = "Solicitud fallida";
        try {
            const error = await response.json();
            message = error.message || error.error || message;
        } catch {
            message = response.statusText || message;
        }
        throw new Error(message);
    }

    if (response.status === 204) {
        return null;
    }
    return response.json();
}

function formData(form) {
    return Object.fromEntries(new FormData(form));
}

function saveSession(response) {
    state.token = response.token;
    state.user = {
        id: response.id,
        name: response.name,
        email: response.email,
        role: response.role
    };
    localStorage.setItem("macondo_token", state.token);
    localStorage.setItem("macondo_user", JSON.stringify(state.user));
    renderSession();
}

function renderSession() {
    const info = state.user ? `${state.user.name} - ${state.user.role}` : "Sin autenticar";
    document.querySelector("#sessionInfo").textContent = info;
}

function requireSession() {
    if (!state.token) {
        throw new Error("Inicia sesion antes de continuar");
    }
}

function safeAction(action) {
    Promise.resolve()
        .then(action)
        .catch(error => toast(error.message || "Ocurrio un error", true));
}

function loadWompiWidget() {
    if (window.WidgetCheckout) {
        return Promise.resolve(true);
    }
    if (loadWompiWidget.promise) {
        return loadWompiWidget.promise;
    }

    loadWompiWidget.promise = new Promise(resolve => {
        const script = document.createElement("script");
        script.src = "https://checkout.wompi.co/widget.js";
        script.async = true;
        script.onload = () => resolve(true);
        script.onerror = () => resolve(false);
        document.head.appendChild(script);
    });
    return loadWompiWidget.promise;
}

function toast(message, error = false) {
    const element = document.querySelector("#toast");
    element.textContent = message;
    element.classList.toggle("error", error);
    element.classList.add("show");
    window.clearTimeout(toast.timeout);
    toast.timeout = window.setTimeout(() => element.classList.remove("show"), 4200);
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function escapeAttr(value) {
    return escapeHtml(value).replaceAll("`", "&#096;");
}

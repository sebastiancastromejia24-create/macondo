const API = "/api/v1";
const state = {
    token: localStorage.getItem("macondo_token"),
    user: JSON.parse(localStorage.getItem("macondo_user") || "null")
};

const money = new Intl.NumberFormat("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 0 });

document.addEventListener("DOMContentLoaded", () => {
    bindForms();
    renderSession();
    loadProducts();
});

function bindForms() {
    document.querySelector("#loginForm").addEventListener("submit", async event => {
        event.preventDefault();
        const data = Object.fromEntries(new FormData(event.target));
        const response = await request("/auth/login", { method: "POST", body: data, publicRequest: true });
        saveSession(response);
        toast("Sesion iniciada");
    });

    document.querySelector("#registerForm").addEventListener("submit", async event => {
        event.preventDefault();
        const data = Object.fromEntries(new FormData(event.target));
        const response = await request("/auth/register", { method: "POST", body: data, publicRequest: true });
        saveSession(response);
        toast("Cuenta creada");
    });

    document.querySelector("#logoutBtn").addEventListener("click", () => {
        localStorage.removeItem("macondo_token");
        localStorage.removeItem("macondo_user");
        state.token = null;
        state.user = null;
        renderSession();
    });

    document.querySelector("#reloadProducts").addEventListener("click", loadProducts);
    document.querySelector("#loadOrders").addEventListener("click", loadOrders);
    document.querySelector("#loadAdminOrders").addEventListener("click", loadAdminOrders);
}

async function loadProducts() {
    const products = await request("/productos", { publicRequest: true });
    const container = document.querySelector("#products");
    container.innerHTML = products.map(product => `
        <article class="product-card">
            <img src="${product.imageUrl || "/images/portada.jpg"}" alt="${product.name}">
            <span class="badge">${product.categoryName || "Joya artesanal"}</span>
            <h3>${product.name}</h3>
            <p>${product.description || ""}</p>
            <div class="product-meta">
                <span class="price">${money.format(product.priceCents / 100)}</span>
                <button class="secondary" data-pay="${product.id}">Pagar</button>
            </div>
        </article>
    `).join("");
    container.querySelectorAll("[data-pay]").forEach(button => {
        button.addEventListener("click", () => createPayment(button.dataset.pay));
    });
}

async function createPayment(productId) {
    requireSession();
    const payment = await request("/pagos/crear", {
        method: "POST",
        body: {
            productId: Number(productId),
            shippingAddress: shippingAddress()
        }
    });

    toast(`Total a pagar: ${money.format(payment.breakdown.totalCents / 100)}`);
    if (!window.WidgetCheckout) {
        toast("No se pudo cargar el widget de Wompi. Revisa conexion a internet.", true);
        return;
    }

    const checkout = new WidgetCheckout({
        currency: payment.currency,
        amountInCents: payment.amountInCents,
        reference: payment.reference,
        publicKey: payment.publicKey,
        signature: { integrity: payment.integritySignature }
    });

    checkout.open(result => {
        const status = result?.transaction?.status || "PENDING";
        toast(`Wompi reporto estado ${status}. El backend confirma por webhook.`);
        loadOrders();
    });
}

async function loadOrders() {
    requireSession();
    const orders = await request("/pedidos");
    document.querySelector("#orders").innerHTML = table(orders);
}

async function loadAdminOrders() {
    requireSession();
    const orders = await request("/admin/pedidos");
    document.querySelector("#adminOrders").innerHTML = table(orders);
}

function table(orders) {
    if (!orders.length) {
        return "<p class='hint'>No hay pedidos registrados.</p>";
    }
    return `
        <table>
            <thead>
                <tr>
                    <th>Referencia</th>
                    <th>Producto</th>
                    <th>Total</th>
                    <th>Estado</th>
                    <th>Creado</th>
                </tr>
            </thead>
            <tbody>
                ${orders.map(order => `
                    <tr>
                        <td>${order.reference}</td>
                        <td>${order.productName}</td>
                        <td>${money.format(order.totalAmountCents / 100)}</td>
                        <td>${order.status}</td>
                        <td>${new Date(order.createdAt).toLocaleString("es-CO")}</td>
                    </tr>
                `).join("")}
            </tbody>
        </table>
    `;
}

function shippingAddress() {
    return Object.fromEntries(new FormData(document.querySelector("#shippingForm")));
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
            message = response.statusText;
        }
        toast(message, true);
        throw new Error(message);
    }

    if (response.status === 204) {
        return null;
    }
    return response.json();
}

function saveSession(response) {
    state.token = response.token;
    state.user = { id: response.id, name: response.name, email: response.email, role: response.role };
    localStorage.setItem("macondo_token", state.token);
    localStorage.setItem("macondo_user", JSON.stringify(state.user));
    renderSession();
}

function renderSession() {
    const info = state.user ? `${state.user.name} (${state.user.role})` : "Sin autenticar";
    document.querySelector("#sessionInfo").textContent = info;
}

function requireSession() {
    if (!state.token) {
        toast("Inicia sesion antes de continuar", true);
        throw new Error("Sesion requerida");
    }
}

function toast(message, error = false) {
    const element = document.querySelector("#toast");
    element.textContent = message;
    element.classList.toggle("error", error);
    element.classList.add("show");
    window.setTimeout(() => element.classList.remove("show"), 3600);
}

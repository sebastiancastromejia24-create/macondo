# Macondo Jewelry

Proyecto final de Desarrollo Empresarial para Macondo Joyeria: comercio electronico basico para joyeria artesanal colombiana con autenticacion JWT, catalogo, pedidos, reserva temporal de producto e integracion Wompi sandbox.

## Equipo

- Juan Diego: seguridad, JWT, roles y filtros.
- Nelson: logica de negocio, pedidos, reservas y modelo JPA.
- Sebastian: pagos Wompi, webhook, despliegue e infraestructura.
- Viasus: frontend, integracion end-to-end y panel admin.

## Arquitectura

La aplicacion usa Java 21 + Spring Boot 3.3.5 con arquitectura por capas:

- `Controller`: expone API REST versionada en `/api/v1/**`. No contiene logica de negocio.
- `Controller/Dtos`: agrupa los DTOs usados por los controladores.
- `Service`: coordina reglas como reserva de producto, calculo de comision Wompi, registro de pedido y autorizacion por dato.
- `Repository`: acceso a datos con Spring Data JPA.
- `Entity`: contiene entidades JPA y enums de estado.
- `Mapper`: mappers MapStruct entre entidades y DTOs.
- `Integration`: integraciones externas como Wompi y correo SMTP.
- `Common`: excepciones propias y respuesta de error estandarizada.

Modelo de negocio principal: `Product`, `Category`, `Material`, `CustomerOrder`, `ShippingAddress`, `PaymentTransaction` y `AppUser`. Incluye relaciones `ManyToMany` entre productos y materiales, y relaciones `ManyToOne/OneToMany` para categoria-productos, usuario-pedidos y pedido-transacciones.

Documento complementario: `docs/ARQUITECTURA.md`.
Guia para sustentar el codigo: `docs/GUIA_EXPOSICION.md`.

## Ejecucion local rapida

Requisitos:

- JDK 21 o superior.
- Maven 3.9 o Maven incluido en IntelliJ.

Para ejecutar sin instalar MySQL, usar H2 en memoria:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Luego abrir:

- Frontend: `http://localhost:8080/`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 console: `http://localhost:8080/h2-console`

Credenciales de prueba:

- Admin: `admin@macondo.local` / `admin12345`
- Cliente: se puede registrar desde el frontend.

## Variables de entorno

Para produccion con MySQL o PostgreSQL:

```text
DATABASE_URL=jdbc:postgresql://host:5432/db
DATABASE_USERNAME=usuario
DATABASE_PASSWORD=clave
DATABASE_DRIVER=org.postgresql.Driver
DATABASE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
JWT_SECRET=change-me-with-at-least-32-chars
WOMPI_PUBLIC_KEY=pub_test_xxx
WOMPI_INTEGRITY_SECRET=integrity_test_xxx
WOMPI_WEBHOOK_SECRET=webhook_test_xxx
WOMPI_COMMISSION_RATE=0.032
WOMPI_FIXED_FEE_CENTS=70000
ADMIN_EMAIL=admin@macondo.local
ADMIN_PASSWORD=admin12345
```

Las llaves Wompi nunca deben subirse al repositorio. Deben configurarse en el panel del proveedor cloud.

Para pruebas Sandbox usa llaves con prefijo `pub_test_`, `prv_test_`, `test_integrity_` o el equivalente mostrado por el dashboard. No uses llaves `pub_prod_` ni `prv_prod_` en ambientes de prueba. La llave privada solo es necesaria si el backend llama endpoints privados de Wompi; este proyecto usa el Widget Checkout, por eso el frontend recibe solo llave publica y firma de integridad generada en backend.

## Endpoints principales

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/productos`
- `GET /api/v1/productos/{id}`
- `POST /api/v1/pedidos`
- `GET /api/v1/pedidos`
- `GET /api/v1/pedidos/{reference}`
- `POST /api/v1/pagos/crear`
- `POST /api/v1/pagos/webhook`
- `GET /api/v1/admin/productos`
- `POST /api/v1/admin/productos`
- `PUT /api/v1/admin/productos/{id}`
- `PATCH /api/v1/admin/productos/{id}/desactivar`
- `DELETE /api/v1/admin/productos/{id}`
- `GET /api/v1/admin/pedidos?status=APPROVED`
- `PATCH /api/v1/admin/pedidos/{id}`

## Flujo Wompi

1. El cliente inicia sesion y selecciona un producto.
2. `POST /api/v1/pagos/crear` reserva el producto por 8 minutos, crea el pedido, calcula comision Wompi y devuelve `publicKey`, `reference`, `amountInCents` y `integritySignature`.
3. El frontend abre el widget de Wompi sandbox.
4. Wompi llama `POST /api/v1/pagos/webhook`.
5. El backend valida `X-Event-Checksum` o `signature.checksum`, registra `PaymentTransaction`, evita reprocesar eventos repetidos y cambia el pedido a `APPROVED`, `FAILED_PAYMENT` o `CANCELLED`.

URL de eventos recomendada en Wompi:

```text
https://TU-DOMINIO-BACKEND/api/v1/pagos/webhook
```

Si usas `https://macondo-six.vercel.app/` como dominio publico, verifica que esa ruta reenvie realmente al backend Spring Boot. Wompi debe llamar el endpoint del backend, no solo la raiz del frontend.

## Despliegue recomendado

Vercel sirve muy bien el frontend estatico y da dominio HTTPS gratis, pero el backend Spring Boot debe vivir en un proveedor que ejecute procesos Java persistentes, por ejemplo Render, Railway, Fly.io o Koyeb.

Pasos sugeridos:

1. Subir este repositorio a GitHub.
2. Crear servicio backend en Render usando `Dockerfile` o `render.yaml`.
3. Configurar variables de entorno y base PostgreSQL.
4. Copiar la URL HTTPS del backend.
5. En `vercel.json`, reemplazar `https://TU-BACKEND-HTTPS.onrender.com` por la URL real.
6. Importar el repositorio en Vercel. Vercel publicara la carpeta `public` y reenviara `/api/**` al backend.

## Pruebas

```bash
mvn test
```

Pruebas incluidas:

- Registro de cliente con JWT usando MockMvc.
- Firma de integridad Wompi y rechazo de webhook sin firma.

## Notas de alcance

Incluido: registro/login, roles CLIENTE/ADMIN, catalogo, pedidos, panel admin basico, reserva temporal, Wompi sandbox, webhook, correo de confirmacion al aprobar pago y frontend conectado al backend real.

No incluido: devoluciones automaticas, API oficial de WhatsApp Business, multiples pasarelas de pago y manejo de tarjetas dentro del sistema.

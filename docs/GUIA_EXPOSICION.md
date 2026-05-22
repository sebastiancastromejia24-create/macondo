# Guia de exposicion - Macondo Joyeria

## Resumen del proyecto

Macondo Joyeria es una aplicacion de comercio electronico para joyeria artesanal. El sistema permite registrar usuarios, iniciar sesion, consultar productos, reservar una joya, crear un pedido pendiente y abrir el checkout de Wompi en modo Sandbox. Cuando Wompi termina una transaccion, envia un evento al backend y el sistema actualiza el pedido.

## Flujo completo

1. El cliente entra al frontend.
2. El cliente se registra o inicia sesion.
3. El frontend consulta `GET /api/v1/productos`.
4. El cliente llena datos de envio.
5. El cliente presiona `Pagar con Wompi`.
6. El frontend envia `POST /api/v1/pagos/crear`.
7. El backend reserva el producto, crea el pedido y calcula el total.
8. El backend genera la firma de integridad para el widget de Wompi.
9. El frontend abre `WidgetCheckout` con la llave publica, referencia, valor y firma.
10. Wompi procesa el pago en Sandbox.
11. Wompi llama la URL de eventos: `/api/v1/pagos/webhook`.
12. El backend valida el checksum del evento.
13. Si el pago fue aprobado, el pedido queda `APPROVED` y el producto queda `SOLD`.
14. Si el pago falla, el pedido queda `FAILED_PAYMENT` y el producto vuelve a estar disponible.

## DTOs

Los DTOs quedaron divididos en dos paquetes:

- `Controller.dto.request`: contiene los datos que entran desde el frontend.
- `Controller.dto.response`: contiene los datos que devuelve el backend.

Esto evita mezclar contratos de entrada con contratos de salida y hace mas facil explicar que informacion recibe y que informacion responde cada endpoint.

## Explicacion por archivo

### `PaymentController`

- `@RestController`: indica que la clase expone endpoints REST.
- `@RequestMapping("/api/v1/pagos")`: todos los endpoints de esta clase empiezan por esa ruta.
- `create(...)`: recibe el usuario autenticado y el JSON de pago.
- `paymentService.createPayment(...)`: delega toda la regla de negocio al servicio.
- `webhook(...)`: recibe el cuerpo crudo del evento enviado por Wompi.
- `webhookSignature(...)`: busca el header `X-Event-Checksum`, que es la firma enviada por Wompi.

### `PaymentService`

- `createPayment(...)`: es el metodo central para iniciar un pago.
- `productService.find(productId)`: valida que el producto exista.
- `calculateCommission(...)`: calcula la comision configurada para Wompi.
- `orderService.createPendingOrder(...)`: reserva el producto y crea un pedido en estado `PENDING_PAYMENT`.
- `signatureService.integritySignature(...)`: crea la firma que Wompi exige para que el valor no sea alterado desde el navegador.
- `CreatePaymentResponse`: devuelve al frontend los datos necesarios para abrir el widget.
- `processWebhook(...)`: procesa el evento enviado por Wompi.
- `readWebhookRoot(...)`: convierte el JSON crudo en un objeto `JsonNode`.
- `isValidWebhookSignature(...)`: valida que el evento sea autentico.
- `readTransaction(...)`: extrae id, referencia, estado y monto.
- `findByWompiTransactionId(...)`: evita procesar dos veces el mismo evento.
- `applyPaymentStatus(...)`: cambia el estado del pedido segun `APPROVED`, `DECLINED`, `VOIDED` o `ERROR`.

### `WompiSignatureService`

- `integritySignature(...)`: concatena referencia, valor en centavos, moneda y secreto de integridad.
- `sha256Digest(...)`: aplica SHA-256 y devuelve el hash hexadecimal.
- `isValidWebhookSignature(...)`: valida eventos recibidos.
- `signature.properties`: Wompi envia una lista de campos que deben usarse para construir el checksum.
- `findByPath(...)`: permite leer rutas dinamicas como `transaction.id` o `transaction.amount_in_cents`.
- `webhookChecksum(...)`: concatena los campos indicados por Wompi, el `timestamp` y el secreto de eventos.
- `constantTimeEquals(...)`: compara firmas evitando diferencias de tiempo faciles de explotar.

### `OrderService`

- `createPendingOrder(...)`: reserva el producto y crea el pedido.
- `saveAddress(...)`: guarda los datos de envio asociados al usuario.
- `nextReference(...)`: genera una referencia unica con prefijo `MAC-`.
- `history(...)`: devuelve los pedidos del cliente autenticado.
- `updateShippingStatus(...)`: permite al administrador marcar un pedido como enviado o entregado.

### `ProductService`

- `availableProducts(...)`: devuelve solo productos disponibles para el catalogo publico.
- `reserve(...)`: bloquea temporalmente un producto durante el pago.
- `isAvailableForCheckout(...)`: evita vender un producto vendido o reservado.
- `deactivate(...)`: permite ocultar productos desde administracion.

### Frontend `app.js`

- `API`: define la base de la API.
- `state`: guarda token y usuario desde `localStorage`.
- `loadProducts()`: pinta el catalogo desde el backend.
- `createPayment(productId)`: envia producto y direccion al backend.
- `new WidgetCheckout(...)`: abre el checkout de Wompi.
- `loadOrders()`: carga los pedidos del cliente.
- `loadAdminOrders()`: carga pedidos para administracion.
- `request(...)`: centraliza `fetch`, headers, JWT y manejo de errores.
- `escapeHtml(...)`: evita insertar HTML peligroso en la pagina.

## Configuracion Wompi

Variables importantes:

- `WOMPI_PUBLIC_KEY`: llave publica del ambiente Sandbox, con prefijo `pub_test_`.
- `WOMPI_INTEGRITY_SECRET`: secreto de integridad del ambiente Sandbox.
- `WOMPI_WEBHOOK_SECRET`: secreto de eventos del ambiente Sandbox.
- `WOMPI_CURRENCY`: normalmente `COP`.

La llave privada no debe ir en el frontend ni en el repositorio. Para este flujo con widget no se necesita llamar directamente la API privada de transacciones.

La URL de eventos debe apuntar al endpoint publico del backend:

```text
https://TU-DOMINIO-BACKEND/api/v1/pagos/webhook
```

Si el dominio visible es Vercel pero el backend esta en Render, Railway o Fly, la URL de eventos debe ser la del backend o una ruta de Vercel que reenvie correctamente al backend.

## Preguntas posibles y respuestas

**Por que se usa JWT?**
Para identificar al cliente despues de iniciar sesion sin guardar estado de sesion en el servidor.

**Por que el backend genera la firma de integridad?**
Porque el secreto de integridad no debe estar en el navegador. Si estuviera en JavaScript, cualquier persona podria modificar montos y generar firmas falsas.

**Que pasa si Wompi envia dos veces el mismo evento?**
El sistema consulta `PaymentTransactionRepository` y no reprocesa un id ya registrado.

**Por que se guarda el pedido antes de abrir Wompi?**
Porque Wompi necesita una referencia unica. Esa referencia pertenece a un pedido real en nuestra base de datos.

**Que pasa si el pago queda pendiente?**
Se registra el evento, pero el pedido no se cierra. El estado final se aplica cuando Wompi envie `APPROVED`, `DECLINED`, `VOIDED` o `ERROR`.

**Por que el producto se reserva?**
Para evitar que dos clientes intenten pagar la misma joya al mismo tiempo.

**Por que se separaron DTOs request y response?**
Porque los datos de entrada y salida no siempre son iguales. Separarlos mejora orden, validacion y mantenimiento.

**Que diferencia hay entre llave publica, llave privada, secreto de integridad y secreto de eventos?**
La llave publica se usa para abrir el widget. La llave privada sirve para llamadas servidor a servidor a la API privada de Wompi. El secreto de integridad firma los datos del checkout. El secreto de eventos valida webhooks.

**Que endpoint se configura en Wompi como URL de eventos?**
`POST /api/v1/pagos/webhook` en el dominio HTTPS donde este publicado el backend.

**Como demostramos el flujo en clase?**
Inician sesion, escogen producto, pagan con el widget Sandbox, revisan el pedido y explican que el webhook es quien confirma el estado real.

## Fuentes oficiales consultadas

- Wompi, eventos y validacion de checksum: https://docs.wompi.co/docs/colombia/eventos/
- Wompi, Widget Checkout Web y firma de integridad: https://docs.wompi.co/docs/colombia/widget-checkout-web/
- Wompi, transacciones y campos principales: https://docs.wompi.co/docs/colombia/transacciones/

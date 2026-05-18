# Macondo Jewelry API

Backend Spring Boot para el CRUD y flujo transaccional descrito en `Proyecto Macondo Completo.docx`.

## Modulos

- Autenticacion JWT con roles `CLIENTE` y `ADMIN`.
- CRUD de productos para el catalogo artesanal.
- Creacion y seguimiento de pedidos.
- Reserva temporal de producto por 8 minutos con bloqueo optimista.
- Preparacion de pago Wompi con firma de integridad, desglose de comision y webhook idempotente.

## Variables de entorno

```text
JWT_SECRET=change-me-with-at-least-32-chars
WOMPI_PUBLIC_KEY=pub_test_xxx
WOMPI_INTEGRITY_SECRET=integrity_test_xxx
WOMPI_WEBHOOK_SECRET=webhook_test_xxx
WOMPI_COMMISSION_RATE=0.032
WOMPI_FIXED_FEE_CENTS=70000
```

## Ejecucion

```bash
mvn spring-boot:run
```

La base local usa H2 en memoria. Para produccion se debe configurar PostgreSQL y publicar el backend detras de HTTPS.

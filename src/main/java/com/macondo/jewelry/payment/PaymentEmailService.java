package com.macondo.jewelry.payment;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PaymentEmailService {
    private final JavaMailSender mailSender;

    public PaymentEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendApprovedPaymentEmail(String email, String customerName, String reference, String productName, long totalAmountCents) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Pago aprobado - Macondo Joyeria");
            message.setText("""
                    Hola %s,

                    Tu pago fue aprobado.
                    Pedido: %s
                    Producto: %s
                    Total: %d COP

                    Tiempo estimado de entrega: 3 a 5 dias habiles.
                    """.formatted(
                    customerName,
                    reference,
                    productName,
                    totalAmountCents / 100
            ));
            mailSender.send(message);
        } catch (RuntimeException ignored) {
            // El webhook no debe fallar ni tardar por problemas del proveedor de correo.
        }
    }
}

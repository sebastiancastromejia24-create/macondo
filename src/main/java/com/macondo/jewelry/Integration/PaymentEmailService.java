package com.macondo.jewelry.Integration;

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
    public void sendApprovedPaymentEmail(
            String email,
            String customerName,
            String reference,
            String productName,
            long totalAmountCents
    ) {

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(email);

            message.setSubject("Pago aprobado - Macondo Joyeria");

            message.setText(
                    "Hola " + customerName + ",\n\n" +
                            "Tu pago fue aprobado.\n" +
                            "Pedido: " + reference + "\n" +
                            "Producto: " + productName + "\n" +
                            "Total: " + (totalAmountCents / 100) + " COP\n\n" +
                            "Tiempo estimado de entrega: 3 a 5 dias habiles."
            );

            mailSender.send(message);

        } catch (RuntimeException ignored) {
            // Evita que falle el webhook si falla el correo
        }
    }
}

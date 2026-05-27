package com.macondo.jewelry.Integration;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PaymentEmailService {

    public PaymentEmailService() {

    }

    @Async
    public void sendApprovedPaymentEmail(
            String email,
            String customerName,
            String reference,
            String productName,
            long totalAmountCents
    ) {

        // TEMPORALMENTE DESACTIVADO EN CLEVER CLOUD

        System.out.println("Correo deshabilitado temporalmente");

    }

}

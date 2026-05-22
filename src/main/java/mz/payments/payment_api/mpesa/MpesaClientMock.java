package mz.payments.payment_api.mpesa;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MpesaClientMock implements MpesaClient {

    @Override
    public MpesaResponse initiatePayment(String phoneNumber, BigDecimal amount) {
        // Simulates M-Pesa accepting the payment request
        // In production this would be a real HTTP call to M-Pesa API
        String fakeTransactionId = "MPESA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new MpesaResponse(fakeTransactionId, "ACCEPTED", "Payment request received");
    }
}
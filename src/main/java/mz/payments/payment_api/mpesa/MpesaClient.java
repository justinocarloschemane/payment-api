package mz.payments.payment_api.mpesa;

import java.math.BigDecimal;

public interface MpesaClient {
    MpesaResponse initiatePayment(String phoneNumber, BigDecimal amount);
}
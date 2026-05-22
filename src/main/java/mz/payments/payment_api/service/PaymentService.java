package mz.payments.payment_api.service;

import mz.payments.payment_api.dto.PaymentRequest;
import mz.payments.payment_api.dto.PaymentResponse;
import mz.payments.payment_api.model.Payment;
import mz.payments.payment_api.mpesa.MpesaClient;
import mz.payments.payment_api.mpesa.MpesaResponse;
import mz.payments.payment_api.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MpesaClient mpesaClient;

    public PaymentService(PaymentRepository paymentRepository, MpesaClient mpesaClient) {
        this.paymentRepository = paymentRepository;
        this.mpesaClient = mpesaClient;
    }

    public PaymentResponse createPayment(PaymentRequest request) {
        // 1. Save payment as PENDING
        Payment payment = new Payment();
        payment.setPhoneNumber(request.getPhoneNumber());
        payment.setAmount(request.getAmount());
        Payment saved = paymentRepository.save(payment);

        // 2. Call M-Pesa
        MpesaResponse mpesaResponse = mpesaClient.initiatePayment(
                request.getPhoneNumber(),
                request.getAmount()
        );

        // 3. Update with M-Pesa transaction ID
        saved.setMpesaTransactionId(mpesaResponse.getTransactionId());
        paymentRepository.save(saved);

        return toResponse(saved);
    }

    public PaymentResponse getPayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return toResponse(payment);
    }

    public void handleWebhook(String transactionId, String status) {
        Payment payment = paymentRepository.findByMpesaTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus(status);
        paymentRepository.save(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPhoneNumber(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getMpesaTransactionId()
        );
    }
}
package mz.payments.payment_api.service;

import mz.payments.payment_api.dto.PaymentRequest;
import mz.payments.payment_api.dto.PaymentResponse;
import mz.payments.payment_api.model.Payment;
import mz.payments.payment_api.mpesa.MpesaClient;
import mz.payments.payment_api.mpesa.MpesaResponse;
import mz.payments.payment_api.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final MpesaClient mpesaClient;

    public PaymentService(PaymentRepository paymentRepository, MpesaClient mpesaClient) {
        this.paymentRepository = paymentRepository;
        this.mpesaClient = mpesaClient;
    }

    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating payment for phoneNumber={} amount={}",
                request.getPhoneNumber(), request.getAmount());

        Payment payment = new Payment();
        payment.setPhoneNumber(request.getPhoneNumber());
        payment.setAmount(request.getAmount());
        Payment saved = paymentRepository.save(payment);

        log.info("Payment saved with id={} status={}", saved.getId(), saved.getStatus());

        MpesaResponse mpesaResponse = mpesaClient.initiatePayment(
                request.getPhoneNumber(),
                request.getAmount()
        );

        log.info("M-Pesa responded with transactionId={} status={}",
                mpesaResponse.getTransactionId(), mpesaResponse.getStatus());

        saved.setMpesaTransactionId(mpesaResponse.getTransactionId());
        paymentRepository.save(saved);

        return toResponse(saved);
    }

    public PaymentResponse getPayment(UUID id) {
        log.info("Fetching payment id={}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return toResponse(payment);
    }

    public void handleWebhook(String transactionId, String status) {
        log.info("Webhook received transactionId={} status={}", transactionId, status);
        Payment payment = paymentRepository.findByMpesaTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus(status);
        paymentRepository.save(payment);
        log.info("Payment updated id={} newStatus={}", payment.getId(), status);
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
package mz.payments.payment_api.service;

import mz.payments.payment_api.dto.PaymentRequest;
import mz.payments.payment_api.dto.PaymentResponse;
import mz.payments.payment_api.model.Payment;
import mz.payments.payment_api.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentResponse createPayment(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setPhoneNumber(request.getPhoneNumber());
        payment.setAmount(request.getAmount());

        Payment saved = paymentRepository.save(payment);

        return toResponse(saved);
    }

    public PaymentResponse getPayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPhoneNumber(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCreatedAt()
        );
    }
}
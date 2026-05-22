package mz.payments.payment_api.controller;

import mz.payments.payment_api.dto.WebhookRequest;
import mz.payments.payment_api.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private final PaymentService paymentService;

    public WebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/mpesa")
    public ResponseEntity<Void> handleMpesaWebhook(@RequestBody WebhookRequest request) {
        paymentService.handleWebhook(request.getTransactionId(), request.getStatus());
        return ResponseEntity.ok().build();
    }
}
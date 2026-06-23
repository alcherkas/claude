package com.quickbite.payment.web;

import com.quickbite.payment.dto.PaymentResponse;
import com.quickbite.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Internal service-to-service API. Never exposed through the gateway.
 * Consumed by notification-service / order-service for payment status lookups.
 */
@RestController
@RequestMapping("/internal/payments")
@RequiredArgsConstructor
public class InternalPaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable UUID id) {
        return PaymentResponse.from(paymentService.getPayment(id));
    }

    @GetMapping
    public List<PaymentResponse> listByOrder(@RequestParam UUID orderId) {
        return paymentService.findByOrderId(orderId).stream()
                .map(PaymentResponse::from)
                .toList();
    }
}

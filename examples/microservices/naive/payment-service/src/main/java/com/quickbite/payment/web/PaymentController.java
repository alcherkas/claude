package com.quickbite.payment.web;

import com.quickbite.payment.dto.CreatePaymentRequest;
import com.quickbite.payment.dto.PaymentResponse;
import com.quickbite.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Public payment API, exposed through the gateway at /api/payments. */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return PaymentResponse.from(paymentService.createPayment(request));
    }

    @PostMapping("/{id}/refund")
    public PaymentResponse refund(@PathVariable UUID id) {
        return PaymentResponse.from(paymentService.refund(id));
    }

    @GetMapping
    public List<PaymentResponse> listByOrder(@RequestParam UUID orderId) {
        return paymentService.findByOrderId(orderId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable UUID id) {
        return PaymentResponse.from(paymentService.getPayment(id));
    }
}

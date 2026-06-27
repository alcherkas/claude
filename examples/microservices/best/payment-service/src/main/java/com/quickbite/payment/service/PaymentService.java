package com.quickbite.payment.service;

import com.quickbite.payment.client.OrderClient;
import com.quickbite.payment.client.WalletClient;
import com.quickbite.payment.domain.Payment;
import com.quickbite.payment.domain.PaymentMethod;
import com.quickbite.payment.domain.PaymentStatus;
import com.quickbite.payment.dto.CreatePaymentRequest;
import com.quickbite.payment.dto.OrderSummary;
import com.quickbite.payment.dto.WalletDebitRequest;
import com.quickbite.payment.event.PaymentEventProducer;
import com.quickbite.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Orchestrates payment capture and refunds.
 * Capture flow: resolve authoritative amount from order-service, charge via wallet or mock card PSP,
 * persist the result, and emit PaymentCaptured / PaymentFailed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String WALLET_PROVIDER = "wallet";

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final WalletClient walletClient;
    private final CardPspGateway cardPspGateway;
    private final PaymentEventProducer eventProducer;

    @Transactional
    public Payment createPayment(CreatePaymentRequest request) {
        OrderSummary order = orderClient.getOrder(request.orderId());
        if (!order.userId().equals(request.userId())) {
            throw new PaymentValidationException(
                    "Order " + order.id() + " does not belong to user " + request.userId());
        }
        long amountCents = order.totalCents();
        if (amountCents <= 0) {
            throw new PaymentValidationException("Order " + order.id() + " has no payable amount");
        }

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(order.id())
                .userId(request.userId())
                .amountCents(amountCents)
                .tipCents(order.tipCents())
                .currency(order.currency())
                .method(request.method())
                .status(PaymentStatus.AUTHORIZED)
                .provider(request.method() == PaymentMethod.WALLET ? WALLET_PROVIDER : CardPspGateway.PROVIDER_NAME)
                .createdAt(Instant.now())
                .build();

        boolean captured = capture(payment, order);
        payment.setStatus(captured ? PaymentStatus.CAPTURED : PaymentStatus.FAILED);
        Payment saved = paymentRepository.save(payment);

        if (captured) {
            eventProducer.publishPaymentCaptured(saved);
        } else {
            eventProducer.publishPaymentFailed(saved);
        }
        return saved;
    }

    private boolean capture(Payment payment, OrderSummary order) {
        try {
            if (payment.getMethod() == PaymentMethod.WALLET) {
                WalletDebitRequest debit = new WalletDebitRequest(
                        order.id(), payment.getAmountCents(), payment.getCurrency(),
                        "payment:" + payment.getId());
                walletClient.debit(payment.getUserId(), debit);
                log.info("Wallet debited for payment {} order {}", payment.getId(), order.id());
                return true;
            }
            CardPspGateway.CaptureResult result =
                    cardPspGateway.capture(order.id(), payment.getAmountCents(), payment.getCurrency());
            if (result.success()) {
                payment.setProvider(CardPspGateway.PROVIDER_NAME);
                return true;
            }
            return false;
        } catch (DependencyUnavailableException e) {
            log.warn("Capture failed for payment {}: {}", payment.getId(), e.getMessage());
            return false;
        }
    }

    @Transactional
    public Payment refund(UUID paymentId) {
        Payment payment = getPayment(paymentId);
        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            throw new PaymentValidationException(
                    "Payment " + paymentId + " is " + payment.getStatus() + "; only CAPTURED payments can be refunded");
        }

        if (payment.getMethod() == PaymentMethod.WALLET) {
            WalletDebitRequest credit = new WalletDebitRequest(
                    payment.getOrderId(), payment.getAmountCents(), payment.getCurrency(),
                    "refund:" + payment.getId());
            walletClient.credit(payment.getUserId(), credit);
            log.info("Wallet credited back for refund of payment {}", paymentId);
        } else {
            log.info("Mock PSP refunded {} {} for payment {}",
                    payment.getAmountCents(), payment.getCurrency(), paymentId);
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        Payment saved = paymentRepository.save(payment);
        eventProducer.publishRefunded(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment " + paymentId + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Payment> findByOrderId(UUID orderId) {
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }
}

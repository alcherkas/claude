package com.quickbite.payment.web;

import com.quickbite.payment.domain.Payment;
import com.quickbite.payment.domain.PaymentMethod;
import com.quickbite.payment.domain.PaymentStatus;
import com.quickbite.payment.service.PaymentNotFoundException;
import com.quickbite.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PaymentController.class, InternalPaymentController.class})
@Import(GlobalExceptionHandler.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Test
    void getPaymentReturnsPayment() throws Exception {
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .id(id)
                .orderId(orderId)
                .userId(UUID.randomUUID())
                .amountCents(1380)
                .currency("USD")
                .method(PaymentMethod.WALLET)
                .status(PaymentStatus.CAPTURED)
                .provider("wallet")
                .createdAt(Instant.now())
                .build();
        given(paymentService.getPayment(id)).willReturn(payment);

        mockMvc.perform(get("/api/payments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("CAPTURED"))
                .andExpect(jsonPath("$.amountCents").value(1380));
    }

    @Test
    void getMissingPaymentReturns404WithErrorBody() throws Exception {
        UUID id = UUID.randomUUID();
        given(paymentService.getPayment(id))
                .willThrow(new PaymentNotFoundException("Payment " + id + " not found"));

        mockMvc.perform(get("/api/payments/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/payments/" + id));
    }
}

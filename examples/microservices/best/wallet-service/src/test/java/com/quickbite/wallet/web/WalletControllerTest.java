package com.quickbite.wallet.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.wallet.dto.CreditRequest;
import com.quickbite.wallet.dto.WalletResponse;
import com.quickbite.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    WalletService walletService;

    @Test
    void getWalletReturnsBalance() throws Exception {
        WalletResponse response = new WalletResponse(
                USER_ID, 2500, "USD", Instant.parse("2026-06-22T10:00:00Z"));
        when(walletService.getWallet(USER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/wallets/" + USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.balanceCents").value(2500))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void creditReturnsUpdatedWallet() throws Exception {
        WalletResponse response = new WalletResponse(
                USER_ID, 5000, "USD", Instant.parse("2026-06-22T10:05:00Z"));
        when(walletService.credit(eq(USER_ID), any(CreditRequest.class))).thenReturn(response);

        CreditRequest body = new CreditRequest(2500L, "promo top-up");

        mockMvc.perform(post("/api/wallets/" + USER_ID + "/credits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceCents").value(5000));
    }

    @Test
    void creditRejectsNonPositiveAmount() throws Exception {
        CreditRequest body = new CreditRequest(0L, "bad");

        mockMvc.perform(post("/api/wallets/" + USER_ID + "/credits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/wallets/" + USER_ID + "/credits"));
    }
}

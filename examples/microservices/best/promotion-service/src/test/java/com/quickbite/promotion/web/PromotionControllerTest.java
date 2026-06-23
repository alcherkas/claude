package com.quickbite.promotion.web;

import com.quickbite.promotion.domain.PromotionType;
import com.quickbite.promotion.dto.ValidationResponse;
import com.quickbite.promotion.service.PromotionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromotionController.class)
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotionService promotionService;

    @Test
    void validateReturnsDiscountForEligibleCode() throws Exception {
        when(promotionService.validate(eq("WELCOME10"), anyLong(), any()))
                .thenReturn(ValidationResponse.valid(PromotionType.PERCENT, 250L));

        mockMvc.perform(get("/api/promotions/validate")
                        .param("code", "WELCOME10")
                        .param("subtotalCents", "2500")
                        .param("userId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.type").value("PERCENT"))
                .andExpect(jsonPath("$.discountCents").value(250));
    }

    @Test
    void validateReturnsReasonForUnknownCode() throws Exception {
        when(promotionService.validate(eq("NOPE"), anyLong(), any()))
                .thenReturn(ValidationResponse.invalid("Unknown promo code"));

        mockMvc.perform(get("/api/promotions/validate")
                        .param("code", "NOPE")
                        .param("subtotalCents", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.reason").value("Unknown promo code"));
    }
}

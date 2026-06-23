package com.quickbite.promotion.web;

import com.quickbite.promotion.dto.CreatePromotionRequest;
import com.quickbite.promotion.dto.PromotionResponse;
import com.quickbite.promotion.dto.ValidationResponse;
import com.quickbite.promotion.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionResponse create(@Valid @RequestBody CreatePromotionRequest request) {
        return promotionService.create(request);
    }

    @GetMapping
    public List<PromotionResponse> list() {
        return promotionService.list();
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidationResponse> validate(
            @RequestParam("code") String code,
            @RequestParam("subtotalCents") long subtotalCents,
            @RequestParam(value = "userId", required = false) Long userId) {
        return ResponseEntity.ok(promotionService.validate(code, subtotalCents, userId));
    }
}

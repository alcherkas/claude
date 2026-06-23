package com.quickbite.wallet.web;

import com.quickbite.wallet.dto.CreditRequest;
import com.quickbite.wallet.dto.WalletResponse;
import com.quickbite.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Public wallet API, reached through the gateway at /api/wallets/**. */
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{userId}")
    public WalletResponse getWallet(@PathVariable UUID userId) {
        return walletService.getWallet(userId);
    }

    @PostMapping("/{userId}/credits")
    public WalletResponse credit(@PathVariable UUID userId,
                                 @Valid @RequestBody CreditRequest request) {
        return walletService.credit(userId, request);
    }
}

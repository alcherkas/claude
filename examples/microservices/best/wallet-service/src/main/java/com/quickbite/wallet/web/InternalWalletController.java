package com.quickbite.wallet.web;

import com.quickbite.wallet.dto.DebitRequest;
import com.quickbite.wallet.dto.WalletResponse;
import com.quickbite.wallet.dto.WalletTxnResponse;
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

/**
 * Service-to-service API. Never exposed through the gateway.
 * Consumed by payment-service to read balances and debit a wallet when a
 * customer pays for an order with method WALLET.
 */
@RestController
@RequestMapping("/internal/wallets")
@RequiredArgsConstructor
public class InternalWalletController {

    private final WalletService walletService;

    @GetMapping("/{userId}")
    public WalletResponse getWallet(@PathVariable UUID userId) {
        return walletService.getWallet(userId);
    }

    @PostMapping("/{userId}/debit")
    public WalletTxnResponse debit(@PathVariable UUID userId,
                                   @Valid @RequestBody DebitRequest request) {
        return walletService.debit(userId, request);
    }

    @PostMapping("/{userId}/credit")
    public WalletTxnResponse credit(@PathVariable UUID userId,
                                    @Valid @RequestBody DebitRequest request) {
        return walletService.creditForOrder(userId, request);
    }
}

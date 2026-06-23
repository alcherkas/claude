package com.quickbite.payment.client;

import com.quickbite.payment.dto.WalletDebitRequest;
import com.quickbite.payment.dto.WalletTxnResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "walletClient", url = "${clients.wallet.url}", fallback = WalletClientFallback.class)
public interface WalletClient {

    @PostMapping("/internal/wallets/{userId}/debit")
    WalletTxnResponse debit(@PathVariable("userId") UUID userId, @RequestBody WalletDebitRequest request);

    @PostMapping("/internal/wallets/{userId}/credit")
    WalletTxnResponse credit(@PathVariable("userId") UUID userId, @RequestBody WalletDebitRequest request);
}

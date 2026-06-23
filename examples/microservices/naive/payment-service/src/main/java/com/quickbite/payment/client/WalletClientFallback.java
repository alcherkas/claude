package com.quickbite.payment.client;

import com.quickbite.payment.dto.WalletDebitRequest;
import com.quickbite.payment.dto.WalletTxnResponse;
import com.quickbite.payment.service.DependencyUnavailableException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WalletClientFallback implements WalletClient {

    @Override
    public WalletTxnResponse debit(UUID userId, WalletDebitRequest request) {
        throw new DependencyUnavailableException("wallet-service is unavailable; cannot debit wallet");
    }

    @Override
    public WalletTxnResponse credit(UUID userId, WalletDebitRequest request) {
        throw new DependencyUnavailableException("wallet-service is unavailable; cannot credit wallet");
    }
}

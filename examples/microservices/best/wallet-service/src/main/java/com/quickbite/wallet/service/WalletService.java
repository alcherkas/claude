package com.quickbite.wallet.service;

import com.quickbite.wallet.client.IdentityClient;
import com.quickbite.wallet.domain.Wallet;
import com.quickbite.wallet.domain.WalletTxn;
import com.quickbite.wallet.dto.CreditRequest;
import com.quickbite.wallet.dto.DebitRequest;
import com.quickbite.wallet.dto.WalletResponse;
import com.quickbite.wallet.dto.WalletTxnResponse;
import com.quickbite.wallet.repository.WalletRepository;
import com.quickbite.wallet.repository.WalletTxnRepository;
import com.quickbite.wallet.web.InsufficientFundsException;
import com.quickbite.wallet.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private static final String DEFAULT_CURRENCY = "USD";

    private final WalletRepository walletRepository;
    private final WalletTxnRepository walletTxnRepository;
    private final IdentityClient identityClient;

    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID userId) {
        Wallet wallet = walletRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("No wallet for user " + userId));
        return toResponse(wallet);
    }

    /**
     * Credit (top up) a wallet, creating it on first use. The user is validated
     * against identity-service before a brand-new wallet is opened.
     */
    @Transactional
    public WalletResponse credit(UUID userId, CreditRequest request) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId).orElse(null);
        if (wallet == null) {
            if (!identityClient.validate(userId).valid()) {
                throw new NotFoundException("User " + userId + " does not exist; cannot open a wallet");
            }
            wallet = new Wallet(userId, DEFAULT_CURRENCY);
        }

        wallet.applyDelta(request.amountCents());
        walletTxnRepository.save(new WalletTxn(userId, request.amountCents(), request.reason()));
        return toResponse(walletRepository.save(wallet));
    }

    /**
     * Credit a wallet against an order reference (e.g. a payment refund) and return
     * the resulting ledger entry. Called by payment-service via
     * POST /internal/wallets/{userId}/credit. The wallet is opened on first use
     * after validating the user against identity-service.
     */
    @Transactional
    public WalletTxnResponse creditForOrder(UUID userId, DebitRequest request) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId).orElse(null);
        if (wallet == null) {
            if (!identityClient.validate(userId).valid()) {
                throw new NotFoundException("User " + userId + " does not exist; cannot open a wallet");
            }
            wallet = new Wallet(userId, DEFAULT_CURRENCY);
        }

        wallet.applyDelta(request.amountCents());
        WalletTxn txn = walletTxnRepository.save(
                new WalletTxn(userId, request.amountCents(), "order:" + request.orderId()));
        walletRepository.save(wallet);

        return new WalletTxnResponse(
                txn.getId(), userId, txn.getDeltaCents(), txn.getReason(),
                wallet.getBalanceCents(), txn.getCreatedAt());
    }

    /**
     * Debit a wallet for an order payment. Rejects with {@link InsufficientFundsException}
     * when the balance would go negative. Called by payment-service via /internal.
     */
    @Transactional
    public WalletTxnResponse debit(UUID userId, DebitRequest request) {
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new NotFoundException("No wallet for user " + userId));

        if (!wallet.canDebit(request.amountCents())) {
            throw new InsufficientFundsException(
                    "Insufficient funds: wallet has " + wallet.getBalanceCents()
                            + " cents but order " + request.orderId() + " requires " + request.amountCents());
        }

        wallet.applyDelta(-request.amountCents());
        WalletTxn txn = walletTxnRepository.save(
                new WalletTxn(userId, -request.amountCents(), "order:" + request.orderId()));
        walletRepository.save(wallet);

        return new WalletTxnResponse(
                txn.getId(), userId, txn.getDeltaCents(), txn.getReason(),
                wallet.getBalanceCents(), txn.getCreatedAt());
    }

    private WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getUserId(), wallet.getBalanceCents(),
                wallet.getCurrency(), wallet.getUpdatedAt());
    }
}

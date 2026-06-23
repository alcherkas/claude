package com.quickbite.wallet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * An append-only ledger entry recording a single balance movement on a wallet.
 * {@code deltaCents} is positive for credits and negative for debits.
 */
@Entity
@Table(name = "wallet_txns")
@Getter
@Setter
@NoArgsConstructor
public class WalletTxn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "delta_cents", nullable = false)
    private long deltaCents;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public WalletTxn(UUID userId, long deltaCents, String reason) {
        this.userId = userId;
        this.deltaCents = deltaCents;
        this.reason = reason;
        this.createdAt = Instant.now();
    }
}

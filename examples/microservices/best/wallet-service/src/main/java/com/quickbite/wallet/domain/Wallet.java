package com.quickbite.wallet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * A user's wallet. The owning user's id (from identity-service) is the primary key —
 * one wallet per user. Balance is held in integer cents and may never go negative.
 */
@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
public class Wallet {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "balance_cents", nullable = false)
    private long balanceCents;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Wallet(UUID userId, String currency) {
        this.userId = userId;
        this.currency = currency;
        this.balanceCents = 0L;
        this.updatedAt = Instant.now();
    }

    /** Apply a signed delta (positive = credit, negative = debit) and bump the timestamp. */
    public void applyDelta(long deltaCents) {
        long updated = this.balanceCents + deltaCents;
        if (updated < 0) {
            throw new IllegalStateException("Wallet balance cannot go negative");
        }
        this.balanceCents = updated;
        this.updatedAt = Instant.now();
    }

    public boolean canDebit(long amountCents) {
        return this.balanceCents >= amountCents;
    }
}

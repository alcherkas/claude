package com.quickbite.wallet.repository;

import com.quickbite.wallet.domain.WalletTxn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WalletTxnRepository extends JpaRepository<WalletTxn, Long> {

    List<WalletTxn> findByUserIdOrderByCreatedAtDesc(UUID userId);
}

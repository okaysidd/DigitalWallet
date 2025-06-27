package com.digitalwallet.digitalwallet.repositories;

import com.digitalwallet.digitalwallet.models.TransactionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface ITransactionEntryRepository extends JpaRepository<TransactionEntry, Long> {
}

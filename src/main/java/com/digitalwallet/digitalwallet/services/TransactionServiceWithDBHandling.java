package com.digitalwallet.digitalwallet.services;

import com.digitalwallet.digitalwallet.models.DigitalWalletEnums;
import com.digitalwallet.digitalwallet.models.TransactionEntry;
import com.digitalwallet.digitalwallet.models.TransferMoneyBetweenWalletsRequest;
import com.digitalwallet.digitalwallet.models.Wallet;
import com.digitalwallet.digitalwallet.repositories.ITransactionEntryRepository;
import com.digitalwallet.digitalwallet.repositories.IWalletRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class TransactionServiceWithDBHandling implements ITransactionService {

    private final static BigDecimal MAXIMUM_AMOUNT_ALLOWED = BigDecimal.valueOf(10000);

    private final IWalletService walletService;
    private final IWalletRepository walletRepository;
    private final ITransactionEntryRepository transactionEntryRepository;

    public TransactionServiceWithDBHandling(IWalletService walletService, IWalletRepository walletRepository, ITransactionEntryRepository transactionEntryRepository) {
        this.walletService = walletService;
        this.walletRepository = walletRepository;
        this.transactionEntryRepository = transactionEntryRepository;
    }

    @Retryable(
            include = {org.springframework.dao.CannotAcquireLockException.class,
                    jakarta.persistence.PessimisticLockException.class,
                    org.springframework.orm.jpa.JpaSystemException.class},
            maxAttempts = 3,
            backoff = @org.springframework.retry.annotation.Backoff(delay = 200, multiplier = 2))
    @Transactional
    @Override
    public Boolean sendMoneyBetweenWallets(TransferMoneyBetweenWalletsRequest transferMoneyBetweenWalletsRequest) {
        log.info("sendMoneyBetweenWallets called-------------");
        Wallet sendingWallet = walletService.getWalletWithLock(transferMoneyBetweenWalletsRequest.getSendingWalletId());
        Wallet receivingWallet = walletService.getWalletWithLock(transferMoneyBetweenWalletsRequest.getReceivingWalletId());
        BigDecimal amount = transferMoneyBetweenWalletsRequest.getAmount();

        if (!validate(sendingWallet, receivingWallet, amount)) {
            log.error("Transaction not allowed as MAXIMUM_AMOUNT_ALLOWED reached or not enough balance");
            return false;
        }
        return initiateTransfer(sendingWallet, receivingWallet, amount);
    }

    @Transactional
    private Boolean initiateTransfer(Wallet sendingWallet, Wallet receivingWallet, BigDecimal amount) {
        try {
            sendingWallet.removeFromBalance(amount);
            walletRepository.save(sendingWallet);

            // add to receiver
            receivingWallet.addToBalance(amount);
            walletRepository.save(receivingWallet);
            createTransactionEntries(sendingWallet, receivingWallet, amount);
            return true;
        } catch (RuntimeException ex) {
            long createdAt = System.currentTimeMillis();
            createTransactionEntry(sendingWallet.getId(), amount, true, DigitalWalletEnums.TransactionStatus.FAILED, createdAt);
            createTransactionEntry(receivingWallet.getId(), amount, false, DigitalWalletEnums.TransactionStatus.FAILED, createdAt);
            log.error(ex.getMessage());
            throw new RuntimeException("Transaction failed");
        }
    }

    private void createTransactionEntries(Wallet sendingWallet, Wallet receivingWallet, BigDecimal amount) {
        long createdAt = System.currentTimeMillis();
        createTransactionEntry(sendingWallet.getId(), amount, true, DigitalWalletEnums.TransactionStatus.COMPLETED, createdAt);
        createTransactionEntry(receivingWallet.getId(), amount, false, DigitalWalletEnums.TransactionStatus.COMPLETED, createdAt);
    }

    private void createTransactionEntry(Long walletId, BigDecimal amount, boolean debit, DigitalWalletEnums.
            TransactionStatus status, long createdAt) {
        TransactionEntry transactionEntry = TransactionEntry.builder()
                .user(walletService.get(walletId).getUser())
                .amount(amount)
                .debit(debit)
                .transactionStatus(status)
                .createdAt(createdAt)
                .build();
        transactionEntryRepository.save(transactionEntry);
    }

    private boolean validate(Wallet sendingWallet, Wallet receivingWallet, BigDecimal amount) {
        // Return true when the transaction is NOT permitted (i.e., it would push the receiving wallet over the allowed limit)
        return receivingWallet.getBalance().add(amount).compareTo(MAXIMUM_AMOUNT_ALLOWED) <= 0 && sendingWallet.getBalance().compareTo(amount) >= 0;
    }
}

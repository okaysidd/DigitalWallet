package com.digitalwallet.digitalwallet.services;

import com.digitalwallet.digitalwallet.models.DigitalWalletEnums;
import com.digitalwallet.digitalwallet.models.TransactionEntry;
import com.digitalwallet.digitalwallet.models.TransferMoneyBetweenWalletsRequest;
import com.digitalwallet.digitalwallet.models.Wallet;
import com.digitalwallet.digitalwallet.repositories.ITransactionEntryRepository;
import com.digitalwallet.digitalwallet.repositories.IWalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
//@Service
public class TransactionService implements ITransactionService {

    private final static BigDecimal MAXIMUM_AMOUNT_ALLOWED = BigDecimal.valueOf(10000);

    private final IWalletService walletService;
    private final IWalletRepository walletRepository;
    private final ITransactionEntryRepository transactionEntryRepository;

    public TransactionService(IWalletService walletService, IWalletRepository walletRepository, ITransactionEntryRepository transactionEntryRepository) {
        this.walletService = walletService;
        this.walletRepository = walletRepository;
        this.transactionEntryRepository = transactionEntryRepository;
    }

    @Override
    public Boolean sendMoneyBetweenWallets(TransferMoneyBetweenWalletsRequest transferMoneyBetweenWalletsRequest) {
        Wallet sendingWallet = walletService.get(transferMoneyBetweenWalletsRequest.getSendingWalletId());
        Wallet receivingWallet = walletService.get(transferMoneyBetweenWalletsRequest.getReceivingWalletId());
        BigDecimal amount = transferMoneyBetweenWalletsRequest.getAmount();

        try {
            if (lockBothParties(sendingWallet, receivingWallet, amount)) {
                return initiateTransfer(sendingWallet, receivingWallet, amount);
            }
        } finally {
            if (sendingWallet.isLocked()) {
                sendingWallet.unlock();
                walletRepository.save(sendingWallet);
            }
            if (receivingWallet.isLocked()) {
                receivingWallet.unlock();
                walletRepository.save(receivingWallet);
            }
        }
        return false;
    }

    @Transactional
    private boolean lockBothParties(Wallet sendingWallet, Wallet receivingWallet, BigDecimal amount) {
        if (sendingWallet.getBalance().compareTo(amount) >= 0 && sendingWallet.lock()) {
            walletRepository.save(sendingWallet);
        }
        if (receivingWallet.getBalance().add(amount).compareTo(MAXIMUM_AMOUNT_ALLOWED) <= 0 && receivingWallet.lock()) {
            walletRepository.save(receivingWallet);
        }
        return sendingWallet.isLocked() && receivingWallet.isLocked();
    }

    @Transactional
    private Boolean initiateTransfer(Wallet sendingWallet, Wallet receivingWallet, BigDecimal amount) {
        if (sendingWallet.isLocked() && receivingWallet.isLocked()) {
            // remove from sender
            try {
                log.info("removeFromBalance called for {}", new ObjectMapper().writeValueAsString(sendingWallet));
            } catch (Exception ignored) {
            }
            sendingWallet.removeFromBalance(amount);
            sendingWallet.unlock();
            walletRepository.save(sendingWallet);

            // add to receiver
            receivingWallet.addToBalance(amount);
            receivingWallet.unlock();
            walletRepository.save(receivingWallet);
            createTransactionEntries(sendingWallet, receivingWallet, amount);
            return true;
        } else {
            log.error("Transaction failed");
            long createdAt = System.currentTimeMillis();
            createTransactionEntry(sendingWallet.getId(), amount, true, DigitalWalletEnums.TransactionStatus.FAILED, createdAt);
            createTransactionEntry(receivingWallet.getId(), amount, false, DigitalWalletEnums.TransactionStatus.FAILED, createdAt);
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
}

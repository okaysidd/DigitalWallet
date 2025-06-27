package com.digitalwallet.digitalwallet.services;

import com.digitalwallet.digitalwallet.models.TransferMoneyBetweenWalletsRequest;
import com.digitalwallet.digitalwallet.models.Wallet;
import com.digitalwallet.digitalwallet.repositories.ITransactionEntryRepository;
import com.digitalwallet.digitalwallet.repositories.IUserRepository;
import com.digitalwallet.digitalwallet.repositories.IWalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private IWalletService walletService;

    @Mock
    private IWalletRepository walletRepository;

    @Mock
    private ITransactionEntryRepository transactionEntryRepository;

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private TransactionServiceWithDBHandling transactionService;

    private Wallet wallet1;
    private Wallet wallet2;

    @BeforeEach
    void setup() {
        wallet1 = new Wallet();
        wallet1.setId(1L);
        wallet1.addToBalance(BigDecimal.valueOf(1000));

        wallet2 = new Wallet();
        wallet2.setId(2L);
        wallet2.addToBalance(BigDecimal.valueOf(1000));

        when(walletService.get(1L)).thenReturn(wallet1);
        when(walletService.get(2L)).thenReturn(wallet2);

        when(walletService.getWalletWithLock(1L)).thenReturn(wallet1);
        when(walletService.getWalletWithLock(2L)).thenReturn(wallet2);

//        // stub checkBalanceAndLock to lock the given wallet
//        lenient().doAnswer(invocation -> {
//            Wallet w = invocation.getArgument(0);
//            w.lock();
//            return null;
//        }).when(walletService).checkBalanceAndLock(org.mockito.ArgumentMatchers.any(Wallet.class), org.mockito.ArgumentMatchers.any(BigDecimal.class));
    }

    @Test
    public void shouldExchangeBalanceOnPaymentBetweenWallets() {
        TransferMoneyBetweenWalletsRequest transferMoneyBetweenWalletsRequest = new TransferMoneyBetweenWalletsRequest();
        transferMoneyBetweenWalletsRequest.setSendingWalletId(wallet1.getId());
        transferMoneyBetweenWalletsRequest.setReceivingWalletId(wallet2.getId());
        transferMoneyBetweenWalletsRequest.setAmount(BigDecimal.valueOf(500));

        transactionService.sendMoneyBetweenWallets(transferMoneyBetweenWalletsRequest);

        assertEquals(BigDecimal.valueOf(500), wallet1.getBalance());
        assertEquals(BigDecimal.valueOf(1500), wallet2.getBalance());
    }

    @Test
    public void shouldNotExchangeBalanceOnPaymentBetweenWalletsDueToMaxAmountReached() {

        TransferMoneyBetweenWalletsRequest transferMoneyBetweenWalletsRequest = new TransferMoneyBetweenWalletsRequest();
        transferMoneyBetweenWalletsRequest.setSendingWalletId(wallet1.getId());
        transferMoneyBetweenWalletsRequest.setReceivingWalletId(wallet2.getId());
        transferMoneyBetweenWalletsRequest.setAmount(BigDecimal.valueOf(500000));

        Boolean success = transactionService.sendMoneyBetweenWallets(transferMoneyBetweenWalletsRequest);

        // The transaction should be rejected due to maximum amount validation
        assertEquals(false, success);
        // Balances should remain unchanged
        assertEquals(BigDecimal.valueOf(1000), wallet1.getBalance());
        assertEquals(BigDecimal.valueOf(1000), wallet2.getBalance());
    }

    @Test
    public void shouldNotExchangeBalanceOnPaymentBetweenWalletsDueToNotEnoughBalance() {

        TransferMoneyBetweenWalletsRequest transferMoneyBetweenWalletsRequest = new TransferMoneyBetweenWalletsRequest();
        transferMoneyBetweenWalletsRequest.setSendingWalletId(wallet1.getId());
        transferMoneyBetweenWalletsRequest.setReceivingWalletId(wallet2.getId());
        transferMoneyBetweenWalletsRequest.setAmount(BigDecimal.valueOf(1500));

        Boolean success = transactionService.sendMoneyBetweenWallets(transferMoneyBetweenWalletsRequest);

        // The transaction should be rejected due to maximum amount validation
        assertEquals(false, success);
        // Balances should remain unchanged
        assertEquals(BigDecimal.valueOf(1000), wallet1.getBalance());
        assertEquals(BigDecimal.valueOf(1000), wallet2.getBalance());
    }

    @Test
    public void shouldExchangeBalanceOnPaymentBetweenWalletsConcurrent() throws ExecutionException, InterruptedException {
        CompletableFuture<Boolean> future1 = CompletableFuture.supplyAsync(() -> {
            TransferMoneyBetweenWalletsRequest transferMoneyBetweenWalletsRequest1 = new TransferMoneyBetweenWalletsRequest();
            transferMoneyBetweenWalletsRequest1.setSendingWalletId(wallet1.getId());
            transferMoneyBetweenWalletsRequest1.setReceivingWalletId(wallet2.getId());
            transferMoneyBetweenWalletsRequest1.setAmount(BigDecimal.valueOf(1000));
            return transactionService.sendMoneyBetweenWallets(transferMoneyBetweenWalletsRequest1);
        });
        CompletableFuture<Boolean> future2 = CompletableFuture.supplyAsync(() -> {
            TransferMoneyBetweenWalletsRequest transferMoneyBetweenWalletsRequest2 = new TransferMoneyBetweenWalletsRequest();
            transferMoneyBetweenWalletsRequest2.setSendingWalletId(wallet1.getId());
            transferMoneyBetweenWalletsRequest2.setReceivingWalletId(wallet2.getId());
            transferMoneyBetweenWalletsRequest2.setAmount(BigDecimal.valueOf(1000));
            return transactionService.sendMoneyBetweenWallets(transferMoneyBetweenWalletsRequest2);
        });
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(future1, future2);
        combinedFuture.join();

        Boolean status1 = future1.get();
        Boolean status2 = future2.get();

        assertEquals(Boolean.FALSE, status1);
        assertEquals(Boolean.TRUE, status2);

        // Balances should remain unchanged
        assertEquals(BigDecimal.valueOf(0), wallet1.getBalance());
        assertEquals(BigDecimal.valueOf(2000), wallet2.getBalance());
    }
}


























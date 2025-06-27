package com.digitalwallet.digitalwallet.controllers;

import com.digitalwallet.digitalwallet.models.DigitalWalletResponse;
import com.digitalwallet.digitalwallet.models.ErrorDTO;
import com.digitalwallet.digitalwallet.models.TransferMoneyBetweenWalletsRequest;
import com.digitalwallet.digitalwallet.services.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/v1/transactions")
@Slf4j
public class TransactionController {

    private final ITransactionService transactionServiceWithDBHandling;

    public TransactionController(ITransactionService transactionService) {
        this.transactionServiceWithDBHandling = transactionService;
    }

    @PostMapping("/transfer")
    public DigitalWalletResponse<Boolean> transfer(@RequestBody TransferMoneyBetweenWalletsRequest request) {
        try {
            Boolean success = transactionServiceWithDBHandling.sendMoneyBetweenWallets(request);
            if (Boolean.TRUE.equals(success)) {
                return DigitalWalletResponse.<Boolean>builder()
                        .response(true)
                        .build();
            } else {
                return DigitalWalletResponse.<Boolean>builder()
                        .status(false)
                        .response(false)
                        .errorDTO(new ErrorDTO("Transfer failed"))
                        .build();
            }
        } catch (Exception e) {
            log.error("Error while transferring money", e);
            return DigitalWalletResponse.<Boolean>builder()
                    .status(false)
                    .response(false)
                    .errorDTO(new ErrorDTO(e.getMessage()))
                    .build();
        }
    }

    @PostMapping("/transferConcurrent")
    public DigitalWalletResponse<Boolean> transferConcurrent() {
        try {
            CompletableFuture<Boolean> future1 = CompletableFuture.supplyAsync(() -> {
                TransferMoneyBetweenWalletsRequest transferMoneyBetweenWalletsRequest1 = new TransferMoneyBetweenWalletsRequest();
                transferMoneyBetweenWalletsRequest1.setSendingWalletId((long) 1);
                transferMoneyBetweenWalletsRequest1.setReceivingWalletId((long) 2);
                transferMoneyBetweenWalletsRequest1.setAmount(BigDecimal.valueOf(1000));
                return transactionServiceWithDBHandling.sendMoneyBetweenWallets(transferMoneyBetweenWalletsRequest1);
            });
            CompletableFuture<Boolean> future2 = CompletableFuture.supplyAsync(() -> {
                TransferMoneyBetweenWalletsRequest transferMoneyBetweenWalletsRequest2 = new TransferMoneyBetweenWalletsRequest();
                transferMoneyBetweenWalletsRequest2.setSendingWalletId((long) 2);
                transferMoneyBetweenWalletsRequest2.setReceivingWalletId((long) 1);
                transferMoneyBetweenWalletsRequest2.setAmount(BigDecimal.valueOf(1000));
                return transactionServiceWithDBHandling.sendMoneyBetweenWallets(transferMoneyBetweenWalletsRequest2);
            });
            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(future1, future2);
            combinedFuture.join();

            Boolean status1 = future1.get();
            Boolean status2 = future2.get();

            if (Boolean.TRUE.equals(status1) && Boolean.TRUE.equals(status2)) {
                return DigitalWalletResponse.<Boolean>builder()
                        .response(true)
                        .build();
            } else {
                return DigitalWalletResponse.<Boolean>builder()
                        .status(false)
                        .response(false)
                        .errorDTO(new ErrorDTO("Transfer failed"))
                        .build();
            }
        } catch (Exception e) {
            log.error("Error while transferring money", e);
            return DigitalWalletResponse.<Boolean>builder()
                    .status(false)
                    .response(false)
                    .errorDTO(new ErrorDTO(e.getMessage()))
                    .build();
        }
    }
}

package com.digitalwallet.digitalwallet.controllers;

import com.digitalwallet.digitalwallet.models.DigitalWalletResponse;
import com.digitalwallet.digitalwallet.models.ErrorDTO;
import com.digitalwallet.digitalwallet.models.Wallet;
import com.digitalwallet.digitalwallet.services.IWalletService;
import com.digitalwallet.digitalwallet.exceptions.InvalidUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/digitalwallet")
@Slf4j
public class DigitalWalletController {

    private final IWalletService walletService;

    public DigitalWalletController(IWalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/create")
    public DigitalWalletResponse<Wallet> createWallet(@RequestParam("userId") Long userId,
                                                     @RequestParam(value = "balance", required = false) BigDecimal balance) {
        try {
            Wallet wallet = (balance == null) ? walletService.addWallet(userId) : walletService.addWallet(userId, balance);
            return DigitalWalletResponse.<Wallet>builder()
                    .response(wallet)
                    .build();
        } catch (InvalidUserException e) {
            return DigitalWalletResponse.<Wallet>builder()
                    .status(false)
                    .errorDTO(new ErrorDTO(e.getMessage()))
                    .build();
        }
    }

    @PostMapping("/get")
    public DigitalWalletResponse<Wallet> get(@RequestParam("id") Long id) {
        Wallet wallet = walletService.get(id);
        if (wallet == null) {
            return DigitalWalletResponse.<Wallet>builder()
                    .status(false)
                    .errorDTO(new ErrorDTO("Wallet not found"))
                    .build();
        }
        return DigitalWalletResponse.<Wallet>builder()
                .response(wallet)
                .build();
    }
}

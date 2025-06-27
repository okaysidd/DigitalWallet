package com.digitalwallet.digitalwallet.services;

import com.digitalwallet.digitalwallet.exceptions.InvalidUserException;
import com.digitalwallet.digitalwallet.models.Wallet;

import java.math.BigDecimal;
import java.util.Optional;

public interface IWalletService {
    Wallet addWallet(Long userId, BigDecimal balance) throws InvalidUserException;

    Wallet addWallet(Long userId) throws InvalidUserException;

    Wallet get(Long walletId);

//    boolean checkBalanceAndLock(Wallet wallet, BigDecimal amount);

    Wallet getWalletWithLock(Long id);
}

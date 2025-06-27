package com.digitalwallet.digitalwallet.services;

import com.digitalwallet.digitalwallet.exceptions.InvalidUserException;
import com.digitalwallet.digitalwallet.models.User;
import com.digitalwallet.digitalwallet.models.Wallet;
import com.digitalwallet.digitalwallet.repositories.IWalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class WalletServiceImpl implements IWalletService {
    private final IWalletRepository walletRepository;
    private final IUserService userService;

    public WalletServiceImpl(IWalletRepository walletRepository, IUserService userService) {
        this.walletRepository = walletRepository;
        this.userService = userService;
    }

    @Override
    public Wallet addWallet(Long userId, BigDecimal balance) throws InvalidUserException {
        // todo: validate no prev wallet for user
        Wallet wallet = new Wallet();
        User user;
        try {
            user = userService.get(userId);
            if (user == null) {
                log.error("Invalid user from user service");
                throw new InvalidUserException("Invalid");
            }
        } catch (Exception ex) {
            log.error("Could not get user");
            throw ex;
        }
        wallet.setUser(user);
        wallet.addToBalance(balance);
        walletRepository.save(wallet);
        return wallet;
    }

    @Override
    public Wallet addWallet(Long userId) throws InvalidUserException {
        return addWallet(userId, BigDecimal.valueOf(0));
    }

    @Override
    public Wallet get(Long walletId) {
        // todo: add validations and null check
        if (walletId == null) {
            throw new IllegalArgumentException("Wallet ID cannot be null");
        }
        return walletRepository.findById(walletId).orElse(null);
    }

    @Override
    public Wallet getWalletWithLock(Long id) {
        return walletRepository
                .findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("No wallet found with id"));
    }

//    @Override
//    public boolean checkBalanceAndLock(Wallet wallet, BigDecimal amount) {
//        log.info("checkBalanceAndLock for {}", wallet.getId());
//        if (wallet.getBalance().compareTo(amount) >= 0 && wallet.lock()) {
//            walletRepository.save(wallet);
//            return true;
//        }
//        return false;
//    }
}

//sanjay.prajapat@slicebank.com

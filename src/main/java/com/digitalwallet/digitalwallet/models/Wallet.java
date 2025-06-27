package com.digitalwallet.digitalwallet.models;

import com.digitalwallet.digitalwallet.exceptions.InsufficientBalanceException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Duration;

@Slf4j
@Entity
@NoArgsConstructor
public class Wallet {
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    private BigDecimal balance = BigDecimal.valueOf(0);

    private Long lockExpiryTime;

    @Getter
    private boolean isLocked;

    public boolean lock() {
        log.info("lock for {}", id);
        if (isLocked) {
            log.info("returning false for {}", id);
            return false;
        }
        lockExpiryTime = System.currentTimeMillis() + Duration.ofMinutes(2).getSeconds() * 1000;
        isLocked = true;
        log.info("returning false2 for {}", id);
        return true;
    }

    public void unlock() {
        lockExpiryTime = null;
        isLocked = false;
    }

    @Setter
    @Getter
    @OneToOne
    private User user;

    public void addToBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void removeFromBalance(BigDecimal amount) {
        log.info("removeFromBalance for {}", amount);
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Not enough balance");
        }
        this.balance = this.balance.subtract(amount);
    }
}

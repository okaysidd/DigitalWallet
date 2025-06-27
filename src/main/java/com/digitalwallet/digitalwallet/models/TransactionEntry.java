package com.digitalwallet.digitalwallet.models;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // milliseconds id, so concurrency upto milliseconds is supported

    @ManyToOne
    private User user;

    private BigDecimal amount;

    private Boolean debit;

    private DigitalWalletEnums.TransactionStatus transactionStatus;

    private Long createdAt;
}

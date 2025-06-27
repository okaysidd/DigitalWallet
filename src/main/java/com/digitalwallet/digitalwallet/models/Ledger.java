package com.digitalwallet.digitalwallet.models;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

public class Ledger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // milliseconds id, so concurrency upto milliseconds is supported

    @ManyToOne
    private User user;

    private BigDecimal amount;

    private Boolean debit;

    private Long createdAt;
}

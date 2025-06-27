package com.digitalwallet.digitalwallet.exceptions;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String e) {
        super(e);
    }
}

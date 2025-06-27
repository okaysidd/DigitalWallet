package com.digitalwallet.digitalwallet.models;

public class DigitalWalletEnums {
    public enum TransactionStatus {
        PENDING((short) 0), COMPLETED((short) 1), FAILED((short) 2);

        private final short code;

        TransactionStatus(short code) {
            this.code = code;
        }

        public short getCode() {
            return code;
        }
    }
}

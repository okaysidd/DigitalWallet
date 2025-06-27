package com.digitalwallet.digitalwallet.models;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferMoneyBetweenWalletsRequest {
    private Long sendingWalletId;
    private Long receivingWalletId;
    private BigDecimal amount;
}

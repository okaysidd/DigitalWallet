package com.digitalwallet.digitalwallet.services;

import com.digitalwallet.digitalwallet.models.TransferMoneyBetweenWalletsRequest;

public interface ITransactionService {
    Boolean sendMoneyBetweenWallets(TransferMoneyBetweenWalletsRequest transferMoneyBetweenWalletsRequest);
}

/*

POST /v1/digitalWallet/sendMoneyBetweenWallets
{
    "sendingWalletId": 21,
    "receivingWalletId": 21,
    "amount": 1000
}


 */

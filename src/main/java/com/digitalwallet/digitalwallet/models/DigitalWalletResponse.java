package com.digitalwallet.digitalwallet.models;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DigitalWalletResponse<T> {
    private final T response;
    private final ErrorDTO errorDTO;
    @Builder.Default
    private final boolean status = true;
}

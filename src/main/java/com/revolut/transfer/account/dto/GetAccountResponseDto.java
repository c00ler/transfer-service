package com.revolut.transfer.account.dto;

import com.revolut.transfer.account.model.Account;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public final class GetAccountResponseDto {

    private UUID id;

    private Long balance;

    public static GetAccountResponseDto of(final Account account) {
        return new GetAccountResponseDto().setId(account.getId()).setBalance(account.getBalance());
    }
}

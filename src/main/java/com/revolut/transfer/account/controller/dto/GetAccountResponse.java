package com.revolut.transfer.account.controller.dto;

import com.revolut.transfer.account.model.Account;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public final class GetAccountResponse {

    private UUID id;

    private Long balance;

    public static GetAccountResponse of(final Account account) {
        return new GetAccountResponse().setId(account.getId()).setBalance(account.getBalance());
    }
}

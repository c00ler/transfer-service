package com.revolut.transfer.account.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public final class GetAccountResponseDto {

    private UUID id;

    private Long balance;
}

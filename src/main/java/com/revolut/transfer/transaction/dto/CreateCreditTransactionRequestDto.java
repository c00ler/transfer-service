package com.revolut.transfer.transaction.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class CreateCreditTransactionRequestDto {

    private UUID id;

    private Long amount;
}

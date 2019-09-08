package com.revolut.transfer.transaction.controller.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class CreateCreditTransactionRequest {

    private UUID id;

    private Long amount;
}

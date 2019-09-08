package com.revolut.transfer.transaction.controller.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class CreateTransferRequest {

    private UUID id;

    private UUID sourceAccountId;

    private UUID targetAccountId;

    private Long amount;
}

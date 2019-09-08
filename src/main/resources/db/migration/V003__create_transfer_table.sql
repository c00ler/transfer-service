-- This table contains the state of the transfer. This state contains all the necessary information
-- needed to recover transactions in case of any failures during the transfer.
CREATE TABLE transfer
(
    id                    UUID                                                             NOT NULL,
    source_account_id     UUID                                                             NOT NULL,
    target_account_id     UUID                                                             NOT NULL,
    debit_transaction_id  UUID                                                             NOT NULL,
    credit_transaction_id UUID                                                             NOT NULL,
    amount                BIGINT                                                           NOT NULL,
    state                 ENUM('NEW', 'SOURCE_CHARGED', 'INSUFFICIENT_FUNDS', 'COMPLETED') NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_transfer_source_account_id FOREIGN KEY (source_account_id) REFERENCES account (id),
    CONSTRAINT fk_transfer_target_account_id FOREIGN KEY (target_account_id) REFERENCES account (id),
    CONSTRAINT check_different_accounts CHECK (source_account_id <> target_account_id),
    CONSTRAINT check_positive_amount CHECK (amount > 0)
);

CREATE UNIQUE INDEX key_transfer_debit_transaction_id ON transfer (debit_transaction_id);
CREATE UNIQUE INDEX key_transfer_credit_transaction_id ON transfer (credit_transaction_id);

CREATE TABLE transaction
(
    id         UUID   NOT NULL,
    account_id UUID   NOT NULL,
    amount     BIGINT NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_transaction_account_id FOREIGN KEY (account_id) REFERENCES account (id)
);

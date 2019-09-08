-- This table suppose to contain general account information, such as account number, account status, etc.
-- Fur the purpose of the task only account id is needed, because it is used for locking, the other fields
-- are omitted.
CREATE TABLE account
(
    id UUID NOT NULL,

    PRIMARY KEY (id)
);

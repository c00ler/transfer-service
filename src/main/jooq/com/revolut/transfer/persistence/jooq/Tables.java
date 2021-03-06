/*
 * This file is generated by jOOQ.
 */
package com.revolut.transfer.persistence.jooq;


import com.revolut.transfer.persistence.jooq.tables.Account;
import com.revolut.transfer.persistence.jooq.tables.Transaction;
import com.revolut.transfer.persistence.jooq.tables.Transfer;

import javax.annotation.processing.Generated;


/**
 * Convenience access to all tables in PUBLIC
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>PUBLIC.ACCOUNT</code>.
     */
    public static final Account ACCOUNT = Account.ACCOUNT;

    /**
     * The table <code>PUBLIC.TRANSACTION</code>.
     */
    public static final Transaction TRANSACTION = Transaction.TRANSACTION;

    /**
     * The table <code>PUBLIC.TRANSFER</code>.
     */
    public static final Transfer TRANSFER = Transfer.TRANSFER;
}

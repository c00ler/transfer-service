package com.revolut.transfer.exception;

public final class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(final String message) {
        super(message);
    }
}

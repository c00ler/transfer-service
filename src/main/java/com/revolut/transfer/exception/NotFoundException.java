package com.revolut.transfer.exception;

public final class NotFoundException extends RuntimeException {

    public NotFoundException(final String message) {
        super(message);
    }
}

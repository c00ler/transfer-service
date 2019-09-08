package com.revolut.transfer.util;

import org.jetbrains.annotations.Nullable;

public final class NumberUtils {

    private NumberUtils() {
    }

    /**
     * Null-safe check if value is greater than the threshold.
     *
     * @param value     evaluated value
     * @param threshold threshold value
     * @return {@code true} if {@code value} is not {@code null} and is greater than the {@code threshold}
     */
    public static boolean isGreaterThan(@Nullable final Long value, final long threshold) {
        return value != null && value > threshold;
    }
}

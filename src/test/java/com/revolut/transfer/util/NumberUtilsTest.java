package com.revolut.transfer.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NumberUtilsTest {

    @Test
    void shouldReturnFalseIfValueIsNull() {
        assertThat(NumberUtils.isGreaterThan(null, 10L)).isFalse();
    }

    @Test
    void shouldReturnFalseIfValueLessThanThreshold() {
        assertThat(NumberUtils.isGreaterThan(5L, 10L)).isFalse();
    }

    @Test
    void shouldReturnFalseIfValueEqualToThreshold() {
        assertThat(NumberUtils.isGreaterThan(10L, 10L)).isFalse();
    }

    @Test
    void shouldReturnTrueIfValueGreaterThanThreshold() {
        assertThat(NumberUtils.isGreaterThan(20L, 10L)).isTrue();
    }
}

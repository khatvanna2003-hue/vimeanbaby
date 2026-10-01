package com.vimeanbaby.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PhoneNumbersTest {

    @ParameterizedTest
    @ValueSource(strings = {"012345678", "+855 12 345 678", "85512345678", "(012) 345-678", "12345678"})
    void normalizesToLocalForm(String raw) {
        assertThat(PhoneNumbers.normalize(raw)).isEqualTo("012345678");
    }

    @ParameterizedTest
    @ValueSource(strings = {"+855 97 123 4567", "0971234567"})
    void keepsTenDigitNumbers(String raw) {
        assertThat(PhoneNumbers.normalize(raw)).isEqualTo("0971234567");
    }
}

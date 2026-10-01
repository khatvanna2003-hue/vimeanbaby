package com.vimeanbaby.user.service;

/**
 * Normalises Cambodian phone numbers to the local form {@code 0XXXXXXXX} so that
 * "+855 12 345 678", "85512345678" and "012345678" are treated as the same account.
 */
public final class PhoneNumbers {

    private static final String COUNTRY_CODE = "855";

    private PhoneNumbers() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.startsWith(COUNTRY_CODE) && digits.length() > COUNTRY_CODE.length() + 7) {
            digits = digits.substring(COUNTRY_CODE.length());
        }
        if (!digits.startsWith("0")) {
            digits = "0" + digits;
        }
        return digits;
    }
}

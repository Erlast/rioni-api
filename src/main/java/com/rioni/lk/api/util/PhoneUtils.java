package com.rioni.lk.api.util;

/**
 * Utility class for phone number normalization.
 * <p>
 * Strips all non-digit characters from a phone number while preserving
 * the leading '+' sign if present.
 * </p>
 * <p>
 * Examples:
 * <ul>
 *   <li>{@code +54 (545) 543-45-54} → {@code +545455434554}</li>
 *   <li>{@code +7 (906) 345-34-34} → {@code +79063453434}</li>
 *   <li>{@code 8 (906) 345-34-34} → {@code 89063453434}</li>
 * </ul>
 * </p>
 */
public final class PhoneUtils {

    private PhoneUtils() {
        // utility class
    }

    /**
     * Normalizes a phone number by removing all non-digit characters,
     * preserving the leading '+' if present.
     *
     * @param phone the raw phone number, may be {@code null}
     * @return the normalized phone number, or {@code null} if input was {@code null}
     */
    public static String normalize(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }

        String trimmed = phone.trim();
        boolean hasPlus = trimmed.startsWith("+");

        String digitsOnly = trimmed.replaceAll("[^\\d]", "");

        if (hasPlus) {
            return "+" + digitsOnly;
        }
        return digitsOnly;
    }
}

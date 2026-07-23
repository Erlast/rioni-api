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

    /**
     * Masks a phone number for display, showing only the first 3 characters
     * (including the leading '+') and the last 2 digits, replacing the middle
     * portion with asterisks.
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code +79123456734} → {@code +79*******34}</li>
     *   <li>{@code +375291234567} → {@code +37*******67}</li>
     * </ul>
     *
     * @param phone the normalized phone number, may be {@code null}
     * @return the masked phone number, or {@code null} if input was {@code null}
     */
    public static String mask(String phone) {
        if (phone == null || phone.length() < 6) {
            return phone;
        }

        return phone.substring(0, 3) + "*".repeat(phone.length() - 5) + phone.substring(phone.length() - 2);
    }
}

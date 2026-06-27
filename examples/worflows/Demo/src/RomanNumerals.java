import java.util.Locale;

/// Converts between integers and Roman numerals.
///
/// Only the classic range 1..3999 is supported, which is the range
/// expressible with the standard symbols I, V, X, L, C, D and M without
/// needing the medieval "bar" (vinculum) notation for thousands.
public final class RomanNumerals {

    /// Symbol table ordered from largest to smallest value, including the
    /// six subtractive pairs (IV, IX, XL, XC, CD, CM). Encoding greedily
    /// walks this table, so the produced numeral is always canonical.
    private static final int[] VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    public static final int MIN_VALUE = 1;
    public static final int MAX_VALUE = 3999;

    private RomanNumerals() {
        // Utility class — not instantiable.
    }

    /// Encodes {@code value} as a canonical Roman numeral.
    ///
    /// @throws IllegalArgumentException if {@code value} is outside 1..3999
    public static String toRoman(int value) {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new IllegalArgumentException(
                    "value out of range [" + MIN_VALUE + ".." + MAX_VALUE + "]: " + value);
        }
        StringBuilder out = new StringBuilder();
        int remaining = value;
        for (int i = 0; i < VALUES.length; i++) {
            while (remaining >= VALUES[i]) {
                out.append(SYMBOLS[i]);
                remaining -= VALUES[i];
            }
        }
        return out.toString();
    }

    /// Decodes a Roman numeral string to its integer value.
    ///
    /// Input is case-insensitive and surrounding whitespace is ignored, but
    /// the numeral must be *canonical*: malformed strings such as "IIII",
    /// "VV" or "IC" are rejected even though a naive parser could assign them
    /// a value. This is enforced by re-encoding the parsed value and
    /// requiring it to match the (normalized) input.
    ///
    /// @throws IllegalArgumentException if the input is null, blank, contains
    ///         an unknown symbol, or is not a canonical numeral in 1..3999
    public static int fromRoman(String roman) {
        if (roman == null || roman.isBlank()) {
            throw new IllegalArgumentException("Roman numeral must not be null or blank");
        }
        String normalized = roman.strip().toUpperCase(Locale.ROOT);

        int total = 0;
        int prev = 0;
        for (int i = normalized.length() - 1; i >= 0; i--) {
            int current = symbolValue(normalized.charAt(i));
            // A smaller symbol to the left of a larger one is subtractive (e.g. IX).
            if (current < prev) {
                total -= current;
            } else {
                total += current;
                prev = current;
            }
        }

        if (total < MIN_VALUE || total > MAX_VALUE || !toRoman(total).equals(normalized)) {
            throw new IllegalArgumentException("Not a canonical Roman numeral: " + roman);
        }
        return total;
    }

    private static int symbolValue(char symbol) {
        return switch (symbol) {
            case 'I' -> 1;
            case 'V' -> 5;
            case 'X' -> 10;
            case 'L' -> 50;
            case 'C' -> 100;
            case 'D' -> 500;
            case 'M' -> 1000;
            default -> throw new IllegalArgumentException("Unknown Roman symbol: '" + symbol + "'");
        };
    }
}

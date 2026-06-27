/// Demo entry point. Run this class to see {@link RomanNumerals} in action.
/// Uses JDK 25+ compact source / instance-main style with the built-in `IO`.
void main() {
    IO.println("Roman numeral demo");
    IO.println("------------------");

    int[] samples = {4, 42, 1994, 2026, RomanNumerals.MAX_VALUE};
    for (int value : samples) {
        String roman = RomanNumerals.toRoman(value);
        int back = RomanNumerals.fromRoman(roman);
        IO.println(String.format("%4d -> %-10s -> %d", value, roman, back));
    }

    IO.println("");
    IO.println("Parsing is case-insensitive: 'mmxxvi' = " + RomanNumerals.fromRoman("mmxxvi"));

    try {
        RomanNumerals.fromRoman("IIII");
    } catch (IllegalArgumentException e) {
        IO.println("Rejected 'IIII' as expected: " + e.getMessage());
    }
}

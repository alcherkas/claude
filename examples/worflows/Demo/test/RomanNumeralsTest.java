/// Tests for {@link RomanNumerals}, run by {@link TestRunner}.
///
/// Everything lives in the default package, so the {@link Assert} helpers are
/// referenced directly (Java forbids `import`ing the unnamed package).
public class RomanNumeralsTest {

    @Test
    void encodesSmallNumbers() {
        Assert.assertEquals("I", RomanNumerals.toRoman(1));
        Assert.assertEquals("IV", RomanNumerals.toRoman(4));
        Assert.assertEquals("IX", RomanNumerals.toRoman(9));
        Assert.assertEquals("XL", RomanNumerals.toRoman(40));
        Assert.assertEquals("XC", RomanNumerals.toRoman(90));
    }

    @Test
    void encodesCompositeNumbers() {
        Assert.assertEquals("XIV", RomanNumerals.toRoman(14));
        Assert.assertEquals("XLII", RomanNumerals.toRoman(42));
        Assert.assertEquals("MCMXCIV", RomanNumerals.toRoman(1994));
        Assert.assertEquals("MMXXVI", RomanNumerals.toRoman(2026));
    }

    @Test
    void encodesBoundaryValues() {
        Assert.assertEquals("I", RomanNumerals.toRoman(RomanNumerals.MIN_VALUE));
        Assert.assertEquals("MMMCMXCIX", RomanNumerals.toRoman(RomanNumerals.MAX_VALUE));
    }

    @Test
    void rejectsOutOfRangeValues() {
        Assert.assertThrows(IllegalArgumentException.class, () -> RomanNumerals.toRoman(0));
        Assert.assertThrows(IllegalArgumentException.class, () -> RomanNumerals.toRoman(-1));
        Assert.assertThrows(IllegalArgumentException.class, () -> RomanNumerals.toRoman(4000));
    }

    @Test
    void decodesValidNumerals() {
        Assert.assertEquals(4, RomanNumerals.fromRoman("IV"));
        Assert.assertEquals(42, RomanNumerals.fromRoman("XLII"));
        Assert.assertEquals(1994, RomanNumerals.fromRoman("MCMXCIV"));
        Assert.assertEquals(3999, RomanNumerals.fromRoman("MMMCMXCIX"));
    }

    @Test
    void decodingIsCaseInsensitiveAndTrimmed() {
        Assert.assertEquals(2026, RomanNumerals.fromRoman("  mmxxvi  "));
        Assert.assertEquals(14, RomanNumerals.fromRoman("xIv"));
    }

    @Test
    void roundTripsEveryValueInRange() {
        for (int n = RomanNumerals.MIN_VALUE; n <= RomanNumerals.MAX_VALUE; n++) {
            String roman = RomanNumerals.toRoman(n);
            Assert.assertEquals(n, RomanNumerals.fromRoman(roman));
        }
    }

    @Test
    void rejectsNonCanonicalNumerals() {
        // Values that a naive parser would accept, but which are not how the
        // numbers are actually written.
        Assert.assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman("IIII")); // should be IV
        Assert.assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman("VV"));   // should be X
        Assert.assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman("IC"));   // should be XCIX
        Assert.assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman("MMMM")); // out of range
    }

    @Test
    void rejectsMalformedInput() {
        Assert.assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman(null));
        Assert.assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman(""));
        Assert.assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman("   "));
        Assert.assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman("XIV?"));
        Assert.assertThrows(IllegalArgumentException.class, () -> RomanNumerals.fromRoman("hello"));
    }
}

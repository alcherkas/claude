import java.util.Objects;
import java.util.function.Supplier;

/// Minimal assertion helpers used by the demo test suite.
///
/// Each failed assertion throws {@link AssertionError}, which the
/// {@link TestRunner} catches and reports as a failing test.
public final class Assert {

    private Assert() {
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    public static void assertEquals(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }

    public static void assertEquals(long expected, long actual) {
        if (expected != actual) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }

    /// Asserts that {@code action} throws an exception of (a subtype of)
    /// {@code expectedType}, and returns the caught exception so callers can
    /// make further assertions about it.
    public static <T extends Throwable> T assertThrows(Class<T> expectedType, Runnable action) {
        try {
            action.run();
        } catch (Throwable thrown) {
            if (expectedType.isInstance(thrown)) {
                return expectedType.cast(thrown);
            }
            throw new AssertionError(
                    "expected " + expectedType.getSimpleName()
                            + " but " + thrown.getClass().getSimpleName() + " was thrown", thrown);
        }
        throw new AssertionError(
                "expected " + expectedType.getSimpleName() + " but nothing was thrown");
    }

    public static void fail(Supplier<String> message) {
        throw new AssertionError(message.get());
    }
}

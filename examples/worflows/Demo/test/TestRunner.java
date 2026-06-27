import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

/// A dependency-free test runner.
///
/// For each supplied class it instantiates the class (via its no-arg
/// constructor) and invokes every {@code @Test}-annotated instance method,
/// reporting a pass/fail line per test and a summary at the end. The process
/// exits with a non-zero status if any test fails, so it behaves correctly in
/// CI and from the IntelliJ "Run" gutter alike.
public final class TestRunner {

    private TestRunner() {
    }

    public static void main(String[] args) {
        run(RomanNumeralsTest.class);
    }

    public static void run(Class<?>... testClasses) {
        int passed = 0;
        int failed = 0;

        for (Class<?> testClass : testClasses) {
            System.out.println("== " + testClass.getSimpleName() + " ==");

            Method[] tests = Arrays.stream(testClass.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(Test.class))
                    .sorted(Comparator.comparing(Method::getName))
                    .toArray(Method[]::new);

            for (Method test : tests) {
                try {
                    Object instance = testClass.getDeclaredConstructor().newInstance();
                    test.setAccessible(true);
                    test.invoke(instance);
                    System.out.println("  [PASS] " + test.getName());
                    passed++;
                } catch (InvocationTargetException e) {
                    failed++;
                    reportFailure(test, e.getCause());
                } catch (ReflectiveOperationException e) {
                    failed++;
                    reportFailure(test, e);
                }
            }
        }

        System.out.println();
        System.out.printf("Tests run: %d, Passed: %d, Failed: %d%n", passed + failed, passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void reportFailure(Method test, Throwable cause) {
        String reason = cause == null ? "unknown error" : cause.toString();
        System.out.println("  [FAIL] " + test.getName() + " -> " + reason);
    }
}

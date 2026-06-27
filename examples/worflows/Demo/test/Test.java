import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Marks a no-argument instance method as a test case.
///
/// Deliberately mirrors JUnit's {@code @Test} so this demo can be migrated to
/// JUnit later by swapping imports — but it carries zero dependencies, so the
/// suite runs with nothing but the JDK.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
}

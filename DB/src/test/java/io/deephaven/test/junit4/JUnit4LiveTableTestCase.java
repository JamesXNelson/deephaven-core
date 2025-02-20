package io.deephaven.test.junit4;

import io.deephaven.db.v2.LiveTableTestCase;

/**
 * When you want to extend LiveTableTestCase, but you need to use JUnit 4 annotations, like @Category
 * or @RunWith(Suite.class), then instead of extending LiveTableTestCase, you should instead create a
 * `JUnit4LiveTableTestCase field;`, and call setUp/tearDown in @Before/@After annotated methods.
 *
 * We could probably implement this as a TestRule instead, but this works fine as-is.
 */
public class JUnit4LiveTableTestCase extends LiveTableTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    // We use this class as a field in JUnit 4 tests which should not extend TestCase. This method is a no-op test
    // method so when we are detected as a JUnit3 test, we do not fail
    public void testMethodSoThisIsValidJUnit3() {}

    public static boolean printTableUpdates() {
        return LiveTableTestCase.printTableUpdates;
    }
}

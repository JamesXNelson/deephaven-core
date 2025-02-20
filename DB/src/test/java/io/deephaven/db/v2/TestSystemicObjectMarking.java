package io.deephaven.db.v2;

import io.deephaven.db.tables.Table;
import io.deephaven.db.tables.live.LiveTableMonitor;
import io.deephaven.db.tables.utils.SystemicObjectTracker;
import io.deephaven.db.tables.utils.TableTools;
import io.deephaven.db.v2.select.FormulaEvaluationException;
import junit.framework.TestCase;

import java.util.List;

import static io.deephaven.db.v2.TstUtils.c;
import static io.deephaven.db.v2.TstUtils.i;

public class TestSystemicObjectMarking extends LiveTableTestCase {
    public void testSystemicObjectMarking() {
        final QueryTable source = TstUtils.testRefreshingTable(c("Str", "a", "b"), c("Str2", "A", "B"));
        final Table updated = LiveTableMonitor.DEFAULT.sharedLock().computeLocked(() -> source.update("UC=Str.toUpperCase()"));
        final Table updated2 = SystemicObjectTracker.executeSystemically(false, () -> LiveTableMonitor.DEFAULT.sharedLock().computeLocked(() -> source.update("LC=Str2.toLowerCase()")));

        TableTools.showWithIndex(updated);

        LiveTableMonitor.DEFAULT.runWithinUnitTestCycle(() -> {
            TstUtils.addToTable(source, i(2, 3), c("Str", "c", "d"), c("Str2", "C", "D"));
            source.notifyListeners(i(2, 3), i(), i());
        });

        assertFalse(((DynamicTable)updated).isFailed());
        assertFalse(((DynamicTable)updated2).isFailed());

        final ErrorListener errorListener2 = new ErrorListener((QueryTable)updated2);
        ((QueryTable) updated2).listenForUpdates(errorListener2);

        LiveTableMonitor.DEFAULT.runWithinUnitTestCycle(() -> {
            TstUtils.addToTable(source, i(4, 5), c("Str", "e", "f"), c("Str2", "E", null));
            source.notifyListeners(i(4, 5), i(), i());
        });

        assertFalse(((DynamicTable)updated).isFailed());
        assertTrue(((DynamicTable)updated2).isFailed());
        assertNotNull(errorListener2.originalException);
        assertEquals("In formula: LC = Str2.toLowerCase()", errorListener2.originalException.getMessage());

        try {
            ((DynamicTable) updated2).listenForUpdates(new ErrorListener((DynamicTable) updated2));
            TestCase.fail("Should not be allowed to listen to failed table");
        } catch (IllegalStateException ise) {
            assertEquals("Can not listen to failed table QueryTable", ise.getMessage());
        }

        final ErrorListener errorListener = new ErrorListener((QueryTable)updated);
        ((QueryTable) updated).listenForUpdates(errorListener);

        allowingError(() -> {
            LiveTableMonitor.DEFAULT.runWithinUnitTestCycle(() -> {
                TstUtils.addToTable(source, i(7, 8), c("Str", "g", null), c("Str2", "G", "H"));
                source.notifyListeners(i(7, 8), i(), i());
            });
            return null;
        }, TestSystemicObjectMarking::isNpe);

        assertTrue(((DynamicTable)updated).isFailed());
        assertTrue(((DynamicTable)updated2).isFailed());
        assertNotNull(errorListener.originalException);
        assertEquals("In formula: UC = Str.toUpperCase()", errorListener.originalException.getMessage());

    }


    private static boolean isNpe(List<Throwable> throwables) {
        if (1 !=  throwables.size()) {
            return false;
        }
        if (!throwables.get(0).getClass().equals(FormulaEvaluationException.class)) {
            return false;
        }
        if (!throwables.get(0).getMessage().equals("In formula: UC = Str.toUpperCase()")) {
            return false;
        }
        return throwables.get(0).getCause().getClass().equals(NullPointerException.class);
    }
}
